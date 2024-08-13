package com.example.sanrio.global.jwt

import com.example.sanrio.global.exception.case.JwtTokenException
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Description
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val entityFinder: EntityFinder
) : OncePerRequestFilter() {

    @Description("필터를 적용하지 않을 URI 목록")
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludeURIs = listOf("/signup", "/login", "/h2-console", "/favicon.ico")
        val path = request.requestURI
        return excludeURIs.any { uri -> path.startsWith(uri) }
    }

    @Description("필터 세부 내용")
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        // 토큰 추출
        var accessTokenValue = jwtProvider.getTokenFromRequest(request = request, type = "atk")
        val refreshTokenValue = jwtProvider.getTokenFromRequest(request = request, type = "rtk")
            ?: throw JwtTokenException("유저 정보가 만료되었습니다. 다시 로그인해주세요.")

        // AccessToken이 만료되면, RefreshToken를 활용해 AccessToken을 새로 발급하고 Cookie에 넣어준다.
        if (accessTokenValue == null) {
            // substring 후 RefreshToken을 활용해 User 정보 추출
            val refreshToken = jwtProvider.substringToken(token = refreshTokenValue)
            val userId = jwtProvider.getUserInfoFromToken(token = refreshToken).subject.toLong()
            val user = entityFinder.findUserById(userId = userId)

            // 새로운 AccessToken을 발급하고 Cookie에 담는다.
            val newAccessToken = jwtProvider.getAccessToken(userId = userId, email = user.email, role = user.role)
            jwtProvider.addTokenToCookie(token = newAccessToken, response = response, type = "atk")

            // 아래 과정을 진행하기 위해 다시 accessTokenValue에 값을 넣어준다.
            accessTokenValue = jwtProvider.getTokenFromRequest(request = request, type = "atk")
        }

        // substring
        val accessToken = jwtProvider.substringToken(accessTokenValue!!)
        val refreshToken = jwtProvider.substringToken(refreshTokenValue)

        // 토큰 검증
        jwtProvider.validateToken(token = accessToken).onFailure { throw JwtTokenException() }
        jwtProvider.validateToken(token = refreshToken).onFailure { throw JwtTokenException() }

        // 토큰에서 유저 정보 추출
        val payload = jwtProvider.getUserInfoFromToken(token = accessToken)

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