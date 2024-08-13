package com.example.sanrio.domain.auth.service

import com.example.sanrio.domain.user.dto.request.LoginRequest
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.LoginException
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.JwtProvider
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Description
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val entityFinder: EntityFinder,
    private val jwtProvider: JwtProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.expiration-time.rtk}") private val expirationTime: Long
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
        val accessToken = jwtProvider.getAccessToken(userId = user.id!!, email = user.email, role = user.role)
        val refreshToken = jwtProvider.getRefreshToken(userId = user.id, email = user.email, role = user.role)

        jwtProvider.addTokenToCookie(token = accessToken, response = response, type = "atk")
        jwtProvider.addTokenToCookie(token = refreshToken, response = response, type = "rtk")

        // Refresh Token을 Redis에 저장
        redisTemplate.opsForValue().set(user.email, refreshToken, expirationTime, TimeUnit.MILLISECONDS)
    }

    @Description("로그아웃")
    fun logout(userId: Long, response: HttpServletResponse) {
        val user = entityFinder.findUserById(userId = userId)

        // Refesh Token 삭제
        redisTemplate.delete(user.email)

        // Cookie 삭제
        jwtProvider.deleteCookie(response = response)
    }
}