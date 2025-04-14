package com.hunzz.userservice.service

import com.hunzz.userservice.dto.request.SignupRequest
import com.hunzz.userservice.entity.User
import com.hunzz.userservice.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class SignupServiceTest {
    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var signupService: SignupService

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

        every { userRepository.existsByLoginId(any()) } returns false
        every { userRepository.save(any()) } returns savedUser

        // when
        val response = signupService.signup(request)

        // then
        verify(exactly = 1) { userRepository.existsByLoginId(any()) }
        verify(exactly = 1) { userRepository.save(any()) }
        assertEquals(savedUser.id, response.userId)
        assertEquals(savedUser.loginId, response.loginId)
        assertEquals(savedUser.name, response.name)
    }
}