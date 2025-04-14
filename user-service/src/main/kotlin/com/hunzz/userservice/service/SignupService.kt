package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.SignupRequest
import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import com.hunzz.userservice.utility.exception.custom.InvalidUserInfoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun signup(request: SignupRequest): UserResponse {
        if (userRepository.existsByLoginId(request.loginId))
            throw InvalidUserInfoException("loginId already exists")

        val user = userRepository.save(
            User(
                loginId = request.loginId,
                password = request.password,
                name = request.name
            )
        )

        return user.toResponse()
    }
}