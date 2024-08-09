package com.example.sanrio.domain.cart.repository

import com.example.sanrio.domain.cart.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : JpaRepository<Cart, Long>