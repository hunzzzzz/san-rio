package com.example.sanrio.domain.product.dto.response

import com.example.sanrio.domain.product.model.CharacterName
import com.example.sanrio.domain.product.model.Product
import com.example.sanrio.domain.product.model.ProductStatus
import java.time.LocalDateTime

data class ProductDetailResponse(
    val productId: Long,
    val status: ProductStatus,
    val name: String,
    val detail: String,
    val price: Int,
    val stock: Int?,
    val characterName: CharacterName,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(product: Product) = ProductDetailResponse(
            productId = product.id!!,
            status = product.status,
            name = product.name,
            detail = product.detail,
            price = product.price,
            stock = if (product.stock < 10) product.stock else null,
            characterName = product.characterName,
            createdAt = product.createdAt
        )
    }
}