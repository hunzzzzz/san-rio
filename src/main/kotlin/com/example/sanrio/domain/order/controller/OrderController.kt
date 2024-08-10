package com.example.sanrio.domain.order.controller

import com.example.sanrio.domain.order.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    fun makeOrder(
        @RequestParam userId: Long // TODO : userID는 추후 UserPrincipal에서 추출한다.
    ) = orderService.makeOrder(userId = userId)
        .let { ResponseEntity.ok().body(it) }
}