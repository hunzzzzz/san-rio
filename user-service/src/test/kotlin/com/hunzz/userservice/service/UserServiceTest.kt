package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.LoginRequest
import com.hunzz.userservice.dto.request.SignupRequest
import com.hunzz.userservice.dto.request.UpdateUserRequest
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `signup 성공 시 저장된 User 객체를 UserResponse로 변환하여 반환한다`() {
        // given
        val request = SignupRequest(
            loginId = "testId",
            password = "testPassword",
            name = "testUser"
        )
        val savedUser = User(
            id = 1L,
            loginId = request.loginId,
            password = request.password,
            name = request.name
        )

        every { userRepository.save(any()) } returns savedUser

        // when
        val response = userService.signup(request)

        // then
        verify(exactly = 1) { userRepository.save(any()) }
        assertEquals(savedUser.id, response.userId)
        assertEquals(savedUser.loginId, response.loginId)
        assertEquals(savedUser.name, response.name)
    }

    @Test
    fun `update 성공 시 변경된 User 객체를 UserResponse로 변환하여 반환한다`() {
        // given
        val userId = 1L
        val request = UpdateUserRequest(
            name = "newName",
            password = "newPassword",
        )
        val savedUser = User(
            id = userId,
            loginId = "testId",
            password = "oldPassword",
            name = "oldName"
        )

        every { userRepository.findByIdOrNull(userId) } returns savedUser

        // when
        val response = userService.update(userId = userId, request = request)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(userId) }
        assertEquals(savedUser.id, response.userId)
        assertEquals(savedUser.loginId, response.loginId)
        assertEquals(savedUser.name, response.name)
        assertEquals(savedUser.password, request.password)
    }

    @Test
    fun `update 시 userId가 유효하지 않으면 Exception을 throw한다`() {
        // given
        val userId = 1L
        val request = UpdateUserRequest(
            name = "newName",
            password = "newPassword",
        )

        every { userRepository.findByIdOrNull(userId) } returns null

        // expected
        assertThrows<IllegalStateException> {
            userService.update(userId = userId, request = request)
        }
    }

    @Test
    fun `로그인 성공 시 해당 User 객체를 UserResponse로 변환하여 반환한다`() {
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

        every { userRepository.findByLoginId(loginId = loginId) } returns savedUser

        // when
        val response = userService.login(request = request)

        // then
        verify(exactly = 1) { userRepository.findByLoginId(loginId = loginId) }
        assertEquals(savedUser.id, response.userId)
        assertEquals(savedUser.loginId, response.loginId)
        assertEquals(savedUser.name, response.name)
    }

    @Test
    fun `로그인 시 아이디가 일치하지 않으면 Exception을 throw한다`() {
        // given
        val request = LoginRequest(
            loginId = "wrongId",
            password = "testPassword"
        )

        every { userRepository.findByLoginId(loginId = request.loginId) } returns null

        // expected
        assertThrows<IllegalStateException> {
            userService.login(request = request)
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

        every { userRepository.findByLoginId(loginId = loginId) } returns savedUser

        // expected
        assertThrows<IllegalStateException> {
            userService.login(request = request)
        }
    }
}