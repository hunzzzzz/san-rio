package com.hunzz.productservice.repository

import com.hunzz.productservice.model.mysql.ProductSeller
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductSellerRepository : JpaRepository<ProductSeller, Long> {
    fun findAllBySellerId(sellerId: Long): List<ProductSeller>
}