package com.example.shop.domain.service

import com.example.shop.cache.OrderCache
import com.example.shop.domain.model.CreateOrderRequest
import com.example.shop.domain.model.Order
import com.example.shop.domain.repository.*
import com.example.shop.messaging.OrderCreatedEvent
import com.example.shop.messaging.OrderEventPublisher
import com.example.shop.util.BusinessException
import com.example.shop.util.NotFoundException
import com.example.shop.util.ValidationException
import java.math.BigDecimal

class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val auditLogRepository: AuditLogRepository,
    private val orderCache: OrderCache,
    private val publisher: OrderEventPublisher,
) {
    fun create(userId: Long, request: CreateOrderRequest): Order {
        if (request.items.isEmpty()) throw ValidationException("Order items cannot be empty")
        val distinctIds = request.items.map { it.productId }.distinct()
        val products = productRepository.fetchForOrder(distinctIds).associateBy { it.id }
        val rows = request.items.map { item ->
            val product = products[item.productId] ?: throw NotFoundException("Product ${item.productId} not found")
            if (item.quantity <= 0) throw ValidationException("Quantity must be positive")
            if (product.stock < item.quantity) throw BusinessException("Not enough stock for product ${product.id}")
            NewOrderItem(product.id, item.quantity, product.price)
        }

        rows.forEach { productRepository.decreaseStock(it.productId, it.quantity) }
        val total = rows.fold(BigDecimal.ZERO) { acc, item -> acc + item.unitPrice.multiply(BigDecimal(item.quantity)) }
        val order = orderRepository.create(userId, rows, total)
        auditLogRepository.write(userId, "ORDER_CREATED", "orderId=${order.id}; total=${order.totalAmount}")
        orderCache.put(order)
        publisher.publish(OrderCreatedEvent(order.id, userId, order.totalAmount))
        return order
    }

    fun list(userId: Long): List<Order> = orderRepository.listByUser(userId)

    fun cancel(userId: Long, orderId: Long) {
        val cancelled = orderRepository.cancel(orderId, userId)
        if (!cancelled) throw NotFoundException("Active order $orderId not found")
        auditLogRepository.write(userId, "ORDER_CANCELLED", "orderId=$orderId")
        orderCache.evict(orderId)
    }
}
