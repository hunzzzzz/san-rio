package com.hunzz.productservice.utility.exception

import com.hunzz.productservice.utility.exception.custom.ProductNotFoundException
import com.hunzz.userservice.utility.exception.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(value = [ProductNotFoundException::class])
    fun handleProductNotFoundException(e: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST,
            message = e.message ?: ""
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}