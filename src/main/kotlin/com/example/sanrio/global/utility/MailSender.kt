package com.example.sanrio.global.utility

import org.springframework.context.annotation.Description
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class MailSender(
    private val mailSender: JavaMailSender
) {
    @Description("이메일 전송")
    @Async
    fun sendEmail(email: String, subject: String, text: String) =
        kotlin.runCatching {
            val message = mailSender.createMimeMessage()

            MimeMessageHelper(message, false, "UTF-8")
                .let {
                    it.setTo(email) // 수신자의 메일 주소
                    it.setSubject(subject) // 메일 제목
                    it.setText(text) // 메일 내용
                }

            mailSender.send(message)
        }
}