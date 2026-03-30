package com.example.shop.cache

import com.example.shop.config.RedisConfig
import com.example.shop.domain.model.Order
import com.example.shop.util.AppJson
import io.lettuce.core.RedisClient
import io.lettuce.core.SetArgs
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.serialization.encodeToString

class OrderCache(config: RedisConfig) : AutoCloseable {
    private val client = RedisClient.create(config.uri)
    private val connection: StatefulRedisConnection<String, String> = client.connect()
    private val sync = connection.sync()
    private val ttl = config.ttlSeconds

    fun put(order: Order) {
        sync.set("order:${order.id}", AppJson.encodeToString(order), SetArgs.Builder.ex(ttl))
    }

    fun evict(orderId: Long) {
        sync.del("order:$orderId")
    }

    override fun close() {
        connection.close()
        client.shutdown()
    }
}
