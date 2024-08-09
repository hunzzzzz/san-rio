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
import com.example.sanrio.global.exception.case.VerificationException
import com.example.sanrio.global.utility.Encryptor
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.MailSender
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Description
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.MailSendException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UserService(
    private val userRepository: UserRepository,
    private val addressRepository: AddressRepository,
    private val cartRepository: CartRepository,
    private val encryptor: Encryptor,
    private val passwordEncoder: PasswordEncoder,
    private val entityFinder: EntityFinder,
    private val mailSender: MailSender,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    @Description("회원가입 시 해당 회원 소유의 장바구니를 생성")
    private fun makeCart(user: User) = Cart(user = user).let { cartRepository.save(it) }

    @Description("회원가입 시 본인 인증에 필요한 랜덤한 6자리의 인증번호를 생성")
    private fun generateRandomNumber() = (100000..999999).random().toString()

    @Description("회원가입 시 본인 인증 확인 이메일의 제목")
    private fun getSubject() = "[산리오 캐릭터즈] 본인 인증을 위한 메일입니다."

    @Description("회원가입 시 본인 인증 확인 이메일의 내용")
    private fun getText(code: String) = "다음 인증 코드 [${code}]를 웹 화면에 입력하면 인증이 완료됩니다."

    @Description("회원가입")
    fun signup(isIdentified: Boolean, request: SignUpRequest) {
        check(isIdentified) { throw VerificationException("본인인증이 완료되지 않았습니다.") }
        check(!userRepository.existsByEmail(email = request.email)) { throw DuplicatedValueException("이메일") }
        check(request.password == request.password2) { throw InvalidValueException("비밀번호") }

        request.to(passwordEncoder = passwordEncoder) // DTO -> 엔티티
            .let { userRepository.save(it) } // 저장
            .let { makeCart(user = it) } // 장바구니 생성
    }

    @Description("회원가입 시 인증번호를 이메일로 전송")
    fun sendVerificationEmail(email: String) {
        val verificationCode = generateRandomNumber()
        val subject = getSubject()
        val text = getText(code = verificationCode)

        // 메일 전송
        mailSender.sendEmail(email = email, subject = subject, text = text)
            // Key: 이메일, Value: 인증 코드 -> 3분의 유효 시간을 설정
            .onSuccess { redisTemplate.opsForValue().set(email, verificationCode, 3, TimeUnit.MINUTES) }
            .onFailure { throw MailSendException("이메일 전송 과정에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.") }
    }

    @Description("인증번호 확인")
    fun checkVerificationCode(email: String, code: String) {
        check(redisTemplate.opsForValue().get(email) != null) { throw VerificationException("인증번호가 만료되었습니다.") }
        check(redisTemplate.opsForValue().get(email) == code) { throw VerificationException("인증번호가 일치하지 않습니다.") }
    }

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