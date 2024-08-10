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

    @Description("전체 가격(totalPrice) 업데이트")
    fun updateTotalPrice(unitPrice: Int) {
        this.totalPrice += unitPrice
    }

    @Description("장바구니 상품 주문 완료 시, 다시 전체 가격을 0으로 리셋")
    fun resetTotalPrice() {
        this.totalPrice = 0
    }
}