# AWS CDK Kotlin DSL
[![CircleCI](https://circleci.com/gh/Semantic-Configuration/AWS-CDK-Kotlin-DSL/tree/main.svg?style=shield)](
  https://circleci.com/gh/Semantic-Configuration/AWS-CDK-Kotlin-DSL/tree/main
)

[**日本語**](README-JA.md)


## Installation
Gradle Kotlin DSL

```kotlin
repositories {
  mavenCentral()
  maven(url = "https://chamelania.lemm.io")
}

dependencies {
  implementation("io.lemm.cdk.kotlin", cdk_module, "$cdk_version-$dsl_version")
}
```


## Usage
Please refer to the [`example`](example) project.
