package com.example.sanrio.domain.character.controller

import com.example.sanrio.domain.character.service.CharacterService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/characters")
class CharacterController(
    private val characterService: CharacterService
) {
}