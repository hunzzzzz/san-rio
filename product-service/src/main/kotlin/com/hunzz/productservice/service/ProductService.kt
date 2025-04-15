package com.hunzz.productservice.service

import com.hunzz.productservice.dto.request.AddProductRequest
import com.hunzz.productservice.dto.response.ProductResponse
import com.hunzz.productservice.model.cassandra.Product
import com.hunzz.productservice.model.mysql.ProductSeller
import com.hunzz.productservice.repository.ProductRepository
import com.hunzz.productservice.repository.ProductSellerRepository
import com.hunzz.productservice.utility.exception.custom.ProductNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productSellerRepository: ProductSellerRepository
) {
    private fun getProduct(productId: UUID): Product {
        return productRepository.findByIdOrNull(productId)
            ?: throw ProductNotFoundException("Product not found")
    }

    @Transactional
    fun add(sellerId: Long, request: AddProductRequest): ProductResponse {
        val productSeller = productSellerRepository.save(
            ProductSeller(sellerId = sellerId)
        )

        val product = productRepository.save(
            Product(
                id = productSeller.id,
                sellerId = sellerId,
                name = request.name,
                description = request.description,
                price = request.price,
                stock = request.stock,
                tags = request.tags
            )
        )

        return product.toResponse()
    }

    fun get(productId: UUID): ProductResponse {
        val product = getProduct(productId = productId)

        return product.toResponse()
    }

    fun getAllBySellerId(sellerId: Long): List<ProductResponse> {
        val productIds = productSellerRepository.findAllBySellerId(sellerId = sellerId)

        return productRepository.findAllById(productIds).map { it.toResponse() }
    }
}