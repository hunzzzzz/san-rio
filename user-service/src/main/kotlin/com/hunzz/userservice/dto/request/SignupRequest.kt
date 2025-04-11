package com.hunzz.userservice.dto.request

data class SignupRequest(
    val loginId: String,
    val password: String,
    val name: String
)
