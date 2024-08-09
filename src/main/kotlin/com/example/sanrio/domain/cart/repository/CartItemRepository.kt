package com.example.sanrio.domain.cart.repository

import com.example.sanrio.domain.cart.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long>