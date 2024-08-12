package com.example.sanrio.global.auth

import com.example.sanrio.global.jwt.UserPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticationHelper {
    fun getCurrentUser() =
        SecurityContextHolder.getContext().authentication
            .let { authentication -> authentication.principal as UserPrincipal }
}