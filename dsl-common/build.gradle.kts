import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "io.lemm.cdk.kotlin"
version = (rootProject.version as String).let {
    if (it == "unsupecified") it else it.split("-")[1]
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.publish {
    dependsOn(":startGitHubPackagesProxy")
}

val taskSourceJar by tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}


publishing {
    publications {
        register("maven", MavenPublication::class) {
            groupId = "io.lemm.cdk.kotlin"
            artifactId = "dsl-common"
            version = project.version as String

            from(components["java"])
            artifact(taskSourceJar)
        }
        repositories {
            maven {
                name = "GitHubPackages"
                isAllowInsecureProtocol = true
                url = uri("http://localhost:38877")
                credentials {
                    username =
                        System.getenv("GITHUB_USER") ?: "${findProperty("GITHUB_USER") ?: "unset"}"
                    password =
                        System.getenv("GITHUB_TOKEN") ?: "${findProperty("GITHUB_TOKEN") ?: "unset"}"
                }
            }
        }
    }
}
