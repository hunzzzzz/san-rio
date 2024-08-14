package com.example.sanrio.domain.auth.controller

import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.order.controller.OrderControllerTest.Companion.ATK_EXPIRATION_TIME
import com.example.sanrio.domain.order.controller.OrderControllerTest.Companion.RTK_EXPIRATION_TIME
import com.example.sanrio.domain.user.dto.request.LoginRequest
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.auth.WithCustomMockUser
import com.example.sanrio.global.jwt.AuthenticationHelper
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.JwtProvider
import com.example.sanrio.global.utility.NicknameGenerator.generateNickname
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URLDecoder

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var entityFinder: EntityFinder

    @Autowired
    lateinit var authenticationHelper: AuthenticationHelper

    @Autowired
    lateinit var jwtProvider: JwtProvider

    @Autowired
    lateinit var response: HttpServletResponse

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var cartRepository: CartRepository

    @AfterEach
    fun clean() {
        cartRepository.deleteAll()
        userRepository.deleteAll()
        jwtProvider.deleteCookie(response = response)
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
            .andExpect(cookie().exists(COOKIE_NAME_ATK))
            .andExpect(cookie().exists(COOKIE_NAME_RTK))
            .andDo(print())
            .andReturn()

        val cookieAtk = result.response.getCookie(COOKIE_NAME_ATK)
        assertThat(cookieAtk).isNotNull
        assertThat(URLDecoder.decode(cookieAtk!!.value, "UTF-8")).startsWith(BEARER_PREFIX)

        val cookieRtk = result.response.getCookie(COOKIE_NAME_RTK)
        assertThat(cookieRtk).isNotNull
        assertThat(URLDecoder.decode(cookieRtk!!.value, "UTF-8")).startsWith(BEARER_PREFIX)
    }

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

    @Test
    @WithCustomMockUser
    fun 정상적으로_로그아웃에_성공한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)

        // expected
        val result = mockMvc.perform(
            get("/logout")
                .contentType(APPLICATION_JSON)
                .cookie(setAtkCookie(user = user))
                .cookie(setRtkCookie(user = user))
        ).andExpect(status().isOk)
            .andDo(print())
            .andReturn()

        val cookieAtk = result.response.getCookie(COOKIE_NAME_ATK)!!
        val cookieRtk = result.response.getCookie(COOKIE_NAME_RTK)!!

        assertThat(cookieAtk.maxAge).isEqualTo(0)
        assertThat(cookieRtk.maxAge).isEqualTo(0)
    }

    private fun setAtkCookie(user: User) =
        jwtProvider.getAccessToken(userId = user.id!!, email = user.email, role = user.role)
            .let { atk ->
                Cookie("AccessToken", atk).let {
                    it.path = "/"
                    it.maxAge = ATK_EXPIRATION_TIME
                    it
                }
            }

    private fun setRtkCookie(user: User) =
        jwtProvider.getAccessToken(userId = user.id!!, email = user.email, role = user.role)
            .let { rtk ->
                Cookie("RefreshToken", rtk).let {
                    it.path = "/"
                    it.maxAge = RTK_EXPIRATION_TIME
                    it
                }
            }

    private fun getUser() = User(
        email = "test@gmail.com",
        password = passwordEncoder.encode("Test1234!"),
        name = "테스트 계정",
        nickname = generateNickname(),
        phone = "010-1234-5678"
    ).let { userRepository.save(it) }

    companion object {
        private const val COOKIE_NAME_ATK = "AccessToken"
        private const val COOKIE_NAME_RTK = "RefreshToken"
        private const val BEARER_PREFIX = "Bearer "
    }
}