package com.hunzz.userservice.dto.response

data class UserResponse(
    val userId: Long,
    val loginId: String,
    val name: String
)