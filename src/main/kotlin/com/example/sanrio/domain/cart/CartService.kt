package com.example.sanrio.domain.cart

import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.cart.repository.CartRepository
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val cartRepository: CartRepository
) {
}