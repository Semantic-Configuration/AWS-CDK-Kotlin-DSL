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
  maven("https://cdk.lemm.io/maven")
}

dependencies {
  implementation("io.lemm.cdk.kotlin", cdk_module, "$cdk_version-$dsl_version")
}
```
For reference, a list of published versions may be accessed via [core/maven-metadata.xml](
  https://cdk.lemm.io/maven/io/lemm/cdk/kotlin/core/maven-metadata.xml
) (or [here](
  https://cdk.lemm.io/maven/jp/justincase/aws-cdk-kotlin-dsl/core/maven-metadata.xml
), for old packages).

## Usage
Please refer to the [`example`](example) project.
