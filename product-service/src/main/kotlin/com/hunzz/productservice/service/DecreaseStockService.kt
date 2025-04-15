package com.hunzz.productservice.service

import com.hunzz.productservice.dto.response.ProductResponse
import com.hunzz.productservice.model.cassandra.Product
import com.hunzz.productservice.repository.ProductRepository
import com.hunzz.productservice.utility.exception.custom.ProductNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class DecreaseStockService(
    private val productRepository: ProductRepository
) {
    private fun getProduct(productId: UUID): Product {
        return productRepository.findByIdOrNull(productId)
            ?: throw ProductNotFoundException("Product not found")
    }

    @Transactional
    fun decreaseStock(productId: UUID, count: Int): ProductResponse {
        val product = getProduct(productId = productId)

        product.decreaseStock(count = count)

        return product.toResponse()
    }
}