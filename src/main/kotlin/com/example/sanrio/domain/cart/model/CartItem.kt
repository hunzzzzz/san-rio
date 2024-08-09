package com.example.sanrio.domain.cart.model

import com.example.sanrio.domain.product.model.Product
import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "cart_items")
class CartItem(
    @Column(name = "count", nullable = false)
    var count: Int,

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id", nullable = false, unique = true)
    var id: Long? = null

    @Description("수량 변경")
    fun updateCount(count: Int) = count.also { this.count = it }
}