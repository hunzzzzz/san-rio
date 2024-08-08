package com.example.sanrio.domain.product.repository

import com.example.sanrio.domain.product.dto.response.ProductResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface ProductRepositoryCustom {
    fun getProducts(pageable: Pageable): Page<ProductResponse>
}