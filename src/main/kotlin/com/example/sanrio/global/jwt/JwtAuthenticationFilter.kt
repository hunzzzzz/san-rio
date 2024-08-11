package com.example.sanrio.global.jwt

import com.example.sanrio.global.exception.case.JwtTokenException
import com.example.sanrio.global.utility.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        // 토큰 추출
        val tokenValue = jwtProvider.getTokenFromRequest(request)

        if (tokenValue == null) chain.doFilter(request, response)
        else {
            // substring
            val token = jwtProvider.substringToken(tokenValue)

            // 토큰 검증
            jwtProvider.validateToken(token = token).onFailure { throw JwtTokenException() }

            // 토큰에서 유저 정보 추출
            val payload = jwtProvider.getUserInfoFromToken(token = token)

            // 추출한 유저 정보를 기반으로 인증 정보 설정
            val principal = UserPrincipal(
                id = payload.subject.toLong(),
                email = payload.get("email", String::class.java),
                roles = setOf(payload.get("role", String::class.java))
            )
            val authentication = JwtAuthenticationToken(
                principal = principal,
                details = WebAuthenticationDetailsSource().buildDetails(request)
            )
            SecurityContextHolder.getContext().authentication = authentication

            // 다음 Filter 로 이동
            chain.doFilter(request, response)
        }
    }
}