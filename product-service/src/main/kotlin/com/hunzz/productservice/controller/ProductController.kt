package com.hunzz.productservice.controller

import com.hunzz.productservice.dto.request.AddProductRequest
import com.hunzz.productservice.dto.response.ProductResponse
import com.hunzz.productservice.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {
    @PostMapping
    fun add(
        @RequestBody request: AddProductRequest
    ): ResponseEntity<ProductResponse> {
        val sellerId = 1L // TODO
        val response = productService.add(sellerId = sellerId, request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{productId}")
    fun get(
        @PathVariable productId: UUID
    ): ResponseEntity<ProductResponse> {
        val response = productService.get(productId = productId)

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/sellers/{sellerId}")
    fun getAllBySellerId(
        @PathVariable sellerId: Long
    ): ResponseEntity<List<ProductResponse>> {
        val response = productService.getAllBySellerId(sellerId = sellerId)

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }
}