package com.example.sanrio.domain.order.model

import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "code", nullable = false)
    val code: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: OrderStatus = OrderStatus.PAID,

    @Column(name = "total_price", nullable = false)
    val totalPrice: Int = 0,

    @Column(name = "street_address", nullable = false)
    val streetAddress: String,

    @Column(name = "detail_address", nullable = false)
    val detailAddress: ByteArray,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, unique = true)
    val id: Long? = null
}