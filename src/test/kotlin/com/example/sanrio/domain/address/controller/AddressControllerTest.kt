package com.example.sanrio.domain.address.controller

import com.example.sanrio.domain.address.dto.request.AddressRequest
import com.example.sanrio.domain.address.model.Address
import com.example.sanrio.domain.address.repository.AddressRepository
import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.jwt.AuthenticationHelper
import com.example.sanrio.global.auth.WithCustomMockUser
import com.example.sanrio.global.utility.Encryptor
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.NicknameGenerator.generateNickname
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var authenticationHelper: AuthenticationHelper

    @Autowired
    lateinit var entityFinder: EntityFinder

    @Autowired
    lateinit var encryptor: Encryptor

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var addressRepository: AddressRepository

    @Autowired
    lateinit var cartRepository: CartRepository

    @AfterEach
    fun clean() {
        cartRepository.deleteAll()
        addressRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @WithCustomMockUser
    fun 주소를_처음으로_추가하는_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val request = AddressRequest(zipCode = "00000", streetAddress = "테스트 도로명 주소", detailAddress = "테스트 상세 주소")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/users/${user.id}/address")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isCreated)
            .andDo(print())

        addressRepository.findByUser(user = user).let {
            assertThat(it.size).isEqualTo(1)
            assertThat(it.first().zipCode).isEqualTo(request.zipCode)
            assertThat(it.first().streetAddress).isEqualTo(request.streetAddress)
            assertThat(encryptor.decrypt(it.first().detailAddress)).isEqualTo(request.detailAddress)
            assertThat(it.first().default).isTrue()
        }
    }

    @Test
    @WithCustomMockUser
    fun 주소를_하나_더_추가하는_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        setAddress(user = user)

        val request =
            AddressRequest(zipCode = "11111", streetAddress = "두 번째 테스트 도로명 주소", detailAddress = "두 번째 테스트 상세 주소")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/users/${user.id}/address")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isCreated)
            .andDo(print())

        addressRepository.findByUser(user = user).let {
            assertThat(it.size).isEqualTo(2)
            assertThat(it.map { address -> address.default }.count { default -> default }).isEqualTo(1)
        }
    }

    @Test
    @WithCustomMockUser
    fun 주소_추가_시_특정_필드를_입력하지_않은_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val request = AddressRequest(zipCode = "00000", streetAddress = "", detailAddress = "테스트 상세 주소")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/users/${user.id}/address")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("도로명 주소를 입력해주세요."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 올바르지_않은_형식의_우편번호를_입력한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val request = AddressRequest(zipCode = "123", streetAddress = "테스트 도로명 주소", detailAddress = "테스트 상세 주소")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/users/${user.id}/address")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("일치하지 않는 우편번호입니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 다른_유저의_프로필에_주소를_추가하려는_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val anotherUser = setUser()
        val request = AddressRequest(zipCode = "123", streetAddress = "테스트 도로명 주소", detailAddress = "테스트 상세 주소")
        val json = objectMapper.writeValueAsString(request)

        // expected
        mockMvc.perform(
            post("/users/${anotherUser.id}/address")
                .contentType(APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
            .andExpect(jsonPath("$.statusCode").value("403 Forbidden"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 정상적으로_기본_주소를_변경한_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val defaultAddress = setAddress(user = user)
        val newAddress = setAddress(
            user = user,
            streetAddress = "두 번째 테스트 도로명 주소",
            detailAddress = "두 번째 테스트 상세 주소",
            isFirst = false
        )

        // expected
        mockMvc.perform(
            put("/users/${user.id}/address/${newAddress.id}/default")
                .contentType(APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andDo(print())

        assertThat(entityFinder.findAddressById(addressId = defaultAddress.id!!).default).isFalse()
        assertThat(entityFinder.findAddressById(addressId = newAddress.id!!).default).isTrue()
    }

    @Test
    @WithCustomMockUser
    fun 해당_주소가_이미_기본_주소로_설정된_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val address = setAddress(user = user)

        // expected
        mockMvc.perform(
            put("/users/${user.id}/address/${address.id}/default")
                .contentType(APPLICATION_JSON)
        ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("이미 기본 주소로 설정되어 있습니다."))
            .andExpect(jsonPath("$.statusCode").value("400 Bad Request"))
            .andDo(print())
    }

    @Test
    @WithCustomMockUser
    fun 다른_유저의_주소를_기본주소로_설정하려는_경우() {
        // given
        val user = entityFinder.findUserById(authenticationHelper.getCurrentUser().id)
        val anotherUser = setUser()
        val address = setAddress(user = anotherUser)

        // expected
        mockMvc.perform(
            put("/users/${anotherUser.id}/address/${address.id}/default")
                .contentType(APPLICATION_JSON)
        ).andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("권한이 없습니다."))
            .andExpect(jsonPath("$.statusCode").value("403 Forbidden"))
            .andDo(print())
    }

    private fun setUser() =
        User(
            email = "test2@gmail.com",
            password = passwordEncoder.encode("Test1234!"),
            name = "테스트 계정",
            nickname = generateNickname(),
            phone = "010-1234-5678"
        ).let { userRepository.save(it) }

    private fun setAddress(
        user: User,
        streetAddress: String = "테스트 도로명 주소",
        detailAddress: String = "테스트 상세 주소",
        isFirst: Boolean = true
    ) =
        Address(
            zipCode = "00000",
            streetAddress = streetAddress,
            detailAddress = encryptor.encrypt(detailAddress),
            default = isFirst,
            user = user
        ).let { addressRepository.save(it) }
}