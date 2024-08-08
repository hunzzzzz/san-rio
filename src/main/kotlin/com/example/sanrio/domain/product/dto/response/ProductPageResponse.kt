package com.example.sanrio.domain.product.dto.response

import org.springframework.data.domain.Page

data class ProductPageResponse(
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val content: List<ProductResponse>
) {
    companion object {
        fun from(page: Page<ProductResponse>) = ProductPageResponse(
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            size = page.size,
            content = page.content
        )
    }
}