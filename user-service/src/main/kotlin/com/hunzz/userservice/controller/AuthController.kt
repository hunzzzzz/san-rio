package com.hunzz.userservice.controller

import com.hunzz.userservice.dto.request.LoginRequest
import com.hunzz.userservice.dto.response.TokenResponse
import com.hunzz.userservice.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<TokenResponse> {
        val user = authService.login(request = request)

        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}