package com.example.sanrio.domain.address.repository

import com.example.sanrio.domain.address.model.Address
import com.example.sanrio.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun existsByUser(user: User): Boolean

    fun findByUserAndDefault(user: User, default: Boolean): Address
}