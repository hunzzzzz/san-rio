package com.example.sanrio.domain.product.repository

import com.example.sanrio.domain.product.dto.response.ProductResponse
import com.example.sanrio.domain.product.dto.response.ProductSortCondition
import com.example.sanrio.domain.product.model.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface ProductRepositoryCustom {
    fun getProducts(pageable: Pageable, status: ProductStatus?, sort: ProductSortCondition?): Page<ProductResponse>
}