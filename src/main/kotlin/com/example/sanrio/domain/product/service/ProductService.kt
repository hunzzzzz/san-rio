package com.example.sanrio.domain.product.service

import com.example.sanrio.domain.product.dto.request.AddProductRequest
import com.example.sanrio.domain.product.dto.response.ProductDetailResponse
import com.example.sanrio.domain.product.dto.response.ProductPageResponse
import com.example.sanrio.domain.product.dto.response.ProductSortCondition
import com.example.sanrio.domain.product.model.CharacterName
import com.example.sanrio.domain.product.model.ProductStatus
import com.example.sanrio.domain.product.repository.ProductRepository
import com.example.sanrio.global.exception.case.ModelNotFoundException
import org.springframework.context.annotation.Description
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    @Description("productId로 상품 엔티티를 가져오는 메서드")
    private fun findProductById(productId: Long) =
        productRepository.findByIdOrNull(productId) ?: throw ModelNotFoundException("상품")

    @Description("상품 추가")
    fun addProduct(characterName: CharacterName, request: AddProductRequest) =
        request.to(characterName = characterName) // DTO -> 엔티티
            .let { productRepository.save(it) } // 저장
            .let { } // 리턴값 X

    @Description("상품 세부 정보")
    fun getProduct(productId: Long) =
        findProductById(productId = productId)
            .let { ProductDetailResponse.from(product = it) }

    @Description("상품 목록")
    fun getProducts(page: Int, status: ProductStatus?, characterName: CharacterName?, sort: ProductSortCondition?) =
        PageRequest.of(page - 1, PRODUCT_PAGE_SIZE)
            .let {
                productRepository.getProducts(
                    pageable = it,
                    status = status,
                    characterName = characterName,
                    sort = sort
                )
            }.let { ProductPageResponse.from(page = it) }

    companion object {
        private const val PRODUCT_PAGE_SIZE = 6
    }
}