package com.example.sanrio.domain.user.model

import jakarta.persistence.*

@Entity
@Table(name = "address")
class Address(
    @Column(name = "zipcode", nullable = false)
    var zipcode: String,

    @Column(name = "street_address", nullable = false)
    var streetAddress: String,

    @Column(name = "detail_address", nullable = false)
    var detailAddress: String,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false, unique = true)
    val id: Long? = null
}