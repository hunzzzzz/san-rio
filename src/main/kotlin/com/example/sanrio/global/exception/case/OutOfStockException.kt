package com.example.sanrio.global.exception.case

class OutOfStockException(stock: Int) : ItemException("상품 재고가 부족합니다. 현재 잔여 수량은 ${stock}개입니다.")