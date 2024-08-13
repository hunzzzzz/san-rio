package com.example.sanrio.domain.user.controller

import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.jwt.AuthenticationHelper
import com.example.sanrio.global.auth.WithCustomMockUser
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.NicknameGenerator.generateNickname
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
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
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var cartRepository: CartRepository

    @AfterEach
    fun clean() {
        cartRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun 정상적으로_회원가입에_성공한_경우() {
        // given
        val request = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234!",
            password2 = "Test1234!",
            name = "테스트 계정",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isCreated)
            .andDo(print())
    }

    @Test
    fun 회원가입시_이메일_형식을_잘못_입력한_경우() {
        // given
        val request = SignUpRequest(
            email = "test",
            password = "Test1234!",
            password2 = "Test1234!",
            name = "테스트 계정",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("올바르지 않은 이메일 형식입니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 회원가입시_패스워드_형식을_잘못_입력한_경우() {
        // given
        val request = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234",
            password2 = "Test1234",
            name = "테스트 계정",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("올바르지 않은 비밀번호 형식입니다. (8~16자의 알파벳 대소문자, 숫자, 특수문자로 구성)"))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 회원가입시_입력한_두개의_패스워드가_일치하지_않는_경우() {
        // given
        val request = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234!",
            password2 = "Test12345!",
            name = "테스트 계정",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("두 비밀번호가 일치하지 않습니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 회원가입시_휴대폰번호_형식을_잘못_입력한_경우() {
        // given
        val request = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234!",
            password2 = "Test1234!",
            name = "테스트 계정",
            phone = "010-123-456"
        )

        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("올바르지 않은 휴대폰번호 형식입니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 회원가입시_특정_필드를_입력하지_않은_경우() {
        // given
        val request = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234!",
            password2 = "Test1234!",
            name = "",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("이름은 필수 입력 항목입니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 이미_사용중인_이메일로_회원가입을_시도하는_경우() {
        // given
        val user = getUser()

        val request = SignUpRequest(
            email = user.email,
            password = "Test1234!",
            password2 = "Test1234!",
            name = "테스트 계정",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=true")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    fun 본인인증이_완료되지_않은_상태에서_회원가입을_시도하는_경우() {
        // given
        val request = SignUpRequest(
            email = "test@gmail.com",
            password = "Test1234!",
            password2 = "Test1234!",
            name = "테스트 계정",
            phone = "010-1234-5678"
        )
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/signup?isIdentified=false")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("본인인증이 완료되지 않았습니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 정상적으로_프로필_조회에_성공한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)

        // expected
        mockMvc.perform(
            get("/users/${user.id}")
                .contentType(APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(user.id))
            .andExpect(jsonPath("$.email").value(user.email))
            .andExpect(jsonPath("$.name").value(user.name))
            .andExpect(jsonPath("$.nickname").value(user.nickname))
            .andExpect(jsonPath("$.phone").value(user.phone))
            .andExpect(jsonPath("$.point").value(user.point))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 다른_회원의_프로필을_조회하려는_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val anotherUser = getUser()

        // expected
        mockMvc.perform(
            get("/users/${anotherUser.id}")
                .contentType(APPLICATION_JSON)
        ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
            .andExpect(jsonPath("$.statusCode").value("403 Forbidden"))
            .andDo(print())
    }

    private fun getUser() = User(
        email = "test2@gmail.com",
        password = passwordEncoder.encode("Test1234!"),
        name = "테스트 계정",
        nickname = generateNickname(),
        phone = "010-1234-5678"
    ).let { userRepository.save(it) }
}
