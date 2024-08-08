package com.example.sanrio.domain.product.dto.request

import com.example.sanrio.domain.product.model.CharacterName
import com.example.sanrio.domain.product.model.Product
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Range

data class AddProductRequest(
    @field:NotBlank(message = "상품 이름은 필수 입력 항목입니다.")
    val name: String,

    @field:NotBlank(message = "상품 설명은 필수 입력 항목입니다.")
    val detail: String,

    @field:NotNull(message = "가격은 필수 입력 항목입니다.")
    @field:Range(
        message = "올바르지 않은 수량 형식입니다.",
        min = 100
    )
    val price: Int?,

    @field:NotNull(message = "재고 수량은 필수 입력 항목입니다.")
    @field:Range(
        message = "올바르지 않은 수량 형식입니다.",
        min = 1
    )
    val stock: Int?
) {
    fun to(characterName: CharacterName) = Product(
        characterName = characterName,
        name = this.name,
        detail = this.detail,
        price = this.price!!,
        stock = this.stock!!
    )
}