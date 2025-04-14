package com.hunzz.userservice.repository

import com.hunzz.userservice.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun existsByLoginId(loginId: String): Boolean
    fun findByLoginId(loginId: String): User?
}