import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `embedded-kotlin`
    scala
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    @Suppress("DEPRECATION") jcenter() // For Ktor Client 1.3.2
}

dependencies {
    implementation(kotlin("stdlib-jdk8", KotlinVersion.CURRENT.toString()))
    implementation("io.ktor:ktor-client-cio:1.3.2")
    implementation("io.ktor:ktor-client-auth-jvm:1.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-exec
    implementation("org.apache.commons:commons-exec:1.3")

    implementation("org.scala-lang", "scala-library", "2.13.6")
    implementation("io.github.portfoligno.porterie", "porterie_2.13", "0.4.0")
    runtimeOnly("org.slf4j", "slf4j-simple", "1.7.32")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-opt-in", "kotlin.RequiresOptIn")
    }
}
tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf("-target:jvm-1.8")
}
