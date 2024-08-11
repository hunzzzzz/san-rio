package com.example.sanrio.domain.auth.controller

import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.user.dto.request.LoginRequest
import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URLDecoder

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var cartRepository: CartRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @AfterEach
    fun clean() {
        cartRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun 정상적으로_로그인에_성공한_경우() {
        // given
        val user = getUser()
        val request = LoginRequest(
            email = "test@gmail.com",
            password = "Test1234!"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        val result = mockMvc.perform(
            post("/login")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.cookie().exists(COOKIE_NAME))
            .andDo(print())
            .andReturn()

        val cookie = result.response.getCookie(COOKIE_NAME)
        assertThat(cookie).isNotNull
        assertThat(URLDecoder.decode(cookie!!.value, "UTF-8")).startsWith(BEARER_PREFIX)
    }

    private fun getUser() = SignUpRequest(
        email = "test@gmail.com",
        password = "Test1234!",
        password2 = "Test1234!",
        name = "테스트 계정"
    ).to(passwordEncoder = passwordEncoder)
        .let { userRepository.save(it) }

    @Test
    fun 존재하지_않는_이메일로_로그인을_시도한_경우() {
        // given
        val user = getUser()
        val request = LoginRequest(
            email = "null@gmail.com",
            password = "Test1234!"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/login")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("아이디 혹은 비밀번호가 잘못되었습니다. 다시 확인해주세요."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 비밀번호가_일치하지_않는_경우() {
        // given
        val user = getUser()
        val request = LoginRequest(
            email = "test@gmail.com",
            password = "Null1234!"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/login")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("아이디 혹은 비밀번호가 잘못되었습니다. 다시 확인해주세요."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    companion object {
        private const val COOKIE_NAME = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}