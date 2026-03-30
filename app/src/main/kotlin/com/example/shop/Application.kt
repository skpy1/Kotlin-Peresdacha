package com.example.shop

import com.example.shop.cache.OrderCache
import com.example.shop.config.AppConfig
import com.example.shop.db.DatabaseFactory
import com.example.shop.domain.repository.*
import com.example.shop.domain.service.*
import com.example.shop.messaging.OrderEventPublisher
import com.example.shop.plugins.configureHttp
import com.example.shop.plugins.configureMonitoring
import com.example.shop.plugins.configureRouting
import com.example.shop.plugins.configureSecurity
import com.example.shop.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = AppConfig.from(environment.config)
    DatabaseFactory.init(config.database)

    val userRepository = UserRepository()
    val productRepository = ProductRepository()
    val orderRepository = OrderRepository()
    val auditLogRepository = AuditLogRepository()
    val orderCache = OrderCache(config.redis)
    val publisher = OrderEventPublisher(config.rabbitmq)

    val authService = AuthService(config.jwt, userRepository)
    val productService = ProductService(productRepository)
    val orderService = OrderService(orderRepository, productRepository, auditLogRepository, orderCache, publisher)
    val statsService = StatsService(orderRepository)

    configureSerialization()
    configureMonitoring()
    configureHttp()
    configureSecurity(authService, config.jwt)
    configureRouting(authService, productService, orderService, statsService)

    environment.monitor.subscribe(ApplicationStopped) {
        orderCache.close()
        publisher.close()
        DatabaseFactory.close()
    }
}
