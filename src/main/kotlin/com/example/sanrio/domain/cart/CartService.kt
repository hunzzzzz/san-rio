package com.example.sanrio.domain.cart

import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.global.exception.case.NegativeCountException
import com.example.sanrio.global.exception.case.OutOfStockException
import com.example.sanrio.global.exception.case.TooManyItemsException
import com.example.sanrio.global.utility.EntityFinder
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val entityFinder: EntityFinder
) {
    @Description("수량의 유효성을 체크")
    fun checkItemCount(count: Int, stock: Int) {
        check(count > 0) { throw NegativeCountException() } // 수량은 0보다 커야한다.
        check(count <= 5) { throw TooManyItemsException() } // 최대 5개의 동일 상품만 담을 수 있다.
        check(count <= stock) { throw OutOfStockException(stock = stock) } // 재고를 초과하는 수량을 담을 수 없다.
    }

    @Description("장바구니에 상품 추가")
    fun addItems(userId: Long, productId: Long, count: Int) {
        val user = entityFinder.findUserById(userId = userId)
        val product = entityFinder.findProductById(productId = productId)
        val cart = entityFinder.findCartByUser(user = user)

        checkItemCount(count = count, stock = product.stock)

        CartItem(count = count, product = product, cart = cart).let { cartItemRepository.save(it) }
    }

    @Description("장바구니에 상품 수량 수정")
    @Transactional
    fun updateItemCount(userId: Long, productId: Long, count: Int) {
        val user = entityFinder.findUserById(userId = userId)
        val product = entityFinder.findProductById(productId = productId)
        val cart = entityFinder.findCartByUser(user = user)

        checkItemCount(count = count, stock = product.stock)

        entityFinder.findCartItemByCartAndProduct(cart = cart, product = product)
            .updateCount(count = count)
    }
}