package com.example.sanrio.domain.order.controller

import com.example.sanrio.domain.address.model.Address
import com.example.sanrio.domain.address.repository.AddressRepository
import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.order.dto.request.OrderRequest
import com.example.sanrio.domain.order.dto.request.RecallRequest
import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.OrderItem
import com.example.sanrio.domain.order.model.OrderStatus
import com.example.sanrio.domain.order.model.RecallReason
import com.example.sanrio.domain.order.repository.OrderItemRepository
import com.example.sanrio.domain.order.repository.OrderRepository
import com.example.sanrio.domain.order.repository.RecallRepository
import com.example.sanrio.domain.product.model.CharacterName
import com.example.sanrio.domain.product.model.Product
import com.example.sanrio.domain.product.model.ProductStatus
import com.example.sanrio.domain.product.repository.ProductRepository
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.model.UserRole
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.auth.WithCustomMockUser
import com.example.sanrio.global.jwt.AuthenticationHelper
import com.example.sanrio.global.utility.Encryptor
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.JwtProvider
import com.example.sanrio.global.utility.NicknameGenerator.generateNickname
import com.example.sanrio.global.utility.OrderCodeGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired
    private lateinit var entityFinder: EntityFinder

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var encryptor: Encryptor

    @Autowired
    lateinit var authenticationHelper: AuthenticationHelper

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var jwtProvider: JwtProvider

    @Autowired
    lateinit var addressRepository: AddressRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var cartRepository: CartRepository

    @Autowired
    lateinit var cartItemRepository: CartItemRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    lateinit var recallRepository: RecallRepository

    @Autowired
    lateinit var response: HttpServletResponse

    @AfterEach
    fun clean() {
        recallRepository.deleteAll()
        orderItemRepository.deleteAll()
        orderRepository.deleteAll()
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        productRepository.deleteAll()
        addressRepository.deleteAll()
        userRepository.deleteAll()
        jwtProvider.deleteCookie(response = response)
    }

    @Test
    @WithCustomMockUser
    fun 정상적으로_주문이_완료된_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val cart = setCart(user = user)

        setAddress(user = user)
        setProducts()
        setCartItems(cart = cart)

        val initialPoint = user.point

        // expected
        mockMvc.perform(
            post("/orders")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(orderRepository.count()).isEqualTo(1)
        assertThat(orderItemRepository.count()).isEqualTo(productRepository.count())
        assertThat(entityFinder.findCartByUser(user = user).totalPrice).isEqualTo(0)
        assertThat(cartItemRepository.count()).isEqualTo(0)
        assertThat(entityFinder.findUserById(userId = user.id!!).point)
            .isEqualTo(initialPoint + (TEMP_TOTAL_PRICE * 0.02).toInt()) // 기존 포인트 + 적립 포인트
    }

    @Test
    @WithCustomMockUser
    fun 포인트를_사용하여_주문한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val cart = setCart(user = user)
        val initialPoint = user.point
        val usingPoint = 1000

        val request = OrderRequest(point = usingPoint, request = "요청사항")
        val json = objectMapper.writeValueAsString(request)

        setAddress(user = user)
        setProducts()
        setCartItems(cart = cart)

        // expected
        mockMvc.perform(
            post("/orders")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(entityFinder.findUserById(userId = user.id!!).point)
            .isEqualTo(initialPoint - usingPoint + (TEMP_TOTAL_PRICE * 0.02).toInt()) // 기존 포인트 - 사용 포인트 + 적립 포인트
    }

    @Test
    @WithCustomMockUser
    fun 주문_완료_이후_특정상품의_재고가_0이_된_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val cart = setCart(user = user)

        setAddress(user = user)
        setProducts(quantity = 1)
        setCartItems(cart = cart, quantity = 1)

        // expected
        mockMvc.perform(
            post("/orders")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(productRepository.findAll().map { it.status }.contains(ProductStatus.SALE)).isFalse()
        assertThat(productRepository.findAll().sumOf { it.stock }).isEqualTo(0)
    }

    @Test
    @WithCustomMockUser
    fun 장바구니가_비어있는_상태에서_주문을_시도한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)

        setAddress(user = user)
        setCart(user = user)
        setProducts()

        // expected
        mockMvc.perform(
            post("/orders")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("장바구니에 상품이 존재하지 않습니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 장바구니에_품절된_상품이_존재하는_상태에서_주문을_시도한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val cart = setCart(user = user)

        setAddress(user = user)
        setProducts(quantity = 0)
        setCartItems(cart = cart)

        // expected
        mockMvc.perform(
            post("/orders")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("장바구니에 품절된 상품이 포함되어 있습니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 모든_주문이_배송완료인_경우_첫페이지_확인() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)

        setOrders(user = user, status = OrderStatus.DELIVERED)

        // expected
        mockMvc.perform(
            get("/orders")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.size").value(ORDER_PAGE_SIZE))
            .andExpect(
                jsonPath("$.contents[0].orderId")
                    .value(orderRepository.findAll(Sort.by(DESC, "id")).first().id!!)
            ).andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 모든_주문이_배송완료인_경우_중간페이지_확인() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val cursorId = (2..<AMOUNT_OF_ORDER).random()

        setOrders(user = user, status = OrderStatus.DELIVERED)

        // expected
        mockMvc.perform(
            get("/orders?cursorId=${cursorId}")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.size").value(ORDER_PAGE_SIZE))
            .andExpect(
                jsonPath("$.contents[0].orderId").value(cursorId - 1)
            ).andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 모든_주문이_배송완료인_경우_마지막페이지_확인() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val cursorId = 1L

        setOrders(user = user, status = OrderStatus.DELIVERED)

        // expected
        mockMvc.perform(
            get("/orders?cursorId=${cursorId}")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.size").value(ORDER_PAGE_SIZE))
            .andExpect(jsonPath("$.contents.size()").value(0))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 모든_주문이_취소완료인_경우_아무런_값을_보여주지_않는지_확인() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        setOrders(user = user, status = OrderStatus.CANCELLED)

        // expected
        mockMvc.perform(
            get("/orders")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.size").value(ORDER_PAGE_SIZE))
            .andExpect(jsonPath("$.contents.size()").value(0))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 주문이_정상적으로_취소_신청된_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user)

        // expected
        mockMvc.perform(
            get("/orders/${order.id}/cancel")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(entityFinder.findOrderById(orderId = order.id!!).status).isEqualTo(OrderStatus.REQUESTED_FOR_CANCEL)
    }

    @Test
    @WithCustomMockUser
    fun 본인이_아닌_주문에_대한_취소_신청한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val anotherUser = setUser()
        val order = setOrder(user = anotherUser)

        // expected
        mockMvc.perform(
            get("/orders/${order.id}/cancel")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("본인의 주문 건에 대해서만 취소 및 반품 신청이 가능합니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 배송중인_주문에_대한_취소_신청한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.SHIPPING)

        // expected
        mockMvc.perform(
            get("/orders/${order.id}/cancel")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("해당 상품에 대한 배송이 시작되어, 취소가 불가능합니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 배송완료된_주문에_대한_취소_신청한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.DELIVERED)

        // expected
        mockMvc.perform(
            get("/orders/${order.id}/cancel")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("잘못된 요청입니다. 취소가 불가능한 주문입니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 정상적으로_관리자가_주문_취소_신청을_승인한_경우() {
        // given
        val admin = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val user = setUser()
        val order = setOrder(user = user, status = OrderStatus.REQUESTED_FOR_CANCEL)

        val initialPoint = user.point
        val quantity = 1

        setProducts()
        setOrderItems(order = order, quantity = quantity)

        // expected
        mockMvc.perform(
            put("/orders/${order.id}/cancel")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = admin))
                .cookie(setRtkCookie(user = admin))
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(entityFinder.findOrderById(orderId = order.id!!).status).isEqualTo(OrderStatus.CANCELLED)
        assertThat(productRepository.findAll().map { it.stock }
            .count { it == AMOUNT_OF_PRODUCTS + quantity }).isEqualTo(productRepository.count())
        assertThat(entityFinder.findUserById(userId = user.id!!).point)
            .isEqualTo(
                initialPoint + (order.usedPoint ?: 0) - (TEMP_TOTAL_PRICE * 0.02).toInt()
            ) // 기존 포인트 + 사용 호인트 - 적립 포인트
    }

    @Test
    @WithCustomMockUser
    fun 주문이_정상적으로_반품_신청된_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.DELIVERED)
        val request = RecallRequest(reason = "DEFECTIVE", detail = "반품 세부 사유")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/orders/${order.id}/recall")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(entityFinder.findOrderById(orderId = order.id!!).status).isEqualTo(OrderStatus.REQUESTED_FOR_RECALL)
        assertThat(recallRepository.count()).isEqualTo(1)
        assertThat(entityFinder.findRecallByOrderId(orderId = order.id!!).reason).isEqualTo(RecallReason.DEFECTIVE)
        assertThat(entityFinder.findRecallByOrderId(orderId = order.id!!).detail).isEqualTo("반품 세부 사유")
    }

    @Test
    @WithCustomMockUser
    fun 본인이_아닌_주문에_대한_반품_신청한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val anotherUser = setUser()
        val order = setOrder(user = anotherUser, status = OrderStatus.DELIVERED)
        val request = RecallRequest(reason = "DEFECTIVE", detail = "반품 세부 사유")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/orders/${order.id}/recall")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("본인의 주문 건에 대해서만 취소 및 반품 신청이 가능합니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 반품_사유에_올바르지_않은_값이_들어온_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.CANCELLED)
        val request = RecallRequest(reason = "WRONG", detail = "반품 세부 사유")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/orders/${order.id}/recall")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("올바르지 않은 반품 사유입니다. 다시 확인해주세요."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 반품_사유를_기타로_설정했는데_세부_사유를_입력하지_않은_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.DELIVERED)
        val request = RecallRequest(reason = "ETC", detail = "")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/orders/${order.id}/recall")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("반품 사유를 입력해주세요."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 배송중인_주문에_대한_반품_신청한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.SHIPPING)
        val request = RecallRequest(reason = "DEFECTIVE", detail = "반품 세부 사유")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/orders/${order.id}/recall")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("해당 상품에 대한 배송이 시작되어, 반품 신청이 불가능합니다. 배송 완료 후 신청해주세요."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 취소완료인_주문에_대한_반품_신청한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val order = setOrder(user = user, status = OrderStatus.CANCELLED)
        val request = RecallRequest(reason = "DEFECTIVE", detail = "반품 세부 사유")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/orders/${order.id}/recall")
                .contentType(APPLICATION_JSON)
                .content(json)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("잘못된 요청입니다. 반품 신청이 불가능합니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    private fun setAtkCookie(user: User) =
        jwtProvider.getAccessToken(userId = user.id!!, email = user.email, role = user.role)
            .let { atk ->
                Cookie("AccessToken", atk).let {
                    it.path = "/"
                    it.maxAge = ATK_EXPIRATION_TIME
                    it
                }
            }

    private fun setRtkCookie(user: User) =
        jwtProvider.getAccessToken(userId = user.id!!, email = user.email, role = user.role)
            .let { rtk ->
                Cookie("RefreshToken", rtk).let {
                    it.path = "/"
                    it.maxAge = RTK_EXPIRATION_TIME
                    it
                }
            }

    private fun setUser() = User(
        role = UserRole.USER,
        email = "test2@gmail.com",
        password = "Test1234!",
        name = "테스트 계정2",
        nickname = generateNickname(),
        phone = "010-1234-5678"
    ).let { userRepository.save(it) }

    private fun setAddress(user: User) = Address(
        zipCode = "00000",
        streetAddress = "테스트 도로명 주소",
        detailAddress = encryptor.encrypt("테스트 상세 주소"),
        default = true,
        user = user
    ).let { addressRepository.save(it) }

    private fun setProducts(quantity: Int = AMOUNT_OF_PRODUCTS) = productRepository.saveAll(
        listOf(
            Product(
                characterName = CharacterName.HELLO_KITTY,
                status = if (quantity == 0) ProductStatus.SOLD_OUT else ProductStatus.SALE,
                name = "헬로키티 굿즈",
                detail = "헬로키티 굿즈입니다.",
                price = PRICE_OF_PRODUCTS,
                stock = quantity
            ),

            Product(
                characterName = CharacterName.POCHACCO,
                name = "포챠코 굿즈",
                detail = "포챠코 굿즈입니다.",
                price = PRICE_OF_PRODUCTS,
                stock = quantity
            )
        )
    )

    private fun setCart(user: User) = Cart(user = user, totalPrice = TEMP_TOTAL_PRICE).let { cartRepository.save(it) }

    private fun setCartItems(cart: Cart, quantity: Int? = null): Int {
        var totalPrice = 0
        productRepository.findAll().forEach { product ->
            val count = quantity ?: (1..5).random()
            val unitPrice = product.price * count
            totalPrice += unitPrice

            CartItem(count = count, unitPrice = unitPrice, product = product, cart = cart)
                .let { cartItem -> cartItemRepository.save(cartItem) }
        }
        return totalPrice
    }

    private fun setOrder(user: User, status: OrderStatus = OrderStatus.PAID) =
        Order(
            code = OrderCodeGenerator.generateOrderCode(CharacterName.entries.toTypedArray().random()),
            status = status,
            totalPrice = TEMP_TOTAL_PRICE,
            streetAddress = "도로명 주소",
            detailAddress = encryptor.encrypt("상세 주소"),
            orderRequest = "요청 사항",
            usedPoint = 0,
            user = user
        ).let { orderRepository.save(it) }

    private fun setOrderItems(order: Order, quantity: Int) = productRepository.findAll().forEach { product ->
        OrderItem(
            count = quantity,
            unitPrice = product.price * quantity,
            product = product,
            order = order
        ).let { orderItemRepository.save(it) }
    }

    private fun setOrders(user: User, status: OrderStatus? = null) =
        (1..AMOUNT_OF_ORDER).forEach { _ ->
            Order(
                code = OrderCodeGenerator.generateOrderCode(CharacterName.entries.toTypedArray().random()),
                status = status ?: OrderStatus.entries.toTypedArray().random(),
                totalPrice = TEMP_TOTAL_PRICE,
                streetAddress = "도로명 주소",
                detailAddress = encryptor.encrypt("상세 주소"),
                orderRequest = "요청 사항",
                usedPoint = 0,
                user = user
            ).let { orderRepository.save(it) }
        }

    companion object {
        const val ORDER_PAGE_SIZE = 5
        const val AMOUNT_OF_ORDER = 100
        const val TEMP_TOTAL_PRICE = 100_000
        const val PRICE_OF_PRODUCTS = 10_000
        const val AMOUNT_OF_PRODUCTS = 100
        const val ATK_EXPIRATION_TIME = 1000 * 60 // 1분
        const val RTK_EXPIRATION_TIME = 1000 * 60 * 5 // 5분
    }
}