plugins {
  kotlin("jvm") version "1.4.32"
  application
}

tasks {
  wrapper {
    gradleVersion = "7.6.1"
  }

  compileKotlin {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
  }

  "run" {
    // Allow skipping code execution conditionally
    outputs.dir("cdk.out")
  }
}
application.mainClass.set("io.lemm.cdkdsl.example.Main")

repositories {
  mavenCentral()
  maven("https://cdk.lemm.io/maven")
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("io.lemm.cdk.kotlin", "s3", "1.118.0-0.7.2")
}
