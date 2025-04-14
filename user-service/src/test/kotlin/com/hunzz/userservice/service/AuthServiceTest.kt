package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.LoginRequest
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import com.hunzz.userservice.utility.auth.JwtProvider
import com.hunzz.userservice.utility.exception.custom.LoginException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class AuthServiceTest {
    @MockK
    private lateinit var jwtProvider: JwtProvider

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var authService: AuthService

    @Test
    fun `로그인 성공 시 ATK와 RTK를 생성하여 TokenResponse로 반환한다`() {
        // given
        val loginId = "testId"
        val request = LoginRequest(
            loginId = loginId,
            password = "testPassword"
        )
        val savedUser = User(
            id = 1L,
            loginId = loginId,
            password = request.password,
            name = "testUser"
        )
        val testAtk = "Bearer abcd1234"
        val testRtk = "Bearer wxyz5678"

        every { userRepository.findByLoginId(any()) } returns savedUser
        every { jwtProvider.createAtk(any()) } returns testAtk
        every { jwtProvider.createRtk(any()) } returns testRtk

        // when
        val response = authService.login(request = request)

        // then
        verify(exactly = 1) { userRepository.findByLoginId(any()) }
        verify(exactly = 1) { jwtProvider.createAtk(any()) }
        verify(exactly = 1) { jwtProvider.createRtk(any()) }
        assertTrue { response.atk.startsWith("Bearer") }
        assertTrue { response.rtk.startsWith("Bearer") }
    }

    @Test
    fun `로그인 시 아이디가 일치하지 않으면 Exception을 throw한다`() {
        // given
        val request = LoginRequest(
            loginId = "wrongId",
            password = "testPassword"
        )

        every { userRepository.findByLoginId(any()) } returns null

        // expected
        org.junit.jupiter.api.assertThrows<LoginException> {
            authService.login(request = request)
        }
    }

    @Test
    fun `로그인 시 비밀번호가 일치하지 않으면 Exception을 throw한다`() {
        // given
        val loginId = "testId"
        val request = LoginRequest(
            loginId = loginId,
            password = "wrongPassword"
        )
        val savedUser = User(
            id = 1L,
            loginId = loginId,
            password = "testPassword",
            name = "testUser"
        )

        every { userRepository.findByLoginId(any()) } returns savedUser

        // expected
        org.junit.jupiter.api.assertThrows<LoginException> {
            authService.login(request = request)
        }
    }
}