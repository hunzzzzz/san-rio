package com.example.sanrio.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig(
    @Value("\${spring.mail.host}")
    private val host: String,

    @Value("\${spring.mail.port}")
    private val port: Int,

    @Value("\${spring.mail.username}")
    private val userName: String,

    @Value("\${spring.mail.password}")
    private val password: String
) {
    @Bean
    @Description("이메일 전송")
    fun sendMail() =
        JavaMailSenderImpl().let {
            it.host = host // 이메일 서버의 호스트 주소
            it.port = port // 이메일 서버의 포트 번호
            it.username = userName // 이메일 계정의 사용자 이름
            it.password = password // 이메일 계정의 비밀번호
            it.javaMailProperties.let { properties ->
                properties["mail.smtp.auth"] = true // SMTP 인증 사용
                properties["mail.smtp.debug"] = true // SMTP 디버그 모드를 사용
                properties["mail.smtp.starttls.enable"] = true // TLS를 사용하여 이메일 전송
                properties["mail.smtp.connectiontimeout"] = 1000 // SMTP 연결 시간 (1초)
            }
            it
        }
}