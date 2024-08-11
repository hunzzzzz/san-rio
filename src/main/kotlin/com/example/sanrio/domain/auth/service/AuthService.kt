package com.example.sanrio.domain.auth.service

import com.example.sanrio.domain.user.dto.request.LoginRequest
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.LoginException
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.JwtProvider
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Description
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val entityFinder: EntityFinder,
    private val jwtProvider: JwtProvider
) {
    @Description("로그인 시 이메일 존재 여부 체크")
    private fun checkEmail(email: String) =
        check(userRepository.existsByEmail(email = email)) { throw LoginException() }

    @Description("로그인 시 패스워드 일치 여부 체크")
    private fun checkPassword(userPassword: String, inputPassword: String) =
        check(passwordEncoder.matches(inputPassword, userPassword)) { throw LoginException() }

    @Description("로그인")
    fun login(request: LoginRequest, response: HttpServletResponse) {
        checkEmail(email = request.email)

        val user = entityFinder.findUserByEmail(email = request.email)
        checkPassword(
            userPassword = user.password,
            inputPassword = request.password
        )

        // JWT 토큰 생성 후 Cookie에 저장
        val token = jwtProvider.createToken(userId = user.id!!, email = user.email, role = user.role)
        jwtProvider.addTokenToCookie(token = token, response = response)
    }
}