package com.example.sanrio.domain.address.service

import com.example.sanrio.domain.address.dto.request.AddressRequest
import com.example.sanrio.domain.address.model.Address
import com.example.sanrio.domain.address.repository.AddressRepository
import com.example.sanrio.global.exception.case.AddressException
import com.example.sanrio.global.exception.case.ForbiddenException
import com.example.sanrio.global.jwt.UserPrincipal
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
    @Description("본인 프로필이 맞는지 체크")
    private fun checkUser(userPrincipal: UserPrincipal, userId: Long) =
        check(userPrincipal.id == userId) { throw ForbiddenException() }

    @Description("우편번호 형식 체크")
    private fun checkZipCode(zipCode: String?) =
        check(zipCode != null && zipCode.length == 5) { throw AddressException("일치하지 않는 우편번호입니다.") }

    @Description("주소 추가")
    fun setAddress(userPrincipal: UserPrincipal, userId: Long, request: AddressRequest) {
        checkUser(userPrincipal = userPrincipal, userId = userId)
        checkZipCode(zipCode = request.zipCode)

        val user = entityFinder.findUserById(userId = userId)

        // 첫 번째로 입력한 주소라면, 해당 주소를 기본 주소로 설정한다.
        val address =
            if (!addressRepository.existsByUser(user = user))
                request.to(user = user, encryptor = encryptor, isFirstAddress = true)
            else
                request.to(user = user, encryptor = encryptor)

        addressRepository.save(address)
    }

    @Description("이미 기본 주소로 설정되있는지 체크")
    private fun checkAlreadyDefaultAddress(defaultAddress: Address, addressId: Long) =
        check(defaultAddress.id != addressId) { throw AddressException("이미 기본 주소로 설정되어 있습니다.") }

    @Description("기본 주소로 설정")
    @Transactional
    fun setDefaultAddress(userPrincipal: UserPrincipal, userId: Long, addressId: Long) {
        checkUser(userPrincipal = userPrincipal, userId = userId)

        val user = entityFinder.findUserById(userId = userId)
        val defaultAddress = addressRepository.findByUserAndDefault(user = user, default = true)

        checkAlreadyDefaultAddress(defaultAddress = defaultAddress, addressId = addressId)

        val newDefaultAddress = entityFinder.findAddressById(addressId = addressId)

        defaultAddress.updateDefault()
        newDefaultAddress.updateDefault()
    }
}