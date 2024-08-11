package com.example.sanrio.domain.auth.controller

import com.example.sanrio.domain.auth.service.AuthService
import com.example.sanrio.domain.user.dto.request.LoginRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) {
    @Description("로그인")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ) = authService.login(request = request, response = response)
        .let { ResponseEntity.ok().body(it) }
}