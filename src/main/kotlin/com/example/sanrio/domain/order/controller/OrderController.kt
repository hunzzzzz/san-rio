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
    @PostMapping("")
    fun doOrder(
        @RequestParam userId: Long
    ) = orderService.makeOrder(userId = userId)
        .let { ResponseEntity.ok().body(it) }
}