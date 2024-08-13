package com.example.sanrio.domain.order.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "recalls")
class Recall(
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    val reason: RecallReason,

    @Column(name = "detail", nullable = false)
    val detail: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recall_id", nullable = false, unique = true)
    val id: Long? = null
}