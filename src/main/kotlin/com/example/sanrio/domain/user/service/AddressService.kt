package com.example.sanrio.domain.user.service

import com.example.sanrio.domain.user.dto.request.AddressRequest
import com.example.sanrio.domain.user.repository.AddressRepository
import com.example.sanrio.global.exception.case.InvalidValueException
import com.example.sanrio.global.utility.Encryptor
import com.example.sanrio.global.utility.EntityFinder
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service

@Service
class AddressService(
    private val addressRepository: AddressRepository,
    private val encryptor: Encryptor,
    private val entityFinder: EntityFinder
) {
    @Description("주소 설정")
    @Transactional
    fun setAddress(userId: Long, request: AddressRequest) {
        val user = entityFinder.findUserById(userId = userId)

        // 우편 번호 형식 체크
        check(request.zipCode != null && request.zipCode.length == 5) { InvalidValueException("우편번호") }

        // 첫 번째로 입력한 주소라면, 해당 주소를 기본 주소로 설정한다.
        val address =
            if (!addressRepository.existsByUser(user = user))
                request.to(user = user, encryptor = encryptor, isFirstAddress = true)
            else
                request.to(user = user, encryptor = encryptor)

        addressRepository.save(address)
    }
}