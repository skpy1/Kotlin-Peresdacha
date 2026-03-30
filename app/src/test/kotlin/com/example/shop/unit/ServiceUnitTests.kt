package com.example.shop.unit

import com.example.shop.config.JwtConfig
import com.example.shop.domain.model.CreateOrderRequest
import com.example.shop.domain.model.CreateOrderItemRequest
import com.example.shop.domain.model.RegisterRequest
import com.example.shop.domain.repository.*
import com.example.shop.domain.service.AuthService
import com.example.shop.domain.service.OrderService
import com.example.shop.domain.service.ProductService
import com.example.shop.util.BusinessException
import com.example.shop.util.ConflictException
import com.example.shop.util.ValidationException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ServiceUnitTests {
    @Test
    fun `product create validates negative stock`() {
        val repo = mockk<ProductRepository>()
        val service = ProductService(repo)
        assertFailsWith<ValidationException> {
            service.create(com.example.shop.domain.model.CreateProductRequest("Phone", "Desc", "100.00", -1))
        }
    }

    @Test
    fun `register throws conflict when user exists`() {
        val repo = mockk<UserRepository>()
        every { repo.findByEmail("user@example.com") } returns UserRow(
            com.example.shop.domain.model.User(1, "user@example.com", "User", com.example.shop.domain.model.UserRole.USER, "2026-01-01T00:00:00"),
            "hash"
        )
        val service = AuthService(JwtConfig("iss", "aud", "realm", "secret", 60), repo)
        assertFailsWith<ConflictException> {
            service.register(RegisterRequest("user@example.com", "secret123", "Ivan"))
        }
    }

    @Test
    fun `order create throws when stock is not enough`() {
        val orderRepository = mockk<OrderRepository>()
        val productRepository = mockk<ProductRepository>()
        val auditRepository = mockk<AuditLogRepository>(relaxed = true)
        val cache = mockk<com.example.shop.cache.OrderCache>(relaxed = true)
        val publisher = mockk<com.example.shop.messaging.OrderEventPublisher>(relaxed = true)
        every { productRepository.fetchForOrder(listOf(1L)) } returns listOf(ProductStockRow(1, "Phone", "100.00".toBigDecimal(), 1))

        val service = OrderService(orderRepository, productRepository, auditRepository, cache, publisher)
        assertFailsWith<BusinessException> {
            service.create(10, CreateOrderRequest(listOf(CreateOrderItemRequest(1, 5))))
        }
        verify(exactly = 0) { auditRepository.write(any(), any(), any()) }
    }
}
