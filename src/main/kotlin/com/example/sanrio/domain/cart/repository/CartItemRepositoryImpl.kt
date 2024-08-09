package com.example.sanrio.domain.cart.repository

import com.example.sanrio.domain.cart.dto.response.CartItemResponse
import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.model.QCartItem
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class CartItemRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : CartItemRepositoryCustom {
    private val cartItem = QCartItem.cartItem

    override fun getCartItems(cart: Cart): List<CartItemResponse> =
        jpaQueryFactory.select(
            Projections.constructor(
                CartItemResponse::class.java,
                cartItem.product.id,
                cartItem.product.status,
                cartItem.product.name,
                cartItem.totalPrice,
                cartItem.count
            )
        ).from(cartItem)
            .where(BooleanBuilder(cartItem.cart.eq(cart)))
            .orderBy(cartItem.id.desc())
            .fetch()
}