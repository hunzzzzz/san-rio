package com.example.sanrio.domain.order.service

import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.order.dto.response.OrderDataResponse
import com.example.sanrio.domain.order.dto.response.OrderSliceResponse
import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.OrderItem
import com.example.sanrio.domain.order.model.OrderPeriod
import com.example.sanrio.domain.order.model.OrderStatus
import com.example.sanrio.domain.order.repository.OrderItemRepository
import com.example.sanrio.domain.order.repository.OrderRepository
import com.example.sanrio.domain.product.model.ProductStatus.SOLD_OUT
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.AddressRepository
import com.example.sanrio.global.exception.case.OrderException
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
    @Description("품절 상품 여부 확인")
    private fun checkSoldOut(cartItems: List<CartItem>) =
        check(cartItems.map { it.product.status }
            .count { it == SOLD_OUT } == 0) { throw OrderException("장바구니에 품절된 상품이 포함되어 있습니다.") }

    @Description("장바구니가 비어있는지 확인")
    private fun checkCartEmpty(cartItems: List<CartItem>) =
        check(cartItems.isNotEmpty()) { throw OrderException("장바구니에 상품이 존재하지 않습니다.") }

    @Description("유저의 배송지 정보가 설정되어 있는지 확인")
    private fun checkAddress(user: User) =
        check(addressRepository.existsByUser(user = user)) { throw OrderException("배송지 정보가 존재하지 않습니다.") }

    @Description("주문 후 장바구니를 리셋")
    private fun resetCart(cart: Cart) {
        cart.resetTotalPrice()
        cartItemRepository.deleteAllByCart(cart = cart)
    }

    @Description("장바구니에 있는 상품들에 대한 주문을 진행")
    @Transactional
    fun makeOrder(userId: Long) {
        val user = entityFinder.findUserById(userId = userId)
        val cart = entityFinder.findCartByUser(user = user)
        val cartItems = cartItemRepository.findAllByCart(cart = cart)

        checkSoldOut(cartItems = cartItems)
        checkCartEmpty(cartItems = cartItems)
        checkAddress(user = user)

        // 배송하고자 하는 유저의 기본 설정 주소
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

            // 저장
            OrderItem(count = cartItem.count, unitPrice = cartItem.unitPrice, product = cartItem.product, order = order)
                .let { orderItem -> orderItemRepository.save(orderItem) }
        }

        // 장바구니 리셋
        resetCart(cart = cart)
    }

    @Description("주문 내역 조회")
    fun getOrders(userId: Long, cursorId: Long?, period: OrderPeriod?): OrderSliceResponse {
        val pageable = PageRequest.ofSize(ORDER_PAGE_SIZE)

        // 주문 내역 조회
        val slice =
            orderRepository.getOrders(pageable = pageable, userId = userId, cursorId = cursorId, period = period)

        // 주문 내역 하위의 상품 목록 조회
        val list = mutableListOf<OrderDataResponse>()
        slice.content.forEach { orderResponse ->
            OrderDataResponse.from(
                orderResponse = orderResponse,
                orderItems = orderItemRepository.getOrderItems(orderId = orderResponse.orderId)
            ).let { list.add(it) }
        }

        return OrderSliceResponse(size = slice.size, numberOfElements = slice.numberOfElements, contents = list)
    }

    @Description("결제완료(PAID)인 주문만 취소")
    private fun checkStatus(status: OrderStatus) =
        check(status == OrderStatus.PAID) {
            if (status == OrderStatus.SHIPPING) throw OrderException("해당 상품에 대한 배송이 시작되어, 취소가 불가능합니다.")
            else throw OrderException("잘못된 요청입니다. 취소가 불가능한 주문입니다.")
        }

    @Description("해당 유저가 진행한 주문이 맞는지 확인")
    private fun checkUser(order: Order, user: User) =
        check(order.user.id == user.id) { throw OrderException("본인의 주문 건에 대한 취소 신청만 가능합니다.") }

    @Description("주문 취소 신청")
    @Transactional
    fun cancelOrder(userId: Long, orderId: Long) {
        val user = entityFinder.findUserById(userId = userId)
        val order = entityFinder.findOrderById(orderId = orderId)

        checkUser(order = order, user = user)
        checkStatus(status = order.status)

        order.updateStatus(status = OrderStatus.REQUESTED_FOR_CANCEL)
    }

    @Description("주문 취소 신청 승인")
    @Transactional
    fun acceptCancelOrder(userId: Long, orderId: Long) {
        val order = entityFinder.findOrderById(orderId = orderId)

        // 재고 수량 복구
        orderItemRepository.findByOrder(order = order)
            .forEach { orderItem -> orderItem.product.increaseStock(count = orderItem.count) }

        // 취소 완료
        order.updateStatus(status = OrderStatus.CANCELLED)
    }

    companion object {
        private const val ORDER_PAGE_SIZE = 5
    }
}