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
    private val issuer: String, // 토큰 발급자

    @Value("\${jwt.expiration-time.atk}")
    private val expirationTimeOfAtk: Int, // Access Token의 만료 시간

    @Value("\${jwt.expiration-time.rtk}")
    private val expirationTimeOfRtk: Int // Refresh Token의 만료 시간
) {
    companion object {
        private const val BEARER_PREFIX = "Bearer " // Token 식별자
    }

    private val key: SecretKey =
        Base64.getDecoder().decode(secretKey).let { Keys.hmacShaKeyFor(it) } // SecretKey를 담을 Key 객체

    @Description("Access Token 생성")
    fun getAccessToken(userId: Long, email: String, role: UserRole) =
        createToken(userId = userId, email = email, role = role, expirationTimeOfAtk)

    @Description("Refresh Token 생성")
    fun getRefreshToken(userId: Long, email: String, role: UserRole) =
        createToken(userId = userId, email = email, role = role, expirationTimeOfRtk)

    @Description("JWT 토큰을 생성")
    private fun createToken(userId: Long, email: String, role: UserRole, expirationTime: Int) =
        Jwts.builder().let {
            it.subject(userId.toString()) // 사용자 식별자값 (ID)
            it.claims(
                Jwts.claims().add(mapOf("role" to role.authority, "email" to email)).build()
            ) // 토큰에 저장할 데이터 (role, email)
            it.expiration(Date(Date().time + expirationTime)) // 토큰 만료 시간
            it.issuedAt(Date()) // 토큰 발급일
            it.issuer(issuer) // 토큰 발급자
            it.signWith(key) // 서명
            it.compact()
        }.let { jwt -> "${BEARER_PREFIX}$jwt" }

    @Description("JWT 토큰을 Cookie에 저장")
    fun addTokenToCookie(token: String, response: HttpServletResponse, type: String) {
        Cookie(
            if (type == "atk") "AccessToken" else "RefreshToken", // name
            URLEncoder.encode(token, "UTF-8") // value
        ).let {
            it.path = "/" // 모든 경로에 Cookie 적용
            it.maxAge = if (type == "atk") expirationTimeOfAtk else expirationTimeOfRtk // 쿠키 유효 시간
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
    fun getTokenFromRequest(request: HttpServletRequest, type: String): String? {
        val cookie = request.cookies?.find { it.name == if (type == "atk") "AccessToken" else "RefreshToken" }
        return cookie?.value?.let { token -> URLDecoder.decode(token, "UTF-8") }
    }
}