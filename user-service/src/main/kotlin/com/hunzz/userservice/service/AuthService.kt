package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.LoginRequest
import com.hunzz.userservice.dto.response.TokenResponse
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import com.hunzz.userservice.utility.auth.JwtProvider
import com.hunzz.userservice.utility.exception.custom.LoginException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository
) {
    private fun getUser(loginId: String): User {
        return userRepository.findByLoginId(loginId = loginId)
            ?: throw LoginException("Login Error")
    }

    private fun createToken(user: User): TokenResponse {
        return TokenResponse(
            atk = jwtProvider.createAtk(user = user),
            rtk = jwtProvider.createRtk(user = user)
        )
    }

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val user = getUser(loginId = request.loginId)

        if (user.password == request.password)
            return createToken(user = user)
        else throw LoginException("Login Error")
    }
}