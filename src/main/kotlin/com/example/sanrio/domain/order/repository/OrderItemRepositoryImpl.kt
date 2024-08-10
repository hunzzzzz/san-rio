package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.dto.response.OrderItemResponse
import com.example.sanrio.domain.order.model.QOrderItem
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Repository

@Repository
class OrderItemRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : OrderItemRepositoryCustom {
    private val orderItem = QOrderItem.orderItem

    @Description("주문 내역 하위 상품 목록 조회")
    override fun getOrderItems(orderId: Long): MutableList<OrderItemResponse> = jpaQueryFactory.select(
        Projections.constructor(
            OrderItemResponse::class.java,
            orderItem.product.name,
            orderItem.unitPrice,
            orderItem.count
        )
    ).from(orderItem)
        .where(BooleanBuilder(orderItem.order.id.eq(orderId)))
        .orderBy(orderItem.id.desc())
        .fetch()
}