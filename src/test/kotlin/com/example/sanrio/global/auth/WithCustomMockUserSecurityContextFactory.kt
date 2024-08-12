package com.example.sanrio.global.auth

import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.jwt.JwtAuthenticationToken
import com.example.sanrio.global.jwt.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource

class WithAccountSecurityContextFactory(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val httpServletRequest: HttpServletRequest
) : WithSecurityContextFactory<WithCustomMockUser> {

    override fun createSecurityContext(annotation: WithCustomMockUser): SecurityContext {
        val user = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234!",
            password2 = "Test1234!",
            name = "테스트 계정"
        ).to(passwordEncoder = passwordEncoder)
            .let { userRepository.save(it) }

        val principal = UserPrincipal(
            id = user.id!!,
            email = user.email,
            roles = setOf(user.role.authority)
        )

        val authentication = JwtAuthenticationToken(
            principal = principal,
            details = WebAuthenticationDetailsSource().buildDetails(httpServletRequest)
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication

        return context
    }
}