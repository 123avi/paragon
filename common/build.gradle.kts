val kotlinxImmutableVersion: String by project
val springWebVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxImmutableVersion")
    // https://mvnrepository.com/artifact/org.springframework/spring-web
    implementation("org.springframework:spring-web:$springWebVersion")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.microutils:kotlin-logging:3.0.5")

}
