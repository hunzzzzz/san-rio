package com.example.sanrio.global.utility

import com.example.sanrio.domain.product.model.CharacterName
import java.time.LocalDateTime

object OrderCodeGenerator {
    private val characterCode = hashMapOf(
        CharacterName.HELLO_KITTY to 'H',
        CharacterName.MY_MELODY to 'M',
        CharacterName.HAN_GYODON to 'N',
        CharacterName.KEROPPI to 'I',
        CharacterName.POCHACCO to 'O',
        CharacterName.POMPOM_PURIN to 'P',
        CharacterName.CINNAMOROLL to 'C',
        CharacterName.KUROMI to 'K'
    )

    fun generateOrderCode(characterName: CharacterName) = LocalDateTime.now().let { now ->
        "${now.year - 2000}${"%02d".format(now.monthValue)}${"%02d".format(now.dayOfMonth)}${characterCode[characterName]}${(10000..99999).random()}"
    }
}