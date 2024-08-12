package com.example.sanrio.domain.address.controller

import com.example.sanrio.domain.address.dto.request.AddressRequest
import com.example.sanrio.domain.address.service.AddressService
import com.example.sanrio.global.jwt.UserPrincipal
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AddressController(
    private val addressService: AddressService
) {
    @Description("주소 추가")
    @PostMapping("/users/{userId}/address")
    fun setAddress(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable userId: Long,
        @Valid @RequestBody request: AddressRequest
    ) = addressService.setAddress(userPrincipal = userPrincipal, userId = userId, request = request)
        .let { ResponseEntity.ok().body(it) }
}