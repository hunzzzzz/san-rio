package com.example.sanrio.global.exception

import com.example.sanrio.global.exception.case.*
import org.springframework.http.HttpStatus
import org.springframework.mail.MailSendException
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

    // 회원가입 관련 예외 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SignUpException::class)
    fun handleSignUpException(e: SignUpException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

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

    // 권한이 없는 요청을 한 경우
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException) =
        ErrorResponse(message = e.message!!, statusCode = "403 Forbidden")

    // 로그인 시 이메일 혹은 비밀번호를 잘못 입력한 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LoginException::class)
    fun handleLoginException(e: LoginException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // 장바구니 상품 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ItemException::class)
    fun handleOutOfStockException(e: ItemException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // 주문 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OrderException::class)
    fun handleOutOfStockException(e: OrderException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // 메일 전송 관련 에러
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MailSendException::class)
    fun handleMailSendException(e: MailSendException) =
        ErrorResponse(message = e.message!!, statusCode = "500 Internal Server Error")

    // 본인인증 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(VerificationException::class)
    fun handleVerificationException(e: VerificationException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")

    // JWT 토큰 관련 에러
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JwtTokenException::class)
    fun handleJwtTokenException(e: JwtTokenException) =
        ErrorResponse(message = e.message!!, statusCode = "400 Bad Request")
}