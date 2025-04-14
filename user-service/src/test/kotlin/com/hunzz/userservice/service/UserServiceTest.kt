package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.UpdatePasswordRequest
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import com.hunzz.userservice.utility.exception.custom.UserNotFoundException
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
    fun `프로필 조회 요청 시 User 객체를 UserResponse로 변환하여 반환한다`() {
        // given
        val userId = 1L
        val savedUser = User(
            id = userId,
            loginId = "testId",
            password = "oldPassword",
            name = "oldName"
        )

        every { userRepository.findByIdOrNull(any()) } returns savedUser

        // when
        val response = userService.get(userId = userId)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
        assertEquals(savedUser.id, response.userId)
        assertEquals(savedUser.loginId, response.loginId)
        assertEquals(savedUser.name, response.name)

    }

    @Test
    fun `update 성공 시 User 객체를 UserResponse로 변환하여 반환한다`() {
        // given
        val userId = 1L
        val request = UpdatePasswordRequest(
            newPassword = "newPassword",
        )
        val savedUser = User(
            id = userId,
            loginId = "testId",
            password = "oldPassword",
            name = "oldName"
        )

        every { userRepository.findByIdOrNull(any()) } returns savedUser

        // when
        val response = userService.updatePassword(userId = userId, request = request)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(any()) }
        assertEquals(savedUser.id, response.userId)
        assertEquals(savedUser.loginId, response.loginId)
        assertEquals(savedUser.name, response.name)
        assertEquals(savedUser.password, request.newPassword)
    }

    @Test
    fun `update 시 userId가 유효하지 않으면 Exception을 throw한다`() {
        // given
        val userId = 1L
        val request = UpdatePasswordRequest(
            newPassword = "newPassword",
        )

        every { userRepository.findByIdOrNull(any()) } returns null

        // expected
        assertThrows<UserNotFoundException> {
            userService.updatePassword(userId = userId, request = request)
        }
    }
}