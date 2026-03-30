plugins {
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
    id("io.ktor.plugin") version "2.3.12" apply false
    id("org.flywaydb.flyway") version "11.8.2" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "21"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
