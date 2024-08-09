package com.example.sanrio.domain.cart.dto.response

import com.example.sanrio.domain.cart.model.Cart

data class CartResponse(
    val userId: Long,
    val totalPrice: Int,
    val cartItems: List<CartItemResponse>
) {
    companion object {
        fun from(cart: Cart, cartItems: List<CartItemResponse>) = CartResponse(
            userId = cart.user.id!!,
            totalPrice = cart.totalPrice,
            cartItems = cartItems
        )
    }
}