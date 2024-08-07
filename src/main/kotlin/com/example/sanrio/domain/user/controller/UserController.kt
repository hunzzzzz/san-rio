package com.example.sanrio.domain.user.controller

import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.service.UserService
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {
    @Description("일반 유저 회원가입")
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignUpRequest
    ) = ResponseEntity.ok().body(userService.signup(request = request))
}