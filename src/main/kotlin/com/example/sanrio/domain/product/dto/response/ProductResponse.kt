package com.example.sanrio.domain.product.dto.response

import com.example.sanrio.domain.product.model.ProductStatus
import java.time.LocalDateTime

data class ProductResponse(
    val productId: Long,
    val status: ProductStatus,
    val name: String,
    val price: Int,
    val characterName: String,
    val createdAt: LocalDateTime
)