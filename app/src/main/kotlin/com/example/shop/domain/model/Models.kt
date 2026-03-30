package com.example.shop.domain.model

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
enum class UserRole { USER, ADMIN }

@Serializable
enum class OrderStatus { CREATED, CANCELLED }

@Serializable
data class User(val id: Long, val email: String, val fullName: String, val role: UserRole, val createdAt: String)

@Serializable
data class Product(val id: Long, val name: String, val description: String, val price: String, val stock: Int, val createdAt: String, val updatedAt: String)

@Serializable
data class OrderItem(val productId: Long, val productName: String, val quantity: Int, val unitPrice: String)

@Serializable
data class Order(val id: Long, val userId: Long, val status: OrderStatus, val totalAmount: String, val createdAt: String, val items: List<OrderItem>)

@Serializable
data class AuditLog(val id: Long, val userId: Long?, val action: String, val payload: String, val createdAt: String)

@Serializable
data class RegisterRequest(val email: String, val password: String, val fullName: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: User)

@Serializable
data class CreateProductRequest(val name: String, val description: String, val price: String, val stock: Int)

@Serializable
data class UpdateProductRequest(val name: String, val description: String, val price: String, val stock: Int)

@Serializable
data class CreateOrderItemRequest(val productId: Long, val quantity: Int)

@Serializable
data class CreateOrderRequest(val items: List<CreateOrderItemRequest>)

@Serializable
data class OrdersStatsResponse(val totalOrders: Long, val cancelledOrders: Long, val revenue: String)

@Serializable
data class ApiMessage(val message: String)

fun BigDecimal.asPlain() = stripTrailingZeros().toPlainString()
fun LocalDateTime.asText(): String = toString()
