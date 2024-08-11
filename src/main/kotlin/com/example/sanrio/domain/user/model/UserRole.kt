package com.example.sanrio.domain.user.model

enum class UserRole(val authority: String) {
    USER(Authority.USER),
    ADMIN(Authority.ADMIN);

    object Authority {
        const val USER: String = "ROLE_USER"
        const val ADMIN: String = "ROLE_ADMIN"
    }
}