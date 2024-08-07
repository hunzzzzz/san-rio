package com.example.sanrio.domain.user.service

import com.example.sanrio.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

}