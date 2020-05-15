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
    kotlin("jvm") version "1.3.61"

    // Apply the application plugin to add support for building a CLI application.
    application

    id("com.github.johnrengelman.shadow") version "5.0.0"

    // https://github.com/melix/jmh-gradle-plugin
    id("me.champeau.gradle.jmh") version "0.5.0"
}

application {
    mainClassName = "nl.knaw.huc.di.rd.tag.tagml.lsp.MainKt"
    applicationName = "tagml-language-server"
    group = "nl.knaw.huc.di.rd.tag"
    version = "0.1"
}

repositories {
    mavenLocal()
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    maven {
        url = uri("http://maven.huygens.knaw.nl/repository")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-dev")
    }
    gradlePluginPortal()
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
    val lspVersion = "0.6.0" // LSP 3.14
//    val lspVersion = "0.8.1" // LSP 3.15
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:${lspVersion}")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:${lspVersion}")
//    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.generator:${lsp_version}")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.debug:${lspVersion}")
//    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc.debug:${lsp_version}")
//    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.websocket:${lspVersion}")

    // https://github.com/Kotlin/kotlinx.cli
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.2.0-dev-7")

//    implementation("javax.servlet:javax.servlet-api:4.0.1")
    val alexandriaVersion = "2.3.3-SNAPSHOT"
    implementation("nl.knaw.huygens.alexandria:alexandria-markup-core:${alexandriaVersion}")
//    testImplementation("com.intigua:antlr4-autosuggest:0.0.1-SNAPSHOT")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.assertj:assertj-core:3.12.2")
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

tasks.register("listrepos") {
    doLast {
        println("Repositories:")
        project.repositories.map { it as MavenArtifactRepository }
                .forEach {
                    println("Name: ${it.name}; url: ${it.url}")
                }
    }
}
