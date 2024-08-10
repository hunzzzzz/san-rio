package com.example.sanrio.domain.order.controller

import com.example.sanrio.domain.order.service.OrderService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {
}