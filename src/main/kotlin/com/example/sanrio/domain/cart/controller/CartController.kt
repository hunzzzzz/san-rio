package com.example.sanrio.domain.cart.controller

import com.example.sanrio.domain.cart.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products/{productId}")
class CartController(
    private val cartService: CartService
) {
    @PostMapping
    fun addItems(
        @RequestParam userId: Long, // TODO : userID는 추후 UserPrincipal에서 추출한다.
        @RequestParam count: Int,
        @PathVariable productId: Long
    ) =
        cartService.addItems(userId = userId, productId = productId, count = count).let { ResponseEntity.ok().body(it) }
}