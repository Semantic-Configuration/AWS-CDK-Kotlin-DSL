# AWS CDK Kotlin DSL
[![CircleCI](https://circleci.com/gh/Semantic-Configuration/AWS-CDK-Kotlin-DSL/tree/main.svg?style=shield)](
  https://circleci.com/gh/Semantic-Configuration/AWS-CDK-Kotlin-DSL/tree/main
)

このライブラリは、[AWS CDK Java](https://mvnrepository.com/artifact/software.amazon.awscdk)のラッパーライブラリです。  
AWS CDKの各モジュールに対してヘルパー関数・ライブラリ群が自動生成され、Kotlin DSLでインフラ設定が書けるようになります。  
Circle CIにより毎日、日本標準時で午後2時にCDKのアップデートのチェックが行われ、アップデートがあった場合はコード生成・デプロイが行われます。

[**English**](README.md)


## インストール
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

AWS-CDK-Kotlin-DSLの各モジュールは、[AWS CDK Java](https://mvnrepository.com/artifact/software.amazon.awscdk)の各モジュールに1:1で対応しています。  
"$moduleName"の部分に適宜必要なモジュール名を補完してください。

アップロード済みバージョンの一覧は [core/maven-metadata.xml](
  https://cdk.lemm.io/maven/io/lemm/cdk/kotlin/core/maven-metadata.xml
) か、（古いバージョンの場合）[ここ](
  https://cdk.lemm.io/maven/jp/justincase/aws-cdk-kotlin-dsl/core/maven-metadata.xml
)を確認してください。


# 使用方法
完全な例は [`example`](example) プロジェクトにあります。

## チュートリアル
以下の内容は、[AWS CDKのチュートリアル](https://docs.aws.amazon.com/ja_jp/cdk/latest/guide/getting_started.html#hello_world_tutorial)の様に、S3 Bucketを追加してみます。
### AppとStackの作成
CDKそのもののセットアップの解説は省略します。  
Appを作成し、その下にStackを作成、その後synthするという流れはJavaと同じです。

```kotlin
fun main() {
    App().apply {
        exampleStack()
        synth()
    }
}
```

`exampleStack()` は以下の様に定義します。

```kotlin
import io.lemm.cdk.kotlin.core.*

fun App.exampleStack() = Stack("example-stack") {
    // 必要があればStackの設定をここで行います。
    // 無ければ省略可能です。
    // 詳細な説明は省略します。
}.apply {
    // 以後、ここにResourceの設定を足していきます。
}
```

### S3 Bucketの追加
以下のように記述することでS3 Bucketを追加できます。

```kotlin
import io.lemm.cdk.kotlin.services.s3.*

Bucket("MyFirstBucket") {
    versioned = true
    encryption = BucketEncryption.S3_MANAGED
}
```

### '+'演算子
CDK Kotlin DSLのスコープ内では、nullableなListとMapに対して+演算子及び+=演算子が利用可能です。  
以下はその利用例です。

```kotlin
Bucket("MyFirstBucket") {
    versioned = true
    encryption = BucketEncryption.S3_MANAGED
    metrics += BucketMetrics {
        id = "example"
        tagFilters += "a" to "b"
    }
}
```

この'+'演算子は、元のList/Mapがnullの時は新しいList/Mapを作って返すという処理を行う拡張関数です。  
そのため、これを使うとIntelliJで警告が出るため、以下のアノテーションをファイルに追加しておくことを推奨します。

```kotlin
@file:Suppress("SuspiciousCollectionReassignment")
```

### Union型
AWS CDKは元はTypeScriptで記述されており、Jsiiというライブラリにより自動生成されたコードを通してJVMからTypeScriptのAPIにアクセスしています。  
そのため、TypeScriptに存在していてもJava/Kotlinでは存在しない言語機能が使われている場合、コードが複雑になる場合があります。  
CDKではいくつかのプロパティに`Union型`というJava/Kotlinに無い機能が使われています。  
これを表現するため、AWS-CDK-Kotlin-DSLではsealed classを用いています。  
そのため、元となるクラスとsealed classへの変換処理が必要になります。  
ここでは、`CfnBucket`クラスを例に該当プロパティの利用方法を見ていきましょう。

```kotlin
CfnBucket("MyCfnBucket") {
    bucketName = "cfn-bucket"
}
```

`CfnBucket`の`objectLockEnabled`というプロパティを例にとってみましょう。  
`objectLockEnabled`プロパティは、元は`Boolean`と`IResolvable`を取りうるUnion型です。  
このプロパティに`true`を代入する事を考えます。  
もっとも安直な方法はコンストラクターを直接呼び出すことです。

```kotlin
objectLockEnabled = CfnBucketPropsBuilderScope.ObjectLockEnabled.Boolean(true)
```

ただし、これは非常に冗長です。  
これよりも非常に単純で古典的な方法があります。  

```kotlin
objectLockEnabled = true.toObjectLockEnabled()
```

最後に、コードは非常に短くなるものの、文脈によってはわかりにくい方法を紹介します。

```kotlin
objectLockEnabled /= true
```

各関数はDSLのスコープ内でのみ利用可能な拡張関数として実装されているため、衝突等の可能性が無く安全です。
