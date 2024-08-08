package com.example.sanrio.domain.user.model

import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*

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
    val password: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "nickname", nullable = false)
    val nickname: String,

    @OneToOne(mappedBy = "user")
    val address: Address? = null
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, unique = true)
    val id: Long? = null


}