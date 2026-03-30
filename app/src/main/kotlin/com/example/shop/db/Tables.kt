package com.example.shop.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val role = varchar("role", 32).index()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    override val primaryKey = PrimaryKey(id)
}

object ProductsTable : Table("products") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255).index()
    val description = text("description")
    val price = decimal("price", 10, 2)
    val stock = integer("stock")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
    override val primaryKey = PrimaryKey(id)
}

object OrdersTable : Table("orders") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE).index()
    val status = varchar("status", 32).index()
    val totalAmount = decimal("total_amount", 10, 2)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    override val primaryKey = PrimaryKey(id)
}

object OrderItemsTable : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").references(OrdersTable.id, onDelete = ReferenceOption.CASCADE).index()
    val productId = long("product_id").references(ProductsTable.id, onDelete = ReferenceOption.RESTRICT).index()
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2)
    override val primaryKey = PrimaryKey(id)
}

object AuditLogsTable : Table("audit_logs") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").nullable().index()
    val action = varchar("action", 100).index()
    val payload = text("payload")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    override val primaryKey = PrimaryKey(id)
}
