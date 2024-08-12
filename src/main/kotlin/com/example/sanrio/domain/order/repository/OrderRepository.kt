package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OrderRepository : JpaRepository<Order, Long>, OrderRepositoryCustom {
    fun findAllByStatusAndCreatedAtAfter(status: OrderStatus, after: LocalDateTime): List<Order>
}