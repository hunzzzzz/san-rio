package com.example.sanrio.domain.user.service

import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.user.dto.request.AddressRequest
import com.example.sanrio.domain.user.dto.request.LoginRequest
import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.AddressRepository
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.DuplicatedValueException
import com.example.sanrio.global.exception.case.InvalidValueException
import com.example.sanrio.global.exception.case.LoginException
import com.example.sanrio.global.utility.Encryptor
import com.example.sanrio.global.utility.EntityFinder
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Description
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val encryptor: Encryptor,
    private val passwordEncoder: PasswordEncoder,
    private val entityFinder: EntityFinder
) {
    @Description("회원가입 시 입력한 이메일에 대한 중복 체크")
    private fun validateEmailDuplication(email: String) =
        if (userRepository.existsByEmail(email)) throw DuplicatedValueException("이메일") else Unit

    @Description("회원가입 시 입력한 두 비밀번호가 일치하는지 체크")
    private fun validateTwoPasswords(password1: String, password2: String) =
        if (password1 != password2) throw InvalidValueException("비밀번호") else Unit

    @Description("회원가입 시 해당 회원 소유의 장바구니를 생성")
    private fun makeCart(user: User) = Cart(user = user).let { cartRepository.save(it) }

    @Description("회원가입")
    fun signup(request: SignUpRequest) =
        validateEmailDuplication(email = request.email) // 이메일 검증
            .let { validateTwoPasswords(password1 = request.password, password2 = request.password2) } // 패스워드 검증
            .let { request.to(passwordEncoder = passwordEncoder) } // DTO -> 엔티티
            .let { userRepository.save(it) } // 저장
            .let { makeCart(user = it) } // 장바구니 생성
            .let { } // 리턴값 X

    @Description("로그인 시 이메일 존재 여부 체크")
    private fun findUserByEmail(email: String) =
        userRepository.findByEmail(email = email) ?: throw LoginException()

    @Description("로그인 시 패스워드 일치 여부 체크")
    private fun validatePassword(user: User, password: String) =
        if (!passwordEncoder.matches(password, user.password)) throw LoginException() else Unit

    @Description("로그인")
    fun login(request: LoginRequest) =
        validatePassword(
            user = findUserByEmail(email = request.email),
            password = request.password
        ) // 패스워드 검증
            .let { } // TODO : 추후 토큰을 리턴할 예정

    @Description("주소 설정 시 우편 번호 형식 체크")
    private fun validateZipCode(zipCode: String) =
        if (zipCode.toIntOrNull() == null || zipCode.length != 5) throw InvalidValueException("우편번호") else Unit

    @Description("주소 설정")
    @Transactional
    fun setAddress(userId: Long, request: AddressRequest) =
        validateZipCode(zipCode = request.zipCode!!)
            .let { entityFinder.findUserById(userId = userId) }
            .let {
                // 이미 입력한 주소가 존재하는 경우
                if (addressRepository.existsByUser(user = it))
                    addressRepository.findByUser(user = it)
                        .update(request = request, encryptor = encryptor)
                // 새로 주소를 등록하는 경우
                else request.to(user = it, encryptor = encryptor)
                    .let { address -> addressRepository.save(address) }
            }
            .let { } // 리턴값 X
}