plugins {
    alias(libs.plugins.ktor)
    application
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    id("com.google.devtools.ksp") version "2.1.20-2.0.0"
    id("eu.vendeli.telegram-bot") version "8.1.0"
}

group = "io.github.kroune"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api(libs.logback.classic)
    api(libs.logback.loki)
    api("io.github.oshai:kotlin-logging-jvm:7.0.3")

    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.auth)

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // micrometer
    implementation("io.ktor:ktor-server-metrics-micrometer:3.0.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.14.3")

    implementation(libs.ktor.client.content.negotiation.jvm)
    implementation("io.ktor:ktor-client-content-negotiation:3.1.2")

    api(libs.ktor.serialization.kotlinx.json.jvm)
    api(libs.kotlinx.serialization.json)

    implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.0.3"))
    testImplementation(kotlin("test"))

    api(libs.insert.koin.core)
    implementation(libs.insert.koin.ktor3)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    api(libs.exposed.json)
    implementation(libs.postgresql)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}