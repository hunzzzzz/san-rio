package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.LoginRequest
import com.hunzz.userservice.dto.request.SignupRequest
import com.hunzz.userservice.dto.request.UpdateUserRequest
import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private fun getUser(userId: Long): User {
        return userRepository.findByIdOrNull(id = userId)
            ?: throw IllegalStateException("User not found")
    }

    private fun getUser(loginId: String): User {
        return userRepository.findByLoginId(loginId = loginId)
            ?: throw IllegalStateException("User not found")
    }

    @Transactional
    fun signup(request: SignupRequest): UserResponse {
        val user = userRepository.save(
            User(
                loginId = request.loginId,
                password = request.password,
                name = request.name
            )
        )

        return user.toResponse()
    }

    @Transactional
    fun update(userId: Long, request: UpdateUserRequest): UserResponse {
        val user = getUser(userId = userId)

        user.update(name = request.name, password = request.password)

        return user.toResponse()
    }

    @Transactional
    fun login(request: LoginRequest): UserResponse {
        val user = getUser(loginId = request.loginId)

        if (user.password == request.password)
            return user.toResponse()
        else throw IllegalStateException("Login Error")
    }
}