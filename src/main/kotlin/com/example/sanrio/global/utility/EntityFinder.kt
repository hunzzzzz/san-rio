package com.example.sanrio.global.utility

import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.product.repository.ProductRepository
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.ModelNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EntityFinder(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) {
    fun findProductById(productId: Long) =
        productRepository.findByIdOrNull(productId) ?: throw ModelNotFoundException("상품")

    fun findCartById(cartId: Long) =
        cartRepository.findByIdOrNull(cartId) ?: throw ModelNotFoundException("장바구니")

    fun findUserById(userId: Long) =
        userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("유저")
}