import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
//    kotlin("jvm") version "2.1.20" apply false
    kotlin("jvm")
    id("org.springframework.boot") version "3.2.2" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    group = "com.evdor"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    }

    kotlin {
        jvmToolchain(22)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
