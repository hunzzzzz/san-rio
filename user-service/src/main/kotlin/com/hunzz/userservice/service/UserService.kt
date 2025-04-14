package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.UpdatePasswordRequest
import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import com.hunzz.userservice.utility.exception.custom.InvalidUserInfoException
import com.hunzz.userservice.utility.exception.custom.UserNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    private fun getUser(userId: Long): User {
        return userRepository.findByIdOrNull(id = userId)
            ?: throw UserNotFoundException("User not found")
    }

    fun get(userId: Long): UserResponse {
        val user = getUser(userId = userId)

        return user.toResponse()
    }

    @Transactional
    fun updatePassword(userId: Long, request: UpdatePasswordRequest): UserResponse {
        val user = getUser(userId = userId)

        if (user.password == request.newPassword)
            throw InvalidUserInfoException("Cannot use same password")

        user.updatePassword(newPassword = request.newPassword)

        return user.toResponse()
    }
}