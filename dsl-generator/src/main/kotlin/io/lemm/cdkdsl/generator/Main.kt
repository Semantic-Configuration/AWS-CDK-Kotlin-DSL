@file:Suppress("UnstableApiUsage")

package io.lemm.cdkdsl.generator

import com.google.common.reflect.ClassPath
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * You can implement your own generator.
 * Create object implements [ICdkDslGenerator], add it to this, then run [main].
 */
val generators = mutableListOf(
    CoreDslGenerator,
    PlusOperatorFunctionsWrapperGenerator
)

/**
 * This string should be used in [javax.annotation.Generated]'s date parameter
 */
val generationDate: String = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

suspend fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "application argument is required" }
    main(File(if (args.size >= 2) args[1] else "build/generated"), args[0])
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun main(targetDir: File, moduleName: String) {
    val cdkClasses = ClassPath.from(ClassLoader.getSystemClassLoader()).allClasses.asFlow()
        .filter { it.packageName.startsWith("software.amazon.awscdk") }
        .map { it.load() }
        .filter {
            File(it.protectionDomain.codeSource.location.toURI()).name.split('-').dropLast(1)
                .joinToString("-") == moduleName
        }

    val srcDir = File(targetDir, "src/main/kotlin").also { if (!it.exists()) it.mkdirs() }

    cdkClasses.toList().groupBy(Class<*>::getDslPackageName).forEach { (packageName, classes) ->
        generators.asFlow().collect {
            it.run(classes.asFlow(), srcDir, moduleName, packageName)
        }
    }
}

fun getFileSpecBuilder(fileName: String, packageName: String): FileSpec.Builder =
    FileSpec.builder(packageName, fileName).apply {
        addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "FunctionName, Unused").build())
    }

fun Class<*>.getTrimmedPackageName() =
    `package`.name.split('.').drop(3).joinToString(".")

fun Class<*>.getDslPackageName() = "io.lemm.cdk.kotlin.${getTrimmedPackageName()}"
