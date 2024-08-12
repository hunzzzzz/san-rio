package com.example.sanrio.domain.user.dto.response

import com.example.sanrio.domain.user.model.User
import java.time.LocalDateTime

class UserResponse(
    val userId: Long,
    val email: String,
    val name: String,
    val nickname: String,
    val phone: String,
    val point: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User) = UserResponse(
            userId = user.id!!,
            email = user.email,
            name = user.name,
            nickname = user.nickname,
            phone = user.phone,
            point = user.point,
            createdAt = user.createdAt
        )
    }
}