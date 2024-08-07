package com.example.sanrio.domain.character.repository

import com.example.sanrio.domain.character.model.Character
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CharacterRepository : JpaRepository<Character, Long>