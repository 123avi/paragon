plugins {
//    kotlin("jvm") version "2.1.20" apply false
    kotlin("jvm")
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects{
    group = "com.paragontech"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }


    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

        // MockK (for mocking Kotlin classes and functions)
        testImplementation("io.mockk:mockk:1.13.10")

        // Optional: JSON assertions / parsing
        testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    }

    kotlin {
        jvmToolchain(22)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
