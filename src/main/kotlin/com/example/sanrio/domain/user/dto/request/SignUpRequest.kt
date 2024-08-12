package com.example.sanrio.domain.user.dto.request

import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.utility.NicknameGenerator.generateNickname
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.context.annotation.Description
import org.springframework.security.crypto.password.PasswordEncoder

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @field:Email(message = "올바르지 않은 이메일 형식입니다.")
    val email: String?,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @field:Pattern(
        message = "올바르지 않은 비밀번호 형식입니다. (8~16자의 알파벳 대소문자, 숫자, 특수문자로 구성)",
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,16}\$"
    )
    val password: String?,

    @field:NotBlank(message = "한 번 더 비밀번호를 입력해주세요.")
    val password2: String?,

    @field:NotBlank(message = "이름은 필수 입력 항목입니다.")
    val name: String?,

    @field:NotBlank(message = "휴대폰번호는 필수 입력 항목입니다.")
    @field:Pattern(
        message = "올바르지 않은 휴대폰번호 형식입니다.",
        regexp = "^(01[016789])-?[0-9]{3,4}-?[0-9]{4}$"
    )
    val phone: String?
) {
    @Description("DTO -> User 엔티티로 변환하는 메서드")
    fun to(passwordEncoder: PasswordEncoder) = User(
        email = this.email!!,
        password = passwordEncoder.encode(this.password),
        name = this.name!!,
        nickname = generateNickname(),
        phone = this.phone!!
    )
}
