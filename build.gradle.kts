val springBootVersion: String by project
val springDependencyManagementVersion: String by project
plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false

    id("org.flywaydb.flyway") version "9.22.0"
    id("nu.studer.jooq") version "8.2"
}

allprojects {
    group = "com.paragontech"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
//dependencies {
//    implementation("org.jooq:jooq")
////    implementation("org.springframework.boot:spring-boot-starter-jooq")
////    implementation("org.flywaydb:flyway-core")
////    implementation("org.postgresql:postgresql")
//
//    // This is the fix — make the driver available for code generation
//    jooqGenerator("org.postgresql:postgresql:42.7.2")
//}

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

