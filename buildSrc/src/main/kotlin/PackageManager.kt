
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import data.PomArtifact
import data.ResponseJson
import data.Version
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.w3c.dom.NodeList
import utility.SuspendedLazy
import utility.cache
import utility.withRetry
import java.net.URL
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

object PackageManager {

    private const val requestUrl =
        "https://search.maven.org/solrsearch/select?q=g:software.amazon.awscdk&rows=400&wt=json&start=0"

    private const val artifactoryBaseUrl =
        "https://chamelania.jfrog.io/artifactory/maven/io/lemm/cdk/kotlin"

    @UseExperimental(KtorExperimentalAPI::class)
    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 30000
        }
        install(HttpTimeout)
    }

    private val lowerBound = Version("1.118.0")
    private val upperBound = Version("2.0.0")

    val allCdkModules = SuspendedLazy {
        println("Start to get list of CDK modules")
        println(requestUrl)
        val obj = jacksonObjectMapper().readValue<ResponseJson>(URL(requestUrl))
        println("Completed getting list of CDK modules")

        obj.response.docs.filter {
            it.ec.containsAll(
                listOf(
                    ".jar",
                    ".pom"
                )
            ) && Version(it.latestVersion) >= lowerBound
        }.map { it.a }.filter { "monocdk" !in it }.toSet()
    }

    val latestCdkDslVersions = SuspendedLazy {
        val results = withContext(IO) {
            println("Start to get latest package version from Artifactory")
            allCdkModules().map { module -> async {
                val dslMavenMetadataUrl =
                    "$artifactoryBaseUrl/$module/maven-metadata.xml"
                println(dslMavenMetadataUrl)

                val response = withRetry {
                    client.get<HttpResponse>(dslMavenMetadataUrl)
                }
                if (response.status != HttpStatusCode.OK) {
                    println("$module may not have published CDK DSL versions yet")
                    return@async module to null
                }
                @Suppress("BlockingMethodInNonBlockingContext")
                val doc =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.content.toInputStream())

                val versionString =
                    doc.getElementsByTagName("latest").item(0).textContent

                val (cdkVersionString, dslVersionString) =
                    versionString.split('-')

                val fullyHandled = arrayOf(
                    "$artifactoryBaseUrl/$module/$versionString/$module-$versionString.pom",
                    "$artifactoryBaseUrl/$module/$versionString/$module-$versionString.module",
                    "$artifactoryBaseUrl/$module/$versionString/$module-$versionString-sources.jar",
                    "$artifactoryBaseUrl/$module/$versionString/$module-$versionString.jar"
                ).all { url ->
                    withRetry {
                        println(url)
                        val r = client.head<HttpResponse>(url)
                        println(r.status)

                        when (r.status) {
                            HttpStatusCode.OK -> true
                            HttpStatusCode.NotFound -> false
                            else -> throw IllegalStateException(r.toString())
                        }
                    }
                }
                module to Triple(fullyHandled, Version(cdkVersionString), Version(dslVersionString))
            } }
        }
        val versions = results.associate { it.await() }
        println("Completed getting latest package version from Artifactory")
        versions
    }

    val cdkVersions =
        SuspendedLazy {
            println("Start to get version list of CDK modules")
            val versions = allCdkModules()
                .map { module -> async(IO) {
                    val cdkMavenMetadataUrl =
                        "https://repo1.maven.org/maven2/software/amazon/awscdk/$module/maven-metadata.xml"
                    println(cdkMavenMetadataUrl)

                    val response =
                        client.get<HttpResponse>(cdkMavenMetadataUrl)
                    val doc =
                        DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(response.content.toInputStream())

                    module to doc.getElementsByTagName("versions").item(0).childNodes.asList()
                        .asSequence()
                        .filter { it.nodeName == "version" }
                        .map { Version(it.textContent) }
                        .filter { it >= lowerBound && it < upperBound }
                        .toSet()
                } }
                .associate { it.await() }
            println("Completed getting version list of CDK modules")
            versions
        }

    val cdkModules = SuspendedLazy<Map<Version, List<String>>> {
        cdkVersions()
            .asSequence()
            .flatMap { (module, versions) ->
                versions.asSequence().map { it to module }
            }
            .groupByTo(TreeMap(), { it.first }) { it.second }
    }

    val unhandledCdkModules = SuspendedLazy<Map<Version, List<String>>> {
        val latestCdkDslVersions = latestCdkDslVersions()

        cdkVersions()
            .asSequence()
            .flatMap { (module, versions) ->
                versions
                    .asSequence()
                    .let { sequence ->
                        latestCdkDslVersions[module]
                            ?.let { (fullyHandled, handledCdkVersion) ->
                                sequence.filter {
                                    if (fullyHandled) {
                                        it > handledCdkVersion
                                    } else {
                                        it >= handledCdkVersion
                                    }
                                }
                            }
                            ?: sequence
                    }
                    .map { it to module }
            }
            .groupByTo(TreeMap(), { it.first }) { it.second }
    }

    val moduleDependency =
        cache { version: Version ->
            println("Start to get dependencies of CDK modules")

            val modules = cdkModules().getOrDefault(version, listOf()).also { modules ->
                println("version: $version, module count: ${modules.size}.")
            }
            val dependencies = modules.map { module ->
                async(IO) {
                    val targetUrl =
                        "https://repo1.maven.org/maven2/software/amazon/awscdk/$module/$version/$module-${version}.pom"
                    println(targetUrl)

                    val doc = withRetry {
                        val response = client.get<HttpResponse>(targetUrl)
                        check(response.status == HttpStatusCode.OK) { "${response.status} on accessing $targetUrl" }

                        @Suppress("BlockingMethodInNonBlockingContext")
                        DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(response.content.toInputStream())
                    }

                    val list = doc.getElementsByTagName("dependency").asList().map { node ->
                        PomArtifact(
                            node.childNodes.asList().single { it.nodeName == "groupId" }.textContent,
                            node.childNodes.asList().single { it.nodeName == "artifactId" }.textContent,
                            node.childNodes.asList().single { it.nodeName == "version" }.textContent
                        )
                    }.filter { it.groupId == "software.amazon.awscdk" }.map { it.artifactId }
                    module to list
                }
            }
            dependencies.associate { it.await() }.also {
                println("Completed getting dependencies of CDK modules")
            }
        }

    suspend fun getUnpublishedModules(cdkVersion: Version, cdkDslVersion: Version) =
        cdkModules()
            .getOrDefault(cdkVersion, listOf())
            .filter { module ->
                latestCdkDslVersions()[module]
                    ?.let { (fullyHandled, handledCdkVersion, handledCdkDslVersion) ->
                        if (fullyHandled) {
                            cdkVersion > handledCdkVersion || cdkDslVersion > handledCdkDslVersion
                        } else {
                            cdkVersion >= handledCdkVersion || cdkDslVersion >= handledCdkDslVersion
                        }
                    }
                    ?: true
            }

    private fun NodeList.asList() =
        List(length, ::item)
}
