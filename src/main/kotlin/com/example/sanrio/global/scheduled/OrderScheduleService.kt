package com.example.sanrio.global.scheduled

import com.example.sanrio.domain.order.model.OrderStatus
import com.example.sanrio.domain.order.repository.OrderRepository
import com.example.sanrio.global.utility.EntityFinder
import org.springframework.context.annotation.Description
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderScheduleService(
    private val orderRepository: OrderRepository,
    private val entityFinder: EntityFinder,
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Description("LocalDateTime을 문자열로 전환")
    private fun localDateTimeToString(time: LocalDateTime) =
        "${time.year}/${time.monthValue}/${time.dayOfMonth}/${time.hour}/${time.minute}"

    @Description("문자열을 LocalDateTime으로 전환")
    private fun stringToLocalDateTime(time: String) =
        time.split('/').map { it.toInt() }
            .let { (year, month, day, hour, minute) -> LocalDateTime.of(year, month, day, hour, minute) }

    @Description("결제완료(PAID) 후 하루가 지나면 배송중(SHIPPING)으로 변경")
    @Scheduled(fixedRate = 1000 * 60 * 5) // 5분 간격
    fun updateOrderStatusByShipping() {
        // 결제완료(PAID) 후 1일이 지난 주문의 status를 배송중(SHIPPING)으로 변경
        val keys = redisTemplate.keys("order_paid_*")
        keys.forEach {
            val hash = redisTemplate.opsForHash<String, String>().entries(it)
            val orderId = hash["orderId"]!!.toLong()
            val createdAt = stringToLocalDateTime(time = hash["createdAt"]!!)

            if (createdAt > LocalDateTime.now().plusDays(1)) {
                val order = entityFinder.findOrderById(orderId = orderId)
                order.updateStatus(OrderStatus.SHIPPING)
                redisTemplate.delete("order_paid_${orderId}")
            }
        }

        // 결제완료(PAID) 후 1일이 채 지나지 않은 모든 주문(Order)의 id와 createdAt을 캐시에 재등록
        orderRepository.findAllByStatusAndCreatedAtAfter(
            status = OrderStatus.PAID,
            after = LocalDateTime.now().minusDays(1)
        ).forEach { order ->
            val hash = mutableMapOf(
                "orderId" to order.id.toString(),
                "createdAt" to localDateTimeToString(time = order.createdAt)
            )
            redisTemplate.opsForHash<String, String>().putAll("order_paid_${order.id}", hash)
        }
    }

    @Description("배송중(SHIPPING) 후 하루가 지나면 배송완료(DELIVERED)으로 변경")
    @Scheduled(fixedRate = 1000 * 60 * 5) // 5분 간격
    fun updateOrderStatusByDelivered() {
        // 배송중(SHIPPING) 후 1일이 지난 주문의 status를 배송완료(DELIVERED)으로 변경
        val keys = redisTemplate.keys("order_shipping_*")
        keys.forEach {
            val hash = redisTemplate.opsForHash<String, String>().entries(it)
            val orderId = hash["orderId"]!!.toLong()
            val createdAt = stringToLocalDateTime(time = hash["createdAt"]!!)

            if (createdAt > LocalDateTime.now().plusDays(1)) {
                val order = entityFinder.findOrderById(orderId = orderId)
                order.updateStatus(OrderStatus.DELIVERED)
                redisTemplate.delete("order_shipping_${orderId}")
            }
        }

        // 배송중(SHIPPING) 후 1일이 채 지나지 않은 모든 주문(Order)의 id와 createdAt을 캐시에 재등록
        orderRepository.findAllByStatusAndCreatedAtAfter(
            status = OrderStatus.SHIPPING,
            after = LocalDateTime.now().minusDays(1)
        ).forEach { order ->
            val hash = mutableMapOf(
                "orderId" to order.id.toString(),
                "createdAt" to localDateTimeToString(time = order.createdAt)
            )
            redisTemplate.opsForHash<String, String>().putAll("order_shipping_${order.id}", hash)
        }
    }
}