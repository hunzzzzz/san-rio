package com.example.sanrio.domain.product.model

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.EnumUtils
import org.springframework.context.annotation.Description

enum class CharacterName(val debutYear: Int) {
    HELLO_KITTY(1974),
    MY_MELODY(1975),
    HAN_GYODON(1985),
    KEROPPI(1988),
    POCHACCO(1989),
    POMPOM_PURIN(1996),
    CINNAMOROLL(2001),
    KUROMI(2005);

    companion object {
        @Description("JSON으로 입력받은 문자열을 파싱하여 ProductStatus로 변환")
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(name: String?): CharacterName? =
            name?.let { EnumUtils.getEnumIgnoreCase(CharacterName::class.java, it.trim()) }
    }
}