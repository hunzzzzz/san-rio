package com.example.sanrio.global.exception

import com.example.sanrio.global.exception.case.DuplicatedValueException
import com.example.sanrio.global.exception.case.InvalidValueException
import com.example.sanrio.global.exception.case.LoginException
import com.example.sanrio.global.exception.case.ModelNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    // Validation 실패
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException) =
        ErrorResponse(message = e.fieldErrors.first().defaultMessage!!, statusCode = "400 Bad Request")

    // 두 value가 일치하지 않는 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidValueException::class)
    fun handleInvalidValueException(e: InvalidValueException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // 중복하는 value가 존재하는 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicatedValueException::class)
    fun handleDuplicatedValueException(e: DuplicatedValueException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // id에 해당하는 모델(엔티티)이 없는 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ModelNotFoundException::class)
    fun handleModelNotFoundException(e: ModelNotFoundException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // 로그인 시 이메일 혹은 비밀번호를 잘못 입력한 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LoginException::class)
    fun handleLoginException(e: LoginException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")
}