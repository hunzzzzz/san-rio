package com.example.sanrio.global.exception.case

class InvalidValueException(value: String) : RuntimeException("일치하지 않는 ${value}입니다.")