package com.example.sanrio.domain.cart.controller

import com.example.sanrio.domain.cart.CartService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products/{productId}")
class CartController(
    private val cartService: CartService
) {
}