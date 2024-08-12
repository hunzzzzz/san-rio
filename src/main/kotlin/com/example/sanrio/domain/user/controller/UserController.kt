package com.example.sanrio.domain.user.controller

import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.service.UserService
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class UserController(
    private val userService: UserService
) {
    @Description("회원가입")
    @PostMapping("/signup")
    fun signup(
        @RequestParam isIdentified: Boolean,
        @Valid @RequestBody request: SignUpRequest
    ) = userService.signup(isIdentified = isIdentified, request = request)
        .let { ResponseEntity.created(URI.create("/login")).body(it) }

    @Description("인증번호 메일 전송")
    @GetMapping("/signup/code")
    fun sendVerificationMail(
        @RequestParam email: String
    ) = userService.sendVerificationEmail(email = email)
        .let { ResponseEntity.ok().body(it) }

    @Description("인증번호 일치 여부 확인")
    @GetMapping("/signup/code-check")
    fun checkVerificationCode(
        @RequestParam email: String,
        @RequestParam code: String
    ) = userService.checkVerificationCode(email = email, code = code)
        .let { ResponseEntity.ok().body(it) }
}