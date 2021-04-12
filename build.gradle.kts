plugins {
    kotlin("jvm") version "1.4.10"
    id("maven-publish")
    id("com.jfrog.bintray") version "1.8.4"
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "6.2.2"
}

fun String.removePrefixOrNull(prefix: String): String? =
    takeIf { it.startsWith(prefix) }?.removePrefix(prefix)

val kotlinVersion: String by project
val awsCdkVersion: String by project
val dslVersion =
    System.getenv("CIRCLE_TAG")?.removePrefixOrNull("v")
        ?: System.getenv("CIRCLE_BRANCH")?.removePrefixOrNull("release/")
        ?: "unspecified"

allprojects {
    group = "jp.justincase"
    version = "$awsCdkVersion-$dslVersion"

    repositories {
        mavenCentral()
    }
}

// todo create bintray package task
tasks {
    if (System.getenv("bintrayApiKey") != null || System.getenv()["bintrayApiKey"] != null || project.hasProperty("bintrayApiKey")) {
        val bintrayUser = System.getenv("bintrayUser") ?: System.getenv()["bintrayUser"]
        ?: project.findProperty("bintrayUser") as String
        val bintrayKey = System.getenv("bintrayApiKey") ?: System.getenv()["bintrayApiKey"]
        ?: project.findProperty("bintrayApiKey") as String

        val genLatest = register("generateAndBuildForLatestVersion") {
            group = "cdk-dsl"
            dependsOn(getByPath(":dsl-generator:publishToMavenLocal"))
            dependsOn(getByPath(":dsl-common:publishToMavenLocal"))
            doLastBlocking {
                BuildFileGenerator.generateAndBuildForLatestVersion(
                    kotlinVersion,
                    dslVersion,
                    File(buildDir, "cdkdsl"),
                    bintrayUser,
                    bintrayKey
                )
            }
        }

        val genUnhandled = register("generateAndBuildForUnhandledCdkVersions") {
            group = "cdk-dsl"
            dependsOn(getByPath(":dsl-generator:publishToMavenLocal"))
            dependsOn(getByPath(":dsl-common:publishToMavenLocal"))
            doLastBlocking {
                BuildFileGenerator.generateAndBuildForUnhandledCdkVersions(
                    kotlinVersion,
                    dslVersion,
                    File(buildDir, "cdkdsl"),
                    bintrayUser,
                    bintrayKey
                )
            }
        }

        val createPackages = register("createBintrayPackages") {
            group = "cdk-dsl"
            doLastBlocking {
                PackageManager.createBintrayPackages(bintrayUser, bintrayKey)
            }
        }

        register("publishForLatestVersion") {
            group = "cdk-dsl"
            dependsOn(genLatest)
            dependsOn(createPackages)
            doLastBlocking {
                BuildFileGenerator.publishForLatestVersion(File(buildDir, "cdkdsl"))
            }
        }

        register("publishForUnhandledCdkVersions") {
            group = "cdk-dsl"
            dependsOn(genUnhandled)
            dependsOn(createPackages)
            doLastBlocking {
                BuildFileGenerator.publishForUnhandledCdkVersions(File(buildDir, "cdkdsl"))
            }
        }
    }
}
