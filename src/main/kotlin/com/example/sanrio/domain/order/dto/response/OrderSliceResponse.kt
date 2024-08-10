package com.example.sanrio.domain.order.dto.response

data class OrderSliceResponse(
    val size: Int,
    val numberOfElements: Int,
    val contents: List<OrderDataResponse>
)