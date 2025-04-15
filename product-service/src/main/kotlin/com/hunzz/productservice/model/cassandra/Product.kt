package com.hunzz.productservice.model.cassandra

import com.hunzz.productservice.dto.response.ProductResponse
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*

@Table
class Product(
    @PrimaryKey
    val id: UUID,

    @Column
    val sellerId: Long,

    @Column
    val name: String,

    @Column
    val description: String,

    @Column
    val price: Int,

    @Column
    var stock: Int,

    @Column
    val tags: List<String>
) {
    fun decreaseStock(count: Int) {
        this.stock -= count
    }

    fun toResponse(): ProductResponse {
        return ProductResponse(
            productId = this.id,
            sellerId = this.sellerId,
            name = this.name,
            description = this.description,
            price = this.price,
            stock = this.stock,
            tags = this.tags
        )
    }
}