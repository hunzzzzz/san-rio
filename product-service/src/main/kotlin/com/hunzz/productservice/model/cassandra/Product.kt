package com.hunzz.productservice.model.cassandra

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
    val stock: Int,

    @Column
    val tags: List<String>
)