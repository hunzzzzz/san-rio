package com.example.sanrio.domain.cart.service

import com.example.sanrio.domain.cart.dto.response.CartResponse
import com.example.sanrio.domain.cart.model.CartItem
import com.example.sanrio.domain.cart.repository.CartItemRepository
import com.example.sanrio.global.exception.case.DuplicatedValueException
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
    @Transactional
    fun addItems(userId: Long, productId: Long, count: Int) {
        val user = entityFinder.findUserById(userId = userId)
        val product = entityFinder.findProductById(productId = productId)
        val cart = entityFinder.findCartByUser(user = user)

        checkItemCount(count = count, stock = product.stock) // count 유효성 체크
        check(
            !cartItemRepository.existsByCartAndProduct(
                cart = cart,
                product = product
            )
        ) { throw DuplicatedValueException("상품") } // 장바구니에 해당 상품 존재 여부 확인

        CartItem(count = count, unitPrice = product.price * count, product = product, cart = cart)
            .let { cartItemRepository.save(it) }

        cart.updateTotalPrice(product.price * count)
    }

    @Description("장바구니에 상품 목록 조회")
    @Transactional
    fun getItems(userId: Long): CartResponse {
        val user = entityFinder.findUserById(userId = userId)
        val cart = entityFinder.findCartByUser(user = user)

        return CartResponse.from(cart = cart, cartItems = cartItemRepository.getCartItems(cart = cart))
    }

    @Description("장바구니에 상품 수량 수정")
    @Transactional
    fun updateItemCount(userId: Long, productId: Long, count: Int) {
        val user = entityFinder.findUserById(userId = userId)
        val product = entityFinder.findProductById(productId = productId)
        val cart = entityFinder.findCartByUser(user = user)

        checkItemCount(count = count, stock = product.stock)

        entityFinder.findCartItemByCartAndProduct(cart = cart, product = product)
            .let { cartItem ->
                // totalCount 에서 기존값을 뺀 다음, 새로운 값을 더한다.
                cart.updateTotalPrice(unitPrice = cartItem.unitPrice * (-1) + product.price * count)
                cartItem.updateCount(count = count)
            }
    }

    @Description("장바구니에 상품 삭제")
    fun deleteItem(userId: Long, productId: Long) {
        val user = entityFinder.findUserById(userId = userId)
        val product = entityFinder.findProductById(productId = productId)
        val cart = entityFinder.findCartByUser(user = user)

        entityFinder.findCartItemByCartAndProduct(cart = cart, product = product)
            .let { cartItem ->
                cart.updateTotalPrice(unitPrice = cartItem.unitPrice * (-1))
                cartItemRepository.delete(cartItem)
            }
    }
}