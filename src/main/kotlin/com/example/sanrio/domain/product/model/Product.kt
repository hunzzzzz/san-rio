package com.example.sanrio.domain.product.model

import com.example.sanrio.domain.character.model.Character
import com.example.sanrio.global.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "products")
class Product(
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ProductStatus = ProductStatus.SALE,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "detail", nullable = false)
    val detail: String,

    @Column(name = "stock", nullable = false)
    val stock: Int,

    @ManyToOne
    @JoinColumn(name = "character_id", nullable = false)
    val character: Character
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, unique = true)
    val id: Long? = null
}