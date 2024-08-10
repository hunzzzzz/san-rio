package com.example.sanrio.domain.product.model

import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "products")
class Product(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ProductStatus = ProductStatus.SALE,

    @Enumerated(EnumType.STRING)
    @Column(name = "character_name", nullable = false)
    val characterName: CharacterName,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "detail", nullable = false)
    val detail: String,

    @Column(name = "price", nullable = false)
    val price: Int,

    @Column(name = "stock", nullable = false)
    var stock: Int
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, unique = true)
    val id: Long? = null

    @Description("재고 수량 감소")
    fun decreaseStock(count: Int) {
        this.stock -= count
        if (this.stock == 0) this.status = ProductStatus.SOLD_OUT
    }
}