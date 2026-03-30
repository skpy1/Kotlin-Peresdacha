plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.example.shop"
version = "1.0.0"
application {
    mainClass.set("com.example.shop.worker.WorkerMainKt")
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.25.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("ch.qos.logback:logback-classic:1.5.18")
}
