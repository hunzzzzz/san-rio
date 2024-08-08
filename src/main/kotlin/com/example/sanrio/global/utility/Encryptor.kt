package com.example.sanrio.global.utility

import org.springframework.context.annotation.Description
import org.springframework.security.crypto.encrypt.AesBytesEncryptor
import org.springframework.stereotype.Service

@Service
class Encryptor(
    private val encryptor: AesBytesEncryptor
) {
    @Description("암호화")
    fun encrypt(value: String): ByteArray = encryptor.encrypt(value.toByteArray(Charsets.UTF_8))

    @Description("복호화")
    fun decrypt(encryptedValue: ByteArray) = String(encryptor.decrypt(encryptedValue))
}