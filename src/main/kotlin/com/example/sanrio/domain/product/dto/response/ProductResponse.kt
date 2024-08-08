package com.example.sanrio.domain.product.dto.response

import java.time.LocalDateTime

data class ProductResponse(
    val productId: Long,
    val name: String,
    val createdAt: LocalDateTime
)