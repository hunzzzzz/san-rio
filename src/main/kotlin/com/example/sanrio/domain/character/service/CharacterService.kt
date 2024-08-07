package com.example.sanrio.domain.character.service

import com.example.sanrio.domain.character.repository.CharacterRepository
import org.springframework.stereotype.Service

@Service
class CharacterService(
    private val characterRepository: CharacterRepository
) {
}