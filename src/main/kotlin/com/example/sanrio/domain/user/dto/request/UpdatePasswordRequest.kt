package com.example.sanrio.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UpdatePasswordRequest(
    @field:NotBlank(message = "기존 비밀번호를 입력해주세요.")
    val currentPassword: String?,

    @field:NotBlank(message = "새로운 비밀번호를 입력해주세요.")
    @field:Pattern(
        message = "올바르지 않은 비밀번호 형식입니다. (8~16자의 알파벳 대소문자, 숫자, 특수문자로 구성)",
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,16}\$"
    )
    val newPassword: String?,

    @field:NotBlank(message = "한 번 더 비밀번호를 입력해주세요.")
    val newPassword2: String?
)
