package com.example.sanrio.domain.order.controller

import com.example.sanrio.domain.order.model.OrderPeriod
import com.example.sanrio.domain.order.service.OrderService
import jdk.jfr.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {
    @Description("주문 완료")
    @PostMapping
    fun makeOrder(
        @RequestParam userId: Long // TODO : userID는 추후 UserPrincipal에서 추출한다.
    ) = orderService.makeOrder(userId = userId)
        .let { ResponseEntity.ok().body(it) }

    @Description("주문 내역 조회")
    @GetMapping
    fun getOrders(
        @RequestParam userId: Long, // TODO : userID는 추후 UserPrincipal에서 추출한다.
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) period: OrderPeriod?
    ) = orderService.getOrders(userId = userId, cursorId = cursorId, period = period)
        .let { ResponseEntity.ok().body(it) }

}