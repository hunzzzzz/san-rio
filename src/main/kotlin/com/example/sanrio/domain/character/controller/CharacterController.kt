package com.example.sanrio.domain.character.controller

import com.example.sanrio.domain.character.dto.request.AddCharacterRequest
import com.example.sanrio.domain.character.service.CharacterService
import jakarta.validation.Valid
import org.springframework.context.annotation.Description
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/characters")
class CharacterController(
    private val characterService: CharacterService
) {
    @Description("캐릭터 추가")
    @PostMapping
    fun addCharacter(
        @Valid @RequestBody request: AddCharacterRequest
    ) = ResponseEntity.ok().body(characterService.addCharacter(request = request))
}