//val kotlinxImmutableVersion: String by project

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    application
//    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}
springBoot{
    mainClass.set("org.paragontech.charger.ChargerServiceKt")
}

dependencies {
   implementation(project(":common"))
   implementation(project(":test-util"))

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // === Spring Boot ===
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web") // optional for REST endpoints
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")


    // === Kotlin + Jackson ===
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // === AWS Lambda Events for WebSocket Support ===
    implementation("com.amazonaws:aws-lambda-java-events:3.15.0")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("software.amazon.awssdk:sqs:2.25.23")


    // === Kafka (optional, only if you're producing events) ===
//    implementation("org.springframework.kafka:spring-kafka")

    // === Test: JUnit 5 + MockK ===
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito") // exclude if you prefer MockK
    }

    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")




}


tasks.test {
    useJUnitPlatform()
}