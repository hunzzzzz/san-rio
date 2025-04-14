package com.hunzz.productservice.model.mysql

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "product_sellers")
class ProductSeller(
    @Id
    @Column(name = "product_id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "seller_id", nullable = false)
    val sellerId: Long
)