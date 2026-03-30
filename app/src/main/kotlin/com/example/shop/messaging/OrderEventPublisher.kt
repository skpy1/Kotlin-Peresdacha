package com.example.shop.messaging

import com.example.shop.config.RabbitMqConfig
import com.example.shop.util.AppJson
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class OrderCreatedEvent(val orderId: Long, val userId: Long, val totalAmount: String)

class OrderEventPublisher(private val config: RabbitMqConfig) : AutoCloseable {
    private val factory = ConnectionFactory().apply {
        host = config.host
        port = config.port
        username = config.username
        password = config.password
    }
    private val connection: Connection = factory.newConnection("shop-api")
    private val channel: Channel = connection.createChannel().apply {
        queueDeclare(config.queue, true, false, false, null)
        confirmSelect()
    }

    fun publish(event: OrderCreatedEvent) {
        channel.basicPublish("", config.queue, null, AppJson.encodeToString(event).toByteArray())
        channel.waitForConfirmsOrDie(5000)
    }

    override fun close() {
        channel.close()
        connection.close()
    }
}
