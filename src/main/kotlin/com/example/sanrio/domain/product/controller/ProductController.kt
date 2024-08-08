package com.example.sanrio.domain.product.controller

import com.example.sanrio.domain.product.dto.request.AddProductRequest
import com.example.sanrio.domain.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {
    @Description("상품 추가")
    @PostMapping
    fun addProduct(
        @Valid @RequestBody request: AddProductRequest
    ) = ResponseEntity.ok().body(productService.addProduct(request = request))
}