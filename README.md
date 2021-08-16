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

## Development Policy
There is a chance that I will reconsider this, but basically, for now:
* This project will continue to aim for a straightforward translation of CDK interfaces
  for idiomatic Kotlin until we have official support -
  [Kotlin language support (#557)](https://github.com/aws/aws-cdk/issues/557).
* To maximize the portability of user projects for the official Kotlin support,
  new additions that do not fit well into the scope of a straightforward translation
  will be organized into separate modules. 
