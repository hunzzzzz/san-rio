package com.hunzz.productservice.dto.request

data class AddProductRequest(
    val name: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val tags: List<String>
)
