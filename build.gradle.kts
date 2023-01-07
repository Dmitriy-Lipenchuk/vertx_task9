plugins {
    id("java")
}

group = "ru.gamesphere"
version = "1.0-SNAPSHOT"

subprojects  {
    repositories {
        mavenCentral()
    }

    apply {
        plugin ("java")
    }

    dependencies {
        implementation("org.jetbrains:annotations:23.0.0")

        implementation("io.vertx:vertx-core:4.3.5")
        implementation("io.vertx:vertx-hazelcast:4.3.5")

        compileOnly("org.projectlombok:lombok:1.18.24")
        annotationProcessor("org.projectlombok:lombok:1.18.24")
    }
}