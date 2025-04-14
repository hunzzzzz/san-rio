package com.hunzz.userservice.utility.exception

import com.hunzz.userservice.utility.exception.custom.InvalidUserInfoException
import com.hunzz.userservice.utility.exception.custom.LoginException
import com.hunzz.userservice.utility.exception.custom.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(value = [UserNotFoundException::class])
    fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST,
            message = e.message ?: ""
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(value = [LoginException::class])
    fun handleLoginException(e: LoginException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST,
            message = e.message ?: ""
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(value = [InvalidUserInfoException::class])
    fun handleInvalidUserInfoException(e: InvalidUserInfoException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            statusCode = HttpStatus.BAD_REQUEST,
            message = e.message ?: ""
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}