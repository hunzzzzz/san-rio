package com.example.sanrio.domain.product.dto.response

import com.example.sanrio.domain.product.model.Product
import java.time.LocalDateTime

data class ProductDetailResponse(
    val productId: Long,
    val name: String,
    val detail: String,
    val stock: Int?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(product: Product) = ProductDetailResponse(
            productId = product.id!!,
            name = product.name,
            detail = product.detail,
            stock = if (product.stock < 10) product.stock else null,
            createdAt = product.createdAt
        )
    }
}