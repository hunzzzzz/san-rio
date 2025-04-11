package com.hunzz.userservice.entity

import com.hunzz.userservice.dto.response.UserResponse
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, unique = true)
    var id: Long? = null,

    @Column(name = "login_id", nullable = false, unique = true)
    val loginId: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "name", nullable = false)
    var name: String
) {
    fun update(name: String, password: String) {
        this.name = name
        this.password = password
    }

    fun toResponse(): UserResponse {
        return UserResponse(
            userId = this.id!!,
            loginId = this.loginId,
            name = this.name
        )
    }
}