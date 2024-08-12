package com.example.sanrio.domain.address.dto.request

import com.example.sanrio.domain.address.model.Address
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.utility.Encryptor
import jakarta.validation.constraints.NotBlank

data class AddressRequest(
    @field:NotBlank(message = "우편번호를 입력해주세요.")
    val zipCode: String?,

    @field:NotBlank(message = "도로명 주소를 입력해주세요.")
    val streetAddress: String?,

    @field:NotBlank(message = "세부 주소를 입력해주세요.")
    val detailAddress: String?
) {
    fun to(user: User, encryptor: Encryptor, isFirstAddress: Boolean = false) = Address(
        zipCode = this.zipCode!!,
        streetAddress = this.streetAddress!!,
        detailAddress = encryptor.encrypt(this.detailAddress!!),
        user = user,
        default = isFirstAddress
    )
}
