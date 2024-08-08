package com.example.sanrio.global.exception.case

class ModelNotFoundException(model: String) : RuntimeException("존재하지 않는 ${model}입니다.")