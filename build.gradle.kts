/*
  Usage:

  gradlew build
  - to build a fat jar in build/libs

  gradlew run
  - to run the application

  gradlew distZip
  - to generate a full distribution zip
*/
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
//    id("org.jetbrains.kotlin.jvm") version "1.3.41"

    // Apply the application plugin to add support for building a CLI application.
    application

    id("com.github.johnrengelman.shadow") version "5.0.0"
    kotlin("jvm") version "1.3.61"
}

application {
    mainClassName = "nl.knaw.huc.di.rd.tag.tagml.lsp.MainKt"
    applicationName = "tagml-language-server"
    group = "nl.knaw.huc.di.rd.tag"
    version = "1.0-SNAPSHOT"
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    maven {
        url = uri("http://maven.huygens.knaw.nl/repository")
    }
}

dependencies {
    compileOnly("javax.servlet:javax.servlet-api:3.1.0")
    compileOnly("javax.servlet.jsp:javax.servlet.jsp-api:2.3.1")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use parsec.
    implementation("lambdada:parsec:1.0")

    // Use Arrow for fp
    val arrowVersion = "0.10.2"
    implementation("io.arrow-kt:arrow-core:${arrowVersion}")
    implementation("io.arrow-kt:arrow-core-data:${arrowVersion}")

    // https://github.com/eclipse/lsp4j
    val lspVersion = "0.8.1"
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:${lspVersion}")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:${lspVersion}")
//    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.generator:${lsp_version}")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.debug:${lspVersion}")
//    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc.debug:${lsp_version}")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.websocket:${lspVersion}")

//    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("nl.knaw.huygens.alexandria:alexandria-markup-core:2.3.2-SNAPSHOT")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("io.github.microutils:kotlin-logging:1.7.7")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        // defaults to project.name
        //archiveBaseName.set("${project.name}-fat")

        // defaults to all, so removing this overrides the normal, non-fat jar
        archiveClassifier.set("")
    }

    jar {
        manifest {
            attributes(
                    mapOf("Implementation-Title" to project.name,
                            "Implementation-Version" to project.version,
                            "Main-Class" to application.mainClassName)
            )
        }
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}