package com.example.sanrio.domain.product.repository

import com.example.sanrio.domain.product.dto.response.ProductResponse
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
): ProductRepositoryCustom {
    override fun getProducts(pageable: Pageable): Page<ProductResponse> {
        TODO("Not yet implemented")
    }
}