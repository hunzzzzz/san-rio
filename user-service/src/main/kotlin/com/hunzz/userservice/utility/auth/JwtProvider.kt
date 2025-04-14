package com.hunzz.userservice.utility.auth

import com.hunzz.userservice.entity.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret.key}")
    private val secretKey: String
) {
    companion object {
        const val ISSUER = "hunzz"
        const val EXP_ATK_MILLIS = 1000 * 60 * 60 // 1시간
        const val EXP_RTK_MILLIS = 1000 * 60 * 60 * 24 // 1일
    }

    private val key: SecretKey =
        Base64.getDecoder().decode(secretKey).let { Keys.hmacShaKeyFor(it) }

    private fun createToken(user: User, exp: Int): String {
        val now = Date()
        val claims = Jwts.claims().add(
            mapOf("loginId" to user.loginId, "role" to user.role)
        ).build()

        return Jwts.builder().apply {
            subject(user.id.toString())
            claims(claims)
            expiration(Date(now.time + exp))
            issuedAt(now)
            issuer(ISSUER)
            signWith(key)
        }.compact().let { jwt -> "Bearer $jwt" }
    }

    fun createAtk(user: User): String {
        return createToken(user = user, exp = EXP_ATK_MILLIS)
    }

    fun createRtk(user: User): String {
        return createToken(user = user, exp = EXP_RTK_MILLIS)
    }
}