package com.example.sanrio.domain.order.controller

import com.example.sanrio.domain.order.model.OrderPeriod
import com.example.sanrio.domain.order.service.OrderService
import com.example.sanrio.global.jwt.UserPrincipal
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {
    @Description("주문 완료")
    @PostMapping
    fun makeOrder(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ) = orderService.makeOrder(userId = userPrincipal.id)
        .let { ResponseEntity.ok().body(it) }

    @Description("주문 내역 조회")
    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) period: OrderPeriod?
    ) = orderService.getOrders(userId = userPrincipal.id, cursorId = cursorId, period = period)
        .let { ResponseEntity.ok().body(it) }

    @Description("주문 취소 신청")
    @GetMapping("/cancel/{orderId}")
    fun cancelOrder(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable orderId: Long
    ) = orderService.cancelOrder(userId = userPrincipal.id, orderId = orderId)
        .let { ResponseEntity.ok().body(it) }

    @Description("주문 취소 신청 승인")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/cancel/{orderId}")
    fun acceptCancelOrder(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable orderId: Long,
    ) = orderService.acceptCancelOrder(userId = userPrincipal.id, orderId = orderId)
        .let { ResponseEntity.ok().body(it) }
}