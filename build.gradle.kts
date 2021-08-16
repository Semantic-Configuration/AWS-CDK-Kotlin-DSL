import data.Version

plugins {
    kotlin("jvm") version "1.4.10"
    id("maven-publish")
}

tasks.wrapper {
    gradleVersion = "7.1.1"
}

fun String.removePrefixOrNull(prefix: String): String? =
    takeIf { it.startsWith(prefix) }?.removePrefix(prefix)

val kotlinVersion: String by project
val awsCdkVersion: String by project
val dslVersion =
    System.getenv("CIRCLE_TAG")?.removePrefixOrNull("v")
        ?: System.getenv("CIRCLE_BRANCH")?.removePrefixOrNull("release/")

allprojects {
    group = "io.lemm.cdk.kotlin.local"
    version = "$awsCdkVersion-${dslVersion ?: "unspecified"}"

    repositories {
        mavenCentral()
    }
}

tasks {
    create("startGitHubPackagesProxy") {
        doLast {
            startGitHubPackagesProxy()
        }
    }

    if (System.getenv("GITHUB_TOKEN") != null || System.getenv()["GITHUB_TOKEN"] != null || project.hasProperty("GITHUB_TOKEN")) {
        val githubUser = System.getenv("GITHUB_USER") ?: System.getenv()["GITHUB_USER"]
        ?: project.findProperty("GITHUB_USER") as String
        val githubKey = System.getenv("GITHUB_TOKEN") ?: System.getenv()["GITHUB_TOKEN"]
        ?: project.findProperty("GITHUB_TOKEN") as String

        val githubCredential = githubUser to githubKey

        create("buildUnhandled") {
            group = "cdk-dsl"
            dependsOn(getByPath(":dsl-generator:publishToMavenLocal"))
            dependsOn(getByPath(":dsl-common:publishToMavenLocal"))
            doLastBlocking {
                BuildFileGenerator.buildUnhandled(
                    kotlinVersion,
                    dslVersion,
                    File(buildDir, "cdkdsl"),
                    githubCredential
                )
            }
        }

        if (dslVersion != null) create("publishUnhandled") {
            group = "cdk-dsl"
            dependsOn(getByPath(":dsl-generator:publishToMavenLocal"))
            dependsOn(getByPath(":dsl-common:publishToMavenLocal"))
            doLastBlocking {
                BuildFileGenerator.publishUnhandled(
                    kotlinVersion,
                    dslVersion,
                    File(buildDir, "cdkdsl"),
                    githubCredential
                )
            }
        }
    }

    create("buildSpecified") {
        group = "cdk-dsl"
        dependsOn(getByPath(":dsl-generator:publishToMavenLocal"))
        dependsOn(getByPath(":dsl-common:publishToMavenLocal"))
        doLastBlocking {
            BuildFileGenerator.buildSpecified(
                kotlinVersion,
                dslVersion,
                File(buildDir, "cdkdsl"),
                null,
                Version(awsCdkVersion)
            )
        }
    }
}
