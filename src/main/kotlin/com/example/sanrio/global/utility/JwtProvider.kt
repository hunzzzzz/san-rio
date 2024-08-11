package com.example.sanrio.global.utility

import com.example.sanrio.domain.user.model.UserRole
import com.example.sanrio.global.exception.case.JwtTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Component
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import javax.crypto.SecretKey


@Component
class JwtProvider(
    @Value("\${jwt.secret.key}")
    private val secretKey: String, // Base64로 인코딩된 SecretKey

    @Value("\${jwt.issuer}")
    private val issuer: String // 토큰 발급자
) {
    companion object {
        private const val COOKIE_NAME = "Authorization" // 쿠키의 name값
        private const val BEARER_PREFIX = "Bearer " // Token 식별자
        private const val EXPIRATION_TIME = 1000L * 60 * 60 // Token 만료 시간 (1시간)
    }

    private val key: SecretKey =
        Base64.getDecoder().decode(secretKey).let { Keys.hmacShaKeyFor(it) } // SecretKey를 담을 Key 객체

    @Description("JWT 토큰을 생성")
    fun createToken(userId: Long, email: String, role: UserRole) =
        Jwts.builder().let {
            it.subject(userId.toString()) // 사용자 식별자값 (ID)
            it.claims(
                Jwts.claims().add(mapOf("role" to role.authority, "email" to email)).build()
            ) // 토큰에 저장할 데이터 (role, email)
            it.expiration(Date(Date().time + EXPIRATION_TIME)) // 토큰 만료 시간
            it.issuedAt(Date()) // 토큰 발급일
            it.issuer(issuer) // 토큰 발급자
            it.signWith(key) // 서명
            it.compact()
        }.let { jwt -> "${BEARER_PREFIX}$jwt" }

    @Description("JWT 토큰을 Cookie에 저장")
    fun addTokenToCookie(token: String, response: HttpServletResponse) {
        Cookie(
            COOKIE_NAME, // name
            URLEncoder.encode(token, "UTF-8") // value
        ).let {
            it.path = "/" // 모든 경로에 Cookie 적용
            it.maxAge = 60 * 60 // 쿠키 유효 시간 (1시간)
            response.addCookie(it) // HTTP Response에 Cookie 추가
        }
    }

    @Description("JWT 토큰을 substring")
    fun substringToken(token: String) =
        if (token.isNotBlank() && token.startsWith(BEARER_PREFIX)) token.substring(7)
        else throw JwtTokenException()

    @Description("JWT 토큰 검증")
    fun validateToken(token: String) =
        kotlin.runCatching { Jwts.parser().verifyWith(key).build().parseSignedClaims(token) }

    @Description("JWT 토큰에서 사용자 정보 추출")
    fun getUserInfoFromToken(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    @Description("Cookie에서 JWT 토큰 추출")
    fun getTokenFromRequest(request: HttpServletRequest): String? {
        val cookie = request.cookies?.find { it.name == COOKIE_NAME }
        return cookie?.value?.let { token -> URLDecoder.decode(token, "UTF-8") }
    }
}