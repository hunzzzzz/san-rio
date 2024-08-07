package com.example.sanrio.domain.character.service

import com.example.sanrio.domain.character.dto.request.AddCharacterRequest
import com.example.sanrio.domain.character.repository.CharacterRepository
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service

@Service
class CharacterService(
    private val characterRepository: CharacterRepository
) {
    @Description("캐릭터 추가")
    fun addCharacter(request: AddCharacterRequest) =
        request.to()
            .let { characterRepository.save(it) } // DTO -> 엔티티
            .let { } // 리턴값 X
}