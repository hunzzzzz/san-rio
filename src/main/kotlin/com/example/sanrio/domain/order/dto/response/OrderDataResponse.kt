package com.example.sanrio.domain.order.dto.response

import com.example.sanrio.domain.order.model.OrderStatus
import java.time.LocalDateTime

data class OrderDataResponse(
    val orderId: Long,
    val code: String,
    val status: OrderStatus,
    val userName: String,
    val userAddress: String?,
    val totalPrice: Int,
    val createdAt: LocalDateTime,
    val orderItems: List<OrderItemResponse>
) {
    companion object {
        fun from(orderResponse: OrderResponse, orderItems: List<OrderItemResponse>) = OrderDataResponse(
            orderId = orderResponse.orderId,
            code = orderResponse.code,
            status = orderResponse.status,
            userName = orderResponse.userName,
            userAddress = orderResponse.userAddress,
            totalPrice = orderResponse.totalPrice,
            createdAt = orderResponse.createdAt,
            orderItems = orderItems
        )
    }
}
