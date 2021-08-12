# AWS CDK Kotlin DSL
[![CircleCI](https://circleci.com/gh/justincase-jp/AWS-CDK-Kotlin-DSL/tree/master.svg?style=shield)](
  https://circleci.com/gh/justincase-jp/AWS-CDK-Kotlin-DSL/tree/master
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
  implementation("jp.justincase.aws-cdk-kotlin-dsl", cdk_module, "$cdk_version-$dsl_version")
}
```


## Usage
Please refer to the [`example`](example) project.
