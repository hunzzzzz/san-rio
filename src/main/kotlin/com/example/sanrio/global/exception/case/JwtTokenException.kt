package com.example.sanrio.global.exception.case

class JwtTokenException(message: String = "유효하지 않은 JWT 토큰입니다.") : RuntimeException(message)