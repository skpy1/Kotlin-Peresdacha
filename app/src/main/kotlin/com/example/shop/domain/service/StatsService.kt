package com.example.shop.domain.service

import com.example.shop.domain.model.OrdersStatsResponse
import com.example.shop.domain.repository.OrderRepository

class StatsService(private val orderRepository: OrderRepository) {
    fun ordersStats(): OrdersStatsResponse = orderRepository.stats()
}
