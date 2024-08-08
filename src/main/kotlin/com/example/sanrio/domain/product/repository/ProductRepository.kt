package com.example.sanrio.domain.product.repository

import com.example.sanrio.domain.product.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long>