package com.example.sanrio.domain.order.model

import com.example.sanrio.domain.product.model.Product
import jakarta.persistence.*

@Entity
@Table(name = "order_items")
class OrderItem(
    @Column(name = "count", nullable = false)
    val count: Int,

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false, unique = true)
    val id: Long? = null
}