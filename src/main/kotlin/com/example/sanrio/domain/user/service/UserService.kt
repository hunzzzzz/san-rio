package com.example.sanrio.domain.user.service

import com.example.sanrio.domain.cart.model.Cart
import com.example.sanrio.domain.cart.repository.CartRepository
import com.example.sanrio.domain.user.dto.request.SignUpRequest
import com.example.sanrio.domain.user.dto.request.UpdatePasswordRequest
import com.example.sanrio.domain.user.dto.response.UserResponse
import com.example.sanrio.domain.user.model.User
import com.example.sanrio.domain.user.repository.UserRepository
import com.example.sanrio.global.exception.case.ForbiddenException
import com.example.sanrio.global.exception.case.SignUpException
import com.example.sanrio.global.exception.case.PasswordException
import com.example.sanrio.global.exception.case.VerificationException
import com.example.sanrio.global.jwt.UserPrincipal
import com.example.sanrio.global.utility.EntityFinder
import com.example.sanrio.global.utility.JwtProvider
import com.example.sanrio.global.utility.MailSender
import jakarta.servlet.http.HttpServletResponse
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
    private val cartRepository: CartRepository,
    private val passwordEncoder: PasswordEncoder,
    private val mailSender: MailSender,
    private val redisTemplate: RedisTemplate<String, String>,
    private val entityFinder: EntityFinder,
    private val jwtProvider: JwtProvider,
) {
    @Description("회원가입 시 본인 인증 완료 여부 확인")
    private fun checkVerification(isIdentified: Boolean) =
        check(isIdentified) { throw SignUpException("본인인증이 완료되지 않았습니다.") }

    @Description("회원가입 시 이메일 중복 여부 확인")
    private fun checkEmailDuplication(email: String) =
        check(!userRepository.existsByEmail(email = email)) { throw SignUpException("이미 사용 중인 이메일입니다.") }

    @Description("회원가입 시 입력한 두 개의 비밀번호의 일치 여부 확인")
    private fun checkTwoPasswords(first: String, second: String) =
        check(first == second) { throw SignUpException("두 비밀번호가 일치하지 않습니다.") }

    @Description("회원가입 시 해당 회원 소유의 장바구니를 생성")
    private fun makeCart(user: User) = Cart(user = user).let { cartRepository.save(it) }

    @Description("회원가입 시 본인 인증에 필요한 랜덤한 6자리의 인증번호를 생성")
    private fun generateRandomNumber() = (100000..999999).random().toString()

    @Description("회원가입 시 본인 인증 확인 이메일의 제목")
    private fun getSubject() = "[산리오 캐릭터즈] 본인 인증을 위한 메일입니다."

    @Description("회원가입 시 본인 인증 확인 이메일의 내용")
    private fun getText(code: String) = "다음 인증 코드 [${code}]를 웹 화면에 입력하면 인증이 완료됩니다."

    @Description("본인 프로필이 맞는지 체크")
    private fun checkUser(userPrincipal: UserPrincipal, userId: Long) =
        check(userPrincipal.id == userId) { throw ForbiddenException() }

    @Description("현재 본인의 패스워드가 맞는지 체크")
    private fun checkCurrentPassword(user: User, inputPassword: String) =
        check(passwordEncoder.matches(inputPassword, user.password))
        { throw PasswordException("비밀번호가 일치하지 않습니다. 기존에 사용하신 패스워드를 정확하게 입력해주세요.") }

    @Description("회원가입")
    fun signup(isIdentified: Boolean, request: SignUpRequest) {
        checkVerification(isIdentified = isIdentified)
        checkEmailDuplication(email = request.email!!)
        checkTwoPasswords(first = request.password!!, second = request.password2!!)

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

    @Description("프로필 조회")
    fun getUserProfile(userPrincipal: UserPrincipal, userId: Long): UserResponse {
        checkUser(userPrincipal = userPrincipal, userId = userId)

        val user = entityFinder.findUserById(userId = userId)
        return UserResponse.from(user = user)
    }

    @Description("비밀번호 변경")
    @Transactional
    fun updatePassword(
        userPrincipal: UserPrincipal,
        userId: Long,
        request: UpdatePasswordRequest,
        response: HttpServletResponse
    ) {
        checkUser(userPrincipal = userPrincipal, userId = userId)

        val user = entityFinder.findUserById(userId = userId)
        checkCurrentPassword(user = user, inputPassword = request.currentPassword!!)
        checkTwoPasswords(first = request.newPassword!!, second = request.newPassword2!!)

        user.updatePassword(newPassword = passwordEncoder.encode(request.newPassword))

        redisTemplate.delete(user.email)
        jwtProvider.deleteCookie(response = response)
    }
}