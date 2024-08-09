package com.example.sanrio.domain.cart.repository

import com.example.sanrio.domain.cart.dto.response.CartItemResponse
import com.example.sanrio.domain.cart.model.Cart
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepositoryCustom {
    fun getCartItems(cart: Cart): List<CartItemResponse>
}