package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, Long>, OrderItemRepositoryCustom {
    fun findByOrder(order: Order): List<OrderItem>
}