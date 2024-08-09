package com.example.sanrio.domain.user.model

import jakarta.persistence.*

@Entity
@Table(name = "mails")
class Mail(
    @Column(name = "subject", nullable = false)
    val subject: String,

    @Column(name = "text", nullable = false)
    val text: String,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    val id: Long? = null
}