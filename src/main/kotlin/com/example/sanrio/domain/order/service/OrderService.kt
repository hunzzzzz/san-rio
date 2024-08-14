package com.example.sanrio.domain.order.service

import com.example.sanrio.domain.address.repository.AddressRepository
import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.order.dto.request.OrderRequest
import com.example.sanrio.domain.order.dto.request.RecallRequest
import com.example.sanrio.domain.order.dto.response.OrderDataResponse
import com.example.sanrio.domain.order.dto.response.OrderSliceResponse
import com.example.sanrio.domain.order.model.*
import com.example.sanrio.domain.order.repository.OrderItemRepository
import com.example.sanrio.domain.order.repository.OrderRepository
import com.example.sanrio.domain.order.repository.RecallRepository
import com.example.sanrio.domain.product.model.ProductStatus.SOLD_OUT
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.global.exception.case.OrderException
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.OrderCodeGenerator.generateOrderCode
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Description
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val cartItemRepository: CartItemRepository,
    private val addressRepository: AddressRepository,
    private val recallRepository: RecallRepository,
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

    @Description("사용하고자 하는 포인트가 유효한지 확인")
    private fun checkPoint(userPoint: Int, point: Int) {
        check(point >= 100 && point % 10 == 0) { throw OrderException("적립금은 100원 이상부터 10원 단위로 사용 가능합니다.") }
        check(point <= userPoint) { throw OrderException("보유하신 적립금보다 작은 값을 입력해주세요.") }
    }

    @Description("주문 후 장바구니를 리셋")
    private fun resetCart(cart: Cart) {
        cart.resetTotalPrice()
        cartItemRepository.deleteAllByCart(cart = cart)
    }

    @Description("결제완료(PAID)인 주문만 취소")
    private fun checkPaid(status: OrderStatus) =
        check(status == OrderStatus.PAID) {
            if (status == OrderStatus.SHIPPING) throw OrderException("해당 상품에 대한 배송이 시작되어, 취소가 불가능합니다.")
            else throw OrderException("잘못된 요청입니다. 취소가 불가능한 주문입니다.")
        }

    @Description("배송완료(DELIVERED)인 주문만 반품 신청 가능")
    private fun checkDelivered(status: OrderStatus) =
        check(status == OrderStatus.DELIVERED) {
            if (status == OrderStatus.SHIPPING) throw OrderException("해당 상품에 대한 배송이 시작되어, 반품 신청이 불가능합니다. 배송 완료 후 신청해주세요.")
            throw OrderException("잘못된 요청입니다. 반품 신청이 불가능합니다.")
        }

    @Description("배송완료(DELIVERED) 후 24시간 내로 반품 신청 가능")
    private fun checkValidDate(updatedAt: LocalDateTime) =
        check(updatedAt <= LocalDateTime.now().plusDays(1))
        { throw OrderException("반품 신청 기한이 지났습니다. 반품 신청은 배송 완료 후 24시간 이내에만 가능합니다.") }

    @Description("유효한 RecallReason인지 확인")
    private fun checkValidReason(reason: String) =
        check(RecallReason.entries.map { it.toString() }
            .contains(reason)) { throw OrderException("올바르지 않은 반품 사유입니다. 다시 확인해주세요.") }

    @Description("반품 이유를 기타(ETC)로 선택한 경우, 반품 사유를 반드시 입력")
    private fun checkRecallDetail(reason: RecallReason, detail: String?) =
        if (reason == RecallReason.ETC) check(!detail.isNullOrBlank()) { throw OrderException("반품 사유를 입력해주세요.") } else Unit

    @Description("해당 유저가 진행한 주문이 맞는지 확인")
    private fun checkUser(order: Order, user: User) =
        check(order.user.id == user.id) { throw OrderException("본인의 주문 건에 대해서만 취소 및 반품 신청이 가능합니다.") }

    @Description("장바구니에 있는 상품들에 대한 주문을 진행")
    @Transactional
    fun makeOrder(userId: Long, request: OrderRequest?) {
        val user = entityFinder.findUserById(userId = userId)
        val cart = entityFinder.findCartByUser(user = user)
        val cartItems = cartItemRepository.findAllByCart(cart = cart)

        checkSoldOut(cartItems = cartItems)
        checkCartEmpty(cartItems = cartItems)
        checkAddress(user = user)
        if (request?.point != null) checkPoint(userPoint = user.point, point = request.point)

        // 배송하고자 하는 유저의 기본 설정 주소
        val address = addressRepository.findByUserAndDefault(user = user, default = true)

        // Order 객체 저장
        val order = Order(
            code = generateOrderCode(cartItems.first().product.characterName),
            totalPrice = cart.totalPrice,
            user = user,
            streetAddress = address.streetAddress,
            detailAddress = address.detailAddress,
            orderRequest = request?.request ?: "부재 시 문 앞에 놔주세요.",
            usedPoint = request?.point
        ).let { orderRepository.save(it) }

        // OrderItem 객체 저장
        cartItems.forEach { cartItem ->
            // 재고 수량 변경
            val product = entityFinder.findProductById(productId = cartItem.product.id!!)
            product.decreaseStock(count = cartItem.count)

            // 저장
            OrderItem(count = cartItem.count, unitPrice = cartItem.unitPrice, product = cartItem.product, order = order)
                .let { orderItem -> orderItemRepository.save(orderItem) }
        }

        // 포인트 적립 - 사용한 포인트
        if (request?.point != null)
            user.updatePoint(point = (cart.totalPrice * 0.02).toInt() + (request.point * (-1)))
        // 포인트 적립
        else
            user.updatePoint(point = (cart.totalPrice * 0.02).toInt())

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

    @Description("주문 취소 신청")
    @Transactional
    fun cancelOrder(userId: Long, orderId: Long) {
        val user = entityFinder.findUserById(userId = userId)
        val order = entityFinder.findOrderById(orderId = orderId)

        checkUser(order = order, user = user)
        checkPaid(status = order.status)

        order.updateStatus(status = OrderStatus.REQUESTED_FOR_CANCEL)
    }

    @Description("주문 취소 신청 승인")
    @Transactional
    fun acceptCancelOrder(userId: Long, orderId: Long) {
        val order = entityFinder.findOrderById(orderId = orderId)
        val user = entityFinder.findUserById(userId = order.user.id!!)

        // 재고 수량 복구
        orderItemRepository.findByOrder(order = order)
            .forEach { orderItem -> orderItem.product.increaseStock(count = orderItem.count) }

        // 사용한 포인트 복구
        user.updatePoint(point = order.usedPoint ?: 0)

        // 적립된 포인트 복구
        user.updatePoint(point = (order.totalPrice * 0.02).toInt() * (-1))

        // 취소 완료
        order.updateStatus(status = OrderStatus.CANCELLED)
    }

    @Description("반품 신청")
    @Transactional
    fun recallOrder(userId: Long, orderId: Long, request: RecallRequest) {
        val order = entityFinder.findOrderById(orderId = orderId)
        val user = entityFinder.findUserById(userId = userId)

        checkUser(order = order, user = user)
        checkValidReason(reason = request.reason!!)
        checkDelivered(status = order.status)
        checkValidDate(updatedAt = order.updatedAt)
        checkRecallDetail(reason = RecallReason.valueOf(request.reason), detail = request.detail)

        order.updateStatus(status = OrderStatus.REQUESTED_FOR_RECALL)

        request.to(order = order).let { recallRepository.save(it) }
    }

    companion object {
        private const val ORDER_PAGE_SIZE = 5
    }
}