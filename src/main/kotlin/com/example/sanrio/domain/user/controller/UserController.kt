package com.example.sanrio.domain.user.controller

import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.dto.request.UpdatePasswordRequest
import com.example.sanrio.domain.user.service.UserService
import com.example.sanrio.global.jwt.UserPrincipal
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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

    @Description("프로필 조회")
    @GetMapping("/users/{userId}")
    fun getUserProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable userId: Long,
    ) = userService.getUserProfile(userPrincipal = userPrincipal, userId = userId)
        .let { ResponseEntity.ok().body(it) }

    @Description("비밀번호 변경")
    @PutMapping("/users/{userId}/password")
    fun updatePassword(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable userId: Long,
        @Valid @RequestBody request: UpdatePasswordRequest,
        response: HttpServletResponse
    ) = userService.updatePassword(
        userPrincipal = userPrincipal,
        userId = userId,
        request = request,
        response = response
    ).let { ResponseEntity.ok().body(it) }
}