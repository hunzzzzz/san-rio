package com.example.sanrio.domain.order.service

import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.order.dto.response.OrderDataResponse
import com.example.sanrio.domain.order.dto.response.OrderSliceResponse
import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.OrderItem
import com.example.sanrio.domain.order.model.OrderPeriod
import com.example.sanrio.domain.order.repository.OrderItemRepository
import com.example.sanrio.domain.order.repository.OrderRepository
import com.example.sanrio.domain.product.model.ProductStatus.SOLD_OUT
import com.example.sanrio.domain.user.repository.AddressRepository
import com.example.sanrio.global.exception.case.EmptyCartException
import com.example.sanrio.global.exception.case.SoldOutItemsInCartException
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.OrderCodeGenerator.generateOrderCode
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Description
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val addressRepository: AddressRepository,
    private val entityFinder: EntityFinder
) {
    @Description("장바구니에 있는 상품들에 대한 주문을 진행")
    @Transactional
    fun makeOrder(userId: Long) {
        val user = entityFinder.findUserById(userId = userId)
        val cart = entityFinder.findCartByUser(user = user)
        val cartItems = cartItemRepository.findAllByCart(cart = cart)

        // 품절 상품 여부 확인
        check(cartItems.map { it.product.status }.count { it == SOLD_OUT } == 0) { throw SoldOutItemsInCartException() }

        // 장바구니가 비어있는지 확인
        check(cartItems.isNotEmpty()) { throw EmptyCartException() }

        // 유저의 배송지 정보가 설정되어 있는지 확인
        check(addressRepository.existsByUser(user = user)) { throw EmptyCartException() }

        val address = addressRepository.findByUserAndDefault(user = user, default = true)

        // Order 객체 저장
        val order = Order(
            code = generateOrderCode(cartItems.first().product.characterName),
            totalPrice = cart.totalPrice,
            user = user,
            streetAddress = address.streetAddress,
            detailAddress = address.detailAddress
        ).let { orderRepository.save(it) }

        // OrderItem 객체 저장
        cartItems.forEach { cartItem ->
            // 재고 수량 변경
            entityFinder.findProductById(productId = cartItem.product.id!!)
                .let { product -> product.decreaseStock(count = cartItem.count) }

            OrderItem(count = cartItem.count, unitPrice = cartItem.unitPrice, product = cartItem.product, order = order)
                .let { orderItem -> orderItemRepository.save(orderItem) }
        }

        // 장바구니 리셋
        cart.resetTotalPrice()
        cartItemRepository.deleteAllByCart(cart = cart)
    }

    @Description("주문 내역 조회")
    fun getOrders(userId: Long, cursorId: Long?, period: OrderPeriod?): OrderSliceResponse {
        val pageable = PageRequest.ofSize(ORDER_PAGE_SIZE)

        val slice =
            orderRepository.getOrders(pageable = pageable, userId = userId, cursorId = cursorId, period = period)
        val list = mutableListOf<OrderDataResponse>()

        slice.content.forEach { orderResponse ->
            OrderDataResponse.from(
                orderResponse = orderResponse,
                orderItems = orderItemRepository.getOrderItems(orderId = orderResponse.orderId)
            ).let { list.add(it) }
        }

        return OrderSliceResponse(size = slice.size, numberOfElements = slice.numberOfElements, contents = list)
    }

    companion object {
        private const val ORDER_PAGE_SIZE = 5
    }
}