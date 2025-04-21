val kotlinxImmutableVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxImmutableVersion")
    implementation(project(":common"))
    testImplementation(kotlin("test"))

}


tasks.test {
    useJUnitPlatform()
}