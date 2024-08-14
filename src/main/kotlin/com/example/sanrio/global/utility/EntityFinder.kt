package com.example.sanrio.global.utility

import com.example.sanrio.domain.address.repository.AddressRepository
import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.order.repository.OrderRepository
import com.example.sanrio.domain.order.repository.RecallRepository
import com.example.sanrio.domain.product.model.Product
import com.example.sanrio.domain.product.repository.ProductRepository
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.ModelNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EntityFinder(
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val orderRepository: OrderRepository,
    private val recallRepository: RecallRepository
) {
    fun findProductById(productId: Long) =
        productRepository.findByIdOrNull(productId) ?: throw ModelNotFoundException("상품")

    fun findCartByUser(user: User) =
        cartRepository.findByUser(user = user) ?: throw ModelNotFoundException("장바구니")

    fun findCartItemByCartAndProduct(cart: Cart, product: Product) =
        cartItemRepository.findByCartAndProduct(cart = cart, product = product)
            ?: throw ModelNotFoundException("상품")

    fun findOrderById(orderId: Long) =
        orderRepository.findByIdOrNull(orderId) ?: throw ModelNotFoundException("주문")

    fun findRecallByOrderId(orderId: Long) =
        recallRepository.findByOrder(order = findOrderById(orderId = orderId)) ?: throw ModelNotFoundException("반품")

    fun findUserById(userId: Long) =
        userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("유저")

    fun findUserByEmail(email: String) =
        userRepository.findByEmail(email = email) ?: throw ModelNotFoundException("유저")

    fun findAddressById(addressId: Long) =
        addressRepository.findByIdOrNull(addressId) ?: throw ModelNotFoundException("주소")
}