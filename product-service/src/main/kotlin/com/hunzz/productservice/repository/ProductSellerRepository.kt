package com.hunzz.productservice.repository

import com.hunzz.productservice.model.mysql.ProductSeller
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductSellerRepository : JpaRepository<ProductSeller, Long> {
    @Query("SELECT ps.id FROM ProductSeller as ps WHERE ps.sellerId = :sellerId")
    fun findAllBySellerId(sellerId: Long): List<UUID>
}