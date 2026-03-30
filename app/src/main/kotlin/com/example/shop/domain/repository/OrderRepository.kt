package com.example.shop.domain.repository

import com.example.shop.db.OrderItemsTable
import com.example.shop.db.OrdersTable
import com.example.shop.db.ProductsTable
import com.example.shop.domain.model.Order
import com.example.shop.domain.model.OrderItem
import com.example.shop.domain.model.OrderStatus
import com.example.shop.domain.model.OrdersStatsResponse
import com.example.shop.domain.model.asPlain
import com.example.shop.domain.model.asText
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderRepository {
    fun create(userId: Long, items: List<NewOrderItem>, totalAmount: BigDecimal): Order = transaction {
        val orderId = OrdersTable.insert {
            it[OrdersTable.userId] = userId
            it[OrdersTable.status] = OrderStatus.CREATED.name
            it[OrdersTable.totalAmount] = totalAmount
            it[OrdersTable.createdAt] = LocalDateTime.now()
        }[OrdersTable.id]

        OrderItemsTable.batchInsert(items) { item ->
            this[OrderItemsTable.orderId] = orderId
            this[OrderItemsTable.productId] = item.productId
            this[OrderItemsTable.quantity] = item.quantity
            this[OrderItemsTable.unitPrice] = item.unitPrice
        }
        getById(orderId, userId)!!
    }

    fun listByUser(userId: Long): List<Order> = transaction {
        OrdersTable.selectAll().where { OrdersTable.userId eq userId }.orderBy(OrdersTable.id, SortOrder.DESC).map { row ->
            row.toOrder(loadItems(row[OrdersTable.id]))
        }
    }

    fun getById(orderId: Long, userId: Long): Order? = transaction {
        OrdersTable.selectAll().where { (OrdersTable.id eq orderId) and (OrdersTable.userId eq userId) }.singleOrNull()?.let {
            it.toOrder(loadItems(orderId))
        }
    }

    fun cancel(orderId: Long, userId: Long): Boolean = transaction {
        OrdersTable.update({ (OrdersTable.id eq orderId) and (OrdersTable.userId eq userId) and (OrdersTable.status eq OrderStatus.CREATED.name) }) {
            it[status] = OrderStatus.CANCELLED.name
        } > 0
    }

    fun stats(): OrdersStatsResponse = transaction {
        val total = OrdersTable.selectAll().count()
        val cancelled = OrdersTable.selectAll().where { OrdersTable.status eq OrderStatus.CANCELLED.name }.count()
        val revenue = OrdersTable
            .slice(OrdersTable.totalAmount)
            .select { OrdersTable.status eq OrderStatus.CREATED.name }
            .map { it[OrdersTable.totalAmount] }
            .fold(BigDecimal.ZERO, BigDecimal::add)
        OrdersStatsResponse(total, cancelled, revenue.asPlain())
    }

    private fun loadItems(orderId: Long): List<OrderItem> = (OrderItemsTable innerJoin ProductsTable)
        .slice(OrderItemsTable.productId, OrderItemsTable.quantity, OrderItemsTable.unitPrice, ProductsTable.name)
        .select { OrderItemsTable.orderId eq orderId }
        .map {
            OrderItem(
                productId = it[OrderItemsTable.productId],
                productName = it[ProductsTable.name],
                quantity = it[OrderItemsTable.quantity],
                unitPrice = it[OrderItemsTable.unitPrice].asPlain(),
            )
        }

    private fun ResultRow.toOrder(items: List<OrderItem>) = Order(
        id = this[OrdersTable.id],
        userId = this[OrdersTable.userId],
        status = OrderStatus.valueOf(this[OrdersTable.status]),
        totalAmount = this[OrdersTable.totalAmount].asPlain(),
        createdAt = this[OrdersTable.createdAt].asText(),
        items = items,
    )
}

data class NewOrderItem(val productId: Long, val quantity: Int, val unitPrice: BigDecimal)
