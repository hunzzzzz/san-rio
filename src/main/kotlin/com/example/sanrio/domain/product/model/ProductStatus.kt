package com.example.sanrio.domain.product.model

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.EnumUtils
import org.springframework.context.annotation.Description

enum class ProductStatus {
    SALE, SOLD_OUT;

    companion object {
        @Description("JSON으로 입력받은 문자열을 파싱하여 ProductStatus로 변환")
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(name: String?): ProductStatus? =
            name?.let { EnumUtils.getEnumIgnoreCase(ProductStatus::class.java, it.trim()) }
    }
}