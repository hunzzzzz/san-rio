package com.example.sanrio.domain.character.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "characters")
class Character(
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "debut", nullable = false)
    val debut: LocalDate
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id", nullable = false, unique = true)
    val id: Long? = null
}