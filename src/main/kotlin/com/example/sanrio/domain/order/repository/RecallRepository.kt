package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.Recall
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecallRepository : JpaRepository<Recall, Long> {
    fun findByOrder(order: Order): Recall?
}