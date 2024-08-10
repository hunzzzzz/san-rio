package com.example.sanrio.domain.order.dto.response

data class OrderItemResponse(
    val productName: String,
    val unitPrice: Int,
    val count: Int
)