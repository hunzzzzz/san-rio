package com.example.sanrio.domain.cart.dto.response

import com.example.sanrio.domain.product.model.ProductStatus

data class CartItemResponse(
    val productId: Long,
    val productStatus: ProductStatus,
    val productName: String,
    val productTotalPrice: Int,
    val count: Int
)
