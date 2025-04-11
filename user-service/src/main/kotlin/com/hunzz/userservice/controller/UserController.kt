package com.hunzz.userservice.controller

import com.hunzz.userservice.dto.request.LoginRequest
import com.hunzz.userservice.dto.request.SignupRequest
import com.hunzz.userservice.dto.request.UpdateUserRequest
import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignupRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.signup(request = request)

        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PutMapping("/users/{userId}")
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.update(userId = userId, request = request)

        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.login(request = request)

        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}