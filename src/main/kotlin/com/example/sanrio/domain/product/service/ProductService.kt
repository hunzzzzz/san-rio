package com.example.sanrio.domain.product.service

import com.example.sanrio.domain.product.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
}