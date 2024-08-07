package com.example.sanrio.global.exception.case

class DuplicatedValueException(value: String) : RuntimeException("이미 존재하는 ${value}입니다.")