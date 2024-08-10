package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.dto.response.OrderResponse
import com.example.sanrio.domain.order.model.OrderPeriod
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository

@Repository
interface OrderRepositoryCustom {
    fun getOrders(
        pageable: Pageable,
        userId: Long,
        cursorId: Long?,
        period: OrderPeriod?
    ): Slice<OrderResponse>
}