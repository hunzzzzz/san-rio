package com.example.sanrio.domain.address.controller

import com.example.sanrio.domain.address.dto.request.AddressRequest
import com.example.sanrio.domain.address.service.AddressService
import com.example.sanrio.global.jwt.UserPrincipal
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

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
        .let { ResponseEntity.created(URI.create("/users/${userPrincipal.id}")).body(it) }

    @Description("기본 주소로 설정")
    @PutMapping("/users/{userId}/address/{addressId}/default")
    fun setDefaultAddress(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable userId: Long,
        @PathVariable addressId: Long
    ) = addressService.setDefaultAddress(userPrincipal = userPrincipal, userId = userId, addressId = addressId)
        .let { ResponseEntity.ok().body(it) }
}