package com.example.sanrio.domain.address.controller

import com.example.sanrio.domain.address.dto.request.AddressRequest
import com.example.sanrio.domain.address.repository.AddressRepository
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.auth.AuthenticationHelper
import com.example.sanrio.global.auth.WithCustomMockUser
import com.example.sanrio.global.utility.Encryptor
import com.example.sanrio.global.utility.EntityFinder
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
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
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var addressRepository: AddressRepository

    @AfterEach
    fun clean() {
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
}