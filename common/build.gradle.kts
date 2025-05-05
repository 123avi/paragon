val kotlinxImmutableVersion: String by project
val springWebVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxImmutableVersion")
    // https://mvnrepository.com/artifact/org.springframework/spring-web
    implementation("org.springframework:spring-web:$springWebVersion")
}
