package com.example.sanrio.domain.product.service

import com.example.sanrio.domain.character.repository.CharacterRepository
import com.example.sanrio.domain.product.dto.request.AddProductRequest
import com.example.sanrio.domain.product.repository.ProductRepository
import com.example.sanrio.global.exception.case.ModelNotFoundException
import org.springframework.context.annotation.Description
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val characterRepository: CharacterRepository
) {
    @Description("characterId로 캐릭터 엔티티를 가져오는 메서드")
    private fun findCharacterById(characterId: Long) =
        characterRepository.findByIdOrNull(characterId) ?: throw ModelNotFoundException("캐릭터")

    @Description("상품 추가")
    fun addProduct(request: AddProductRequest) =
        findCharacterById(characterId = request.characterId!!) // Character 가져오기
            .let { request.to(character = it) } // DTO -> 엔티티
            .let { productRepository.save(it) } // 저장
            .let { } // 리턴값 X
}