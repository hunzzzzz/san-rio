package com.example.sanrio.domain.cart.model

import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "carts")
class Cart(
    @Column(name = "total_price", nullable = false)
    var totalPrice: Int = 0,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false)
    var id: Long? = null

    @Description("전체 수량(totalPrice) 업데이트")
    fun updateTotalPrice(unitPrice: Int) {
        this.totalPrice += unitPrice
    }
}