package com.example.sanrio.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.encrypt.AesBytesEncryptor

@Configuration
class EncryptionConfig(
    @Value("\${encryptor.symmetric}") private val symmetric: String,
    @Value("\${encryptor.salt}") private val salt: String
) {
    @Description("비밀번호 단방향 암호화를 위한 BCryptPasswordEncoder")
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Description("상세 주소 양방향 암호화를 위한 AesBytesEncryptor")
    @Bean
    fun aesBytesEncryptor() = AesBytesEncryptor(symmetric, salt)
}