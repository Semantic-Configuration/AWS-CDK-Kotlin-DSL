@file:Suppress("SuspiciousCollectionReassignment")
@file:JvmName("Main")
package io.lemm.cdkdsl.example

import io.lemm.cdk.kotlin.services.s3.Bucket
import io.lemm.cdk.kotlin.services.s3.BucketMetrics
import io.lemm.cdk.kotlin.services.s3.CfnBucket
import software.amazon.awscdk.services.s3.BucketEncryption

fun main() = synthesize {
  Stack("example-stack") {
    // Define an S3 bucket
    Bucket("MyBucket") {
      versioned = true
      encryption = BucketEncryption.S3_MANAGED
      metrics += BucketMetrics {
        id = "example-id"
        tagFilters += "key1" to "value1"
      }
    }

    // Define an S3 bucket using escape hatches
    // (https://docs.aws.amazon.com/cdk/latest/guide/cfn_layer.html)
    CfnBucket("MyCfnBucket") {
      bucketName = "my-cfn-bucket"
      objectLockEnabled /= true
    }
  }
}
