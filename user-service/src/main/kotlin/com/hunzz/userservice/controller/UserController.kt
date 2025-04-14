package com.hunzz.userservice.controller

import com.hunzz.userservice.dto.request.UpdatePasswordRequest
import com.hunzz.userservice.dto.response.UserResponse
import com.hunzz.userservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("{userId}")
    fun get(
        @PathVariable userId: Long
    ): ResponseEntity<UserResponse> {
        val user = userService.get(userId = userId)

        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @PutMapping("/{userId}")
    fun updatePassword(
        @PathVariable userId: Long,
        @RequestBody request: UpdatePasswordRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updatePassword(userId = userId, request = request)

        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}