package com.example.shop.integration

import com.example.shop.cache.OrderCache
import com.example.shop.config.RedisConfig
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import redis.clients.jedis.Jedis
import com.redis.testcontainers.RedisContainer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
class IntegrationTests {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @Container
        @JvmStatic
        val redis = RedisContainer(com.redis.testcontainers.RedisContainer.DEFAULT_IMAGE_NAME.withTag("7.2-alpine"))
    }

    @Test
    fun `flyway applies schema successfully`() {
        val flyway = Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("filesystem:src/main/resources/db/migration")
            .load()
        val migrations = flyway.migrate()
        assertTrue(migrations.migrationsExecuted >= 1)
    }

    @Test
    fun `redis cache stores order with ttl`() {
        val cache = OrderCache(RedisConfig("redis://${redis.host}:${redis.firstMappedPort}", 60))
        cache.put(com.example.shop.domain.model.Order(1, 1, com.example.shop.domain.model.OrderStatus.CREATED, "100", "2026-01-01T00:00:00", emptyList()))
        Jedis(redis.redisURI).use {
            assertEquals("1", it.exists("order:1").toString())
        }
        cache.close()
    }
}
