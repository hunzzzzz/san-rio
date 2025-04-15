package com.hunzz.productservice.dto.response

import java.util.*

data class ProductResponse(
    val productId: UUID,
    val sellerId: Long,
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val tags: List<String>
)
