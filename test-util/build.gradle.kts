plugins {
    kotlin("jvm")
}

group = "com.paragontech"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("io.mockk:mockk:1.13.10")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}