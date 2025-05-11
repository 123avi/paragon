plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("nu.studer.jooq") version "8.2"
    id("org.flywaydb.flyway") version "9.22.0"

}

//springBoot{
//    mainClass.set("org.paragontech.charger.ChargerServiceKt")
//}

dependencies {
    implementation(project(":common"))
    implementation(project(":test-util"))
    implementation(project(":db-charger"))


    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    // === Spring Boot ===
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web") // optional for REST endpoints
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // === Spring Boot + JOOQ ===
    implementation("org.jooq:jooq") // or latest
    implementation("org.springframework.boot:spring-boot-starter-jooq")
//    implementation("org.postgresql:postgresql:42.7.3") // or your DB driver
    implementation("org.flywaydb:flyway-core")
    // PostgreSQL driver for runtime (for Spring)
    implementation("org.postgresql:postgresql:42.7.3")

    // PostgreSQL driver for jOOQ code generation
    jooqGenerator("org.postgresql:postgresql:42.7.3")

    //for dev-time DB access
    implementation("com.zaxxer:HikariCP")

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
// Define flywayRuntime configuration if it's not implicitly created
configurations {
    create("flywayRuntime")
}
flyway {
    url = "jdbc:postgresql://localhost:5432/chargers"
    user = "dev"
    password = "devpass"
    locations = arrayOf("filesystem:src/main/resources/db/migration")

}

jooq {
    version.set("3.18.6") // match your jOOQ version
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5433/chargers"
                    user = "dev"
                    password = "devpass"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isImmutablePojos = true
                        isPojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.paragon.charger.jooq"
                        directory = "src/generated/jooq"
                    }
                }
            }
        }
    }
}

// Run Flyway before generating JOOQ code
tasks.named("generateJooq") {
    dependsOn("flywayMigrate")
}

// Run generateJooq before build
tasks.named("build") {
    dependsOn("generateJooq")
}

// Ensure generated code is compiled
sourceSets {
    main {
        java {
            srcDir("src/generated/jooq")
        }
    }
}
// Add this task to debug classpath issues
tasks.register("printFlywayClasspath") {
    doLast {
        println("Flyway classpath:")
        configurations.getByName("flywayRuntime").files.forEach { println(it) }
    }
}
tasks.test {
    useJUnitPlatform()
}