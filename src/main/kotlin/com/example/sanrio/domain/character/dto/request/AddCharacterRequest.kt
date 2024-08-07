package com.example.sanrio.domain.character.dto.request

import com.example.sanrio.domain.character.model.Character
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Range

data class AddCharacterRequest(
    @field:NotBlank(message = "캐릭터 이름은 필수 입력 항목입니다.")
    val name: String,

    @field:NotNull(message = "데뷔 연도는 필수 입력 항목입니다.")
    @field:Range(
        message = "올바르지 않은 연도 형식입니다.",
        min = 1900
    )
    val debutYear: Int?
) {
    fun to() = Character(
        name = this.name,
        debutYear = this.debutYear!!
    )
}
