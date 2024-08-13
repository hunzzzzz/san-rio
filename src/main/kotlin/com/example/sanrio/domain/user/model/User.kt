package com.example.sanrio.domain.user.model

import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "users")
class User(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: UserStatus = UserStatus.NEW,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole = UserRole.USER,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "nickname", nullable = false)
    val nickname: String,

    @Column(name = "phone", nullable = false)
    val phone: String,

    @Column(name = "point", nullable = false)
    var point: Int = 0
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, unique = true)
    val id: Long? = null

    @Description("비밀번호 변경")
    fun updatePassword(newPassword: String) = newPassword.also { this.password = it }

    @Description("주문이 배송완료가 되면, 주문 금액의 1%가 유저의 포인트로 적립됨")
    fun updatePoint(point: Int) {
        this.point += point
    }
}