package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.dto.response.OrderItemResponse
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepositoryCustom {
    fun getOrderItems(orderId: Long): List<OrderItemResponse>
}