package com.example.sanrio.domain.order.dto.request

import com.example.sanrio.domain.order.model.Order
import com.example.sanrio.domain.order.model.Recall
import com.example.sanrio.domain.order.model.RecallReason
import jakarta.validation.constraints.NotBlank

data class RecallRequest(
    @field:NotBlank(message = "반품 사유를 선택해주세요.")
    val reason: String?,

    val detail: String?
) {
    fun to(order: Order) = Recall(
        reason = RecallReason.valueOf(this.reason!!),
        detail = this.detail,
        order = order
    )
}