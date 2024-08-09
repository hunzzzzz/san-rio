package com.example.sanrio.domain.cart.repository

import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.product.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartAndProduct(cart: Cart, product: Product): CartItem?
}