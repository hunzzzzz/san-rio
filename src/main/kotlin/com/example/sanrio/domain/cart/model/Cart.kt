package com.example.sanrio.domain.cart.model

import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "carts")
class Cart(
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false)
    var id: Long? = null
}