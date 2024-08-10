package com.example.sanrio.domain.order.service

import com.example.sanrio.domain.order.repository.OrderItemRepository
import com.example.sanrio.domain.order.repository.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository
) {
}