package com.example.sanrio.domain.order.repository

import com.example.sanrio.domain.order.dto.response.OrderResponse
import com.example.sanrio.domain.order.model.OrderPeriod
import com.example.sanrio.domain.order.model.OrderStatus
import com.example.sanrio.domain.order.model.QOrder
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Description
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.Period

@Repository
class OrderRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : OrderRepositoryCustom {
    private val order = QOrder.order

    @Description("주문 내역 조회")
    override fun getOrders(
        pageable: Pageable,
        userId: Long,
        cursorId: Long?,
        period: OrderPeriod?
    ) =
        BooleanBuilder()
            .let {
                // 커서 기반 페이지네이션을 위해 cursorId 보다 작은 id의 order를 판단
                cursorId?.let { cursorId -> it.and(order.id.lt(cursorId)) }
                it
            }.let {
                // 해당 유저의 주문 내역만 조회
                it.and(order.user.id.eq(userId))
                it
            }.let {
                // PAID(결제완료), SHIPPING(배송중), DELIVERED(배송완료)인 내역만 조회
                it.and(
                    order.status.eq(OrderStatus.PAID)
                        .or(order.status.eq(OrderStatus.SHIPPING))
                        .or(order.status.eq(OrderStatus.DELIVERED))
                )
                it
            }.let {
                // period에 넘어온 값에 따른 기간 판정
                period?.let { period ->
                    LocalDateTime.now().let { now ->
                        it.and(
                            order.createdAt.between(
                                now.minus(
                                    when (period) {
                                        OrderPeriod.ONE_MONTH -> Period.ofMonths(1)
                                        OrderPeriod.THREE_MONTH -> Period.ofMonths(3)
                                        OrderPeriod.ONE_YEAR -> Period.ofYears(1)
                                    }
                                ), now
                            )
                        )
                    }
                }
                it
            }.let { getContents(pageable = pageable, whereClause = it) }
            .let { SliceImpl(it, pageable, hasNext(content = it, pageable = pageable)) }

    @Description("페이지에 포함될 주문 데이터를 가져오는 내부 메서드")
    private fun getContents(pageable: Pageable, whereClause: BooleanBuilder) =
        jpaQueryFactory.select(
            Projections.constructor(
                OrderResponse::class.java,
                order.id,
                order.code,
                order.status,
                order.user.name,
                order.streetAddress,
                order.totalPrice,
                order.createdAt
            )
        ).from(order)
            .where(whereClause)
            .limit(pageable.pageSize.toLong() + 1)
            .orderBy(order.id.desc())
            .fetch()

    @Description("다음 데이터가 있는지 여부를 확인하는 내부 메서드")
    private fun hasNext(content: MutableList<OrderResponse>, pageable: Pageable) =
        (content.size > pageable.pageSize).let {
            if (it) content.removeLast()
            it
        }
}