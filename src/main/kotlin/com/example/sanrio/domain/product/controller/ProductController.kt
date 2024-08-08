package com.example.sanrio.domain.product.controller

import com.example.sanrio.domain.product.dto.request.AddProductRequest
import com.example.sanrio.domain.product.dto.response.ProductSortCondition
import com.example.sanrio.domain.product.model.CharacterName
import com.example.sanrio.domain.product.model.ProductStatus
import com.example.sanrio.domain.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {
    @Description("상품 추가")
    @PostMapping
    fun addProduct(
        @RequestParam characterName: CharacterName,
        @Valid @RequestBody request: AddProductRequest
    ) = ResponseEntity.ok().body(productService.addProduct(characterName = characterName, request = request))

    @Description("상품 세부 정보")
    @GetMapping("/{productId}")
    fun getProduct(
        @PathVariable productId: Long
    ) = ResponseEntity.ok().body(productService.getProduct(productId = productId))

    @Description("상품 목록")
    @GetMapping
    fun getProducts(
        @RequestParam(required = false) status: ProductStatus?, // 필터 조건 (판매 중, 품절)
        @RequestParam(required = false) characterName: CharacterName?, // 필터 조건 (캐릭터 이름)
        @RequestParam(required = false) sort: ProductSortCondition?, // 정렬 조건
        @RequestParam(defaultValue = "1") page: Int
    ) =
        productService.getProducts(page = page, status = status, characterName = characterName, sort = sort)
            .let { ResponseEntity.ok().body(it) }
}