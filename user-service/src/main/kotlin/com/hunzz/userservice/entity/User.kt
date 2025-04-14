package com.hunzz.userservice.entity

import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.entity.enums.UserRole
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, unique = true)
    var id: Long? = null,

    @Enumerated
    @Column(name = "role", nullable = false)
    val role: UserRole = UserRole.USER,

    @Column(name = "login_id", nullable = false, unique = true)
    val loginId: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "name", nullable = false)
    val name: String
) {
    fun updatePassword(newPassword: String) {
        this.password = newPassword
    }

    fun toResponse(): UserResponse {
        return UserResponse(
            userId = this.id!!,
            loginId = this.loginId,
            name = this.name
        )
    }
}