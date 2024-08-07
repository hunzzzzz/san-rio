package com.example.sanrio.domain.user.service

import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.DuplicatedValueException
import com.example.sanrio.global.exception.case.InvalidValueException
import org.springframework.context.annotation.Description
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Description("회원가입 시 입력한 이메일에 대한 중복 체크")
    private fun validateEmailDuplication(email: String) =
        if (userRepository.existsByEmail(email)) throw DuplicatedValueException("이메일") else Unit

    @Description("회원가입 시 입력한 두 비밀번호가 일치하는지 체크")
    private fun validateTwoPasswords(password1: String, password2: String) =
        if (password1 != password2) throw InvalidValueException("비밀번호") else Unit

    @Description("회원가입")
    fun signup(request: SignUpRequest) =
        validateEmailDuplication(email = request.email) // 이메일 검증
            .let { validateTwoPasswords(password1 = request.password, password2 = request.password2) } // 패스워드 검증
            .let { request.to(passwordEncoder = passwordEncoder) } // DTO -> 엔티티
            .let { userRepository.save(it) } // 저장
            .let { } // 리턴값 X
}