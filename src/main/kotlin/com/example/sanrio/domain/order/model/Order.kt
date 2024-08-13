package com.example.sanrio.domain.order.model

import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "code", nullable = false)
    val code: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PAID,

    @Column(name = "total_price", nullable = false)
    val totalPrice: Int,

    @Column(name = "street_address", nullable = false)
    val streetAddress: String,

    @Column(name = "detail_address", nullable = false)
    val detailAddress: ByteArray,

    @Column(name = "order_request", nullable = false)
    val orderRequest: String,

    @Column(name = "used_point", nullable = true)
    val usedPoint: Int?,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, unique = true)
    val id: Long? = null

    @Description("주문 상태 변경")
    fun updateStatus(status: OrderStatus) = status.also { this.status = it }
}