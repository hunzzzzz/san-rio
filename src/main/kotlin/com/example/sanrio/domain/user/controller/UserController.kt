package com.example.sanrio.domain.user.controller

import com.example.sanrio.domain.user.service.UserService
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {
}