package com.example.shop.config

import io.ktor.server.config.*

data class AppConfig(
    val jwt: JwtConfig,
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val rabbitmq: RabbitMqConfig,
) {
    companion object {
        fun from(config: ApplicationConfig) = AppConfig(
            jwt = JwtConfig(
                issuer = config.property("shop.jwt.issuer").getString(),
                audience = config.property("shop.jwt.audience").getString(),
                realm = config.property("shop.jwt.realm").getString(),
                secret = System.getenv("JWT_SECRET") ?: config.property("shop.jwt.secret").getString(),
                expiresInMinutes = config.property("shop.jwt.expiresInMinutes").getString().toLong(),
            ),
            database = DatabaseConfig(
                jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: config.property("shop.database.jdbcUrl").getString(),
                user = System.getenv("JDBC_DATABASE_USER") ?: config.property("shop.database.user").getString(),
                password = System.getenv("JDBC_DATABASE_PASSWORD") ?: config.property("shop.database.password").getString(),
                driverClassName = config.property("shop.database.driverClassName").getString(),
                maximumPoolSize = config.property("shop.database.maximumPoolSize").getString().toInt(),
            ),
            redis = RedisConfig(
                uri = System.getenv("REDIS_URL") ?: config.property("shop.redis.uri").getString(),
                ttlSeconds = config.property("shop.redis.ttlSeconds").getString().toLong(),
            ),
            rabbitmq = RabbitMqConfig(
                host = System.getenv("RABBITMQ_HOST") ?: config.property("shop.rabbitmq.host").getString(),
                port = (System.getenv("RABBITMQ_PORT") ?: config.property("shop.rabbitmq.port").getString()).toInt(),
                username = System.getenv("RABBITMQ_USER") ?: config.property("shop.rabbitmq.username").getString(),
                password = System.getenv("RABBITMQ_PASSWORD") ?: config.property("shop.rabbitmq.password").getString(),
                queue = System.getenv("RABBITMQ_QUEUE") ?: config.property("shop.rabbitmq.queue").getString(),
            )
        )
    }
}

data class JwtConfig(val issuer: String, val audience: String, val realm: String, val secret: String, val expiresInMinutes: Long)
data class DatabaseConfig(val jdbcUrl: String, val user: String, val password: String, val driverClassName: String, val maximumPoolSize: Int)
data class RedisConfig(val uri: String, val ttlSeconds: Long)
data class RabbitMqConfig(val host: String, val port: Int, val username: String, val password: String, val queue: String)
