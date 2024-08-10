package com.example.sanrio.domain.user.model

import com.example.sanrio.domain.user.dto.request.AddressRequest
import com.example.sanrio.global.model.BaseEntity
import com.example.sanrio.global.utility.Encryptor
import jakarta.persistence.*
import org.springframework.context.annotation.Description

@Entity
@Table(name = "address")
class Address(
    @Column(name = "zip_code", nullable = false)
    var zipCode: String,

    @Column(name = "street_address", nullable = false)
    var streetAddress: String,

    @Lob
    @Column(name = "detail_address", nullable = false)
    var detailAddress: ByteArray,

    @Column(name = "is_selected", nullable = false)
    var default: Boolean,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false, unique = true)
    val id: Long? = null

    @Description("주소 업데이트")
    fun update(request: AddressRequest, encryptor: Encryptor) =
        request.let {
            this.zipCode = it.zipCode!!
            this.streetAddress = it.streetAddress!!
            this.detailAddress = encryptor.encrypt(it.detailAddress!!)
            this
        }
}