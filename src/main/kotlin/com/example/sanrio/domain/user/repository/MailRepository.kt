package com.example.sanrio.domain.user.repository

import com.example.sanrio.domain.user.model.Mail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MailRepository : JpaRepository<Mail, Long>