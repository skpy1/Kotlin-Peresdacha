package com.example.shop.worker

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class OrderCreatedEvent(val orderId: Long, val userId: Long, val totalAmount: String)

fun main() {
    val host = System.getenv("RABBITMQ_HOST") ?: "localhost"
    val port = (System.getenv("RABBITMQ_PORT") ?: "5672").toInt()
    val username = System.getenv("RABBITMQ_USER") ?: "guest"
    val password = System.getenv("RABBITMQ_PASSWORD") ?: "guest"
    val queue = System.getenv("RABBITMQ_QUEUE") ?: "orders.events"
    val logger = LoggerFactory.getLogger("OrderWorker")

    val factory = ConnectionFactory().apply {
        this.host = host
        this.port = port
        this.username = username
        this.password = password
    }

    val connection = factory.newConnection("shop-worker")
    val channel = connection.createChannel()
    channel.queueDeclare(queue, true, false, false, null)

    logger.info("Worker started, waiting for messages in queue {}", queue)

    val deliverCallback = DeliverCallback { _, delivery ->
        val event = Json.decodeFromString<OrderCreatedEvent>(String(delivery.body, Charsets.UTF_8))
        logger.info("ORDER EVENT -> orderId={}, userId={}, total={}", event.orderId, event.userId, event.totalAmount)
        logger.info("EMAIL STUB -> Sent fake email for order {}", event.orderId)
        channel.basicAck(delivery.envelope.deliveryTag, false)
    }

    channel.basicConsume(queue, false, deliverCallback) { consumerTag ->
        logger.info("Consumer {} cancelled", consumerTag)
    }
}
