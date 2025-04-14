package com.hunzz.userservice.controller

import com.hunzz.userservice.dto.request.SignupRequest
import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.service.SignupService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/signup")
class SignupController(
    private val signupService: SignupService
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignupRequest
    ): ResponseEntity<UserResponse> {
        val user = signupService.signup(request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
}