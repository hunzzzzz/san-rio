package com.example.sanrio.domain.cart

import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.global.exception.case.OutOfStockException
import com.example.sanrio.global.exception.case.TooManyItemsException
import com.example.sanrio.global.utility.EntityFinder
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val cartRepository: CartRepository,
    private val entityFinder: EntityFinder
) {
    @Description("장바구니에 상품 추가")
    fun addItems(userId: Long, productId: Long, count: Int) {
        val product = entityFinder.findProductById(productId = productId)
        val cart = cartRepository.findByUser(user = entityFinder.findUserById(userId = userId))

        check(count <= 5) { throw TooManyItemsException() } // 최대 5개의 동일 상품만 담을 수 있다.
        check(count <= product.stock) { throw OutOfStockException(stock = product.stock) } // 재고를 초과하는 수량을 담을 수 없다.

        CartItem(count = count, product = product, cart = cart).let { cartItemRepository.save(it) }
    }
}