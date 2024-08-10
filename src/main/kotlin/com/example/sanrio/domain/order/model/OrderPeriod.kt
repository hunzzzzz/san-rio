package com.example.sanrio.domain.order.model

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.EnumUtils
import org.springframework.context.annotation.Description

enum class OrderPeriod {
    ONE_MONTH, THREE_MONTH, ONE_YEAR;

    companion object {
        @Description("JSON으로 입력받은 문자열을 파싱하여 ProductStatus로 변환")
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(name: String?): OrderPeriod? =
            name?.let { EnumUtils.getEnumIgnoreCase(OrderPeriod::class.java, it.trim()) }
    }
}