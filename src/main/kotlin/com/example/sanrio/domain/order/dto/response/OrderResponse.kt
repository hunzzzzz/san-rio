package com.example.sanrio.domain.order.dto.response

import com.example.sanrio.domain.order.model.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val orderId: Long,
    val code: String,
    val status: OrderStatus,
    val userName: String,
    val userAddress: String,
    val totalPrice: Int,
    val createdAt: LocalDateTime
)
