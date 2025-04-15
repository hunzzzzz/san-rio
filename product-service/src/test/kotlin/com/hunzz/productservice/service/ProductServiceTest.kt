package com.hunzz.productservice.service

import com.hunzz.productservice.dto.request.AddProductRequest
import com.hunzz.productservice.model.cassandra.Product
import com.hunzz.productservice.model.mysql.ProductSeller
import com.hunzz.productservice.repository.ProductRepository
import com.hunzz.productservice.repository.ProductSellerRepository
import com.hunzz.productservice.utility.exception.custom.ProductNotFoundException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ProductServiceTest {
    @MockK
    private lateinit var productRepository: ProductRepository

    @MockK
    private lateinit var productSellerRepository: ProductSellerRepository

    @InjectMockKs
    private lateinit var productService: ProductService

    @Test
    fun `물품 등록 시 저장된 Product 객체를 ProductResponse로 변환하여 반환한다`() {
        val sellerId = 1L
        val request = AddProductRequest(
            name = "testName",
            description = "testDescription",
            price = 10_000,
            stock = 100,
            tags = listOf("testTag1", "testTag2")
        )
        val savedProductSeller = ProductSeller(
            sellerId = sellerId
        )
        val savedProduct = Product(
            id = savedProductSeller.id,
            sellerId = sellerId,
            name = request.name,
            description = request.description,
            price = request.price,
            stock = request.stock,
            tags = request.tags
        )

        every { productSellerRepository.save(any()) } returns savedProductSeller
        every { productRepository.save(any()) } returns savedProduct

        // when
        val response = productService.add(sellerId = sellerId, request = request)

        // then
        verify(exactly = 1) { productSellerRepository.save(any()) }
        verify(exactly = 1) { productRepository.save(any()) }
        assertEquals(savedProductSeller.id, response.productId)
        assertEquals(savedProduct.id, response.productId)
        assertEquals(savedProduct.sellerId, response.sellerId)
        assertEquals(savedProduct.name, response.name)
        assertEquals(savedProduct.description, response.description)
        assertEquals(savedProduct.price, response.price)
        assertEquals(savedProduct.stock, response.stock)
        assertEquals(savedProduct.tags, response.tags)
    }

    @Test
    fun `물품 조회 시 Product 객체를 ProductResponse로 변환하여 반환한다`() {
        // given
        val productId = UUID.randomUUID()
        val savedProduct = Product(
            id = productId,
            sellerId = 1L,
            name = "testName",
            description = "testDescription",
            price = 10_000,
            stock = 100,
            tags = listOf("testTag1", "testTag2")
        )

        every { productRepository.findByIdOrNull(any()) } returns savedProduct

        // when
        val response = productService.get(productId = productId)

        // then
        verify(exactly = 1) { productRepository.findByIdOrNull(any()) }
        assertEquals(savedProduct.id, response.productId)
        assertEquals(savedProduct.sellerId, response.sellerId)
        assertEquals(savedProduct.name, response.name)
        assertEquals(savedProduct.description, response.description)
        assertEquals(savedProduct.price, response.price)
        assertEquals(savedProduct.stock, response.stock)
        assertEquals(savedProduct.tags, response.tags)
    }

    @Test
    fun `물품 조회 시 productId가 유효하지 않으면 Exception을 throw한다`() {
        // given
        val wrongProductId = UUID.randomUUID()

        every { productRepository.findByIdOrNull(any()) } returns null

        // expected
        assertThrows<ProductNotFoundException> {
            productService.get(productId = wrongProductId)
        }
    }

    @Test
    fun `sellerId로 물품 목록 조회 시 ProductResponse 리스트로 반환한다`() {
        // given
        val sellerId = 1L
        val productSeller1 = ProductSeller(sellerId = sellerId)
        val productSeller2 = ProductSeller(sellerId = sellerId)
        val product1 = Product(
            id = productSeller1.id,
            sellerId = sellerId,
            name = "testName1",
            description = "testDescription",
            price = 10_000,
            stock = 100,
            tags = listOf("testTag1", "testTag2")
        )
        val product2 = Product(
            id = productSeller2.id,
            sellerId = sellerId,
            name = "testName2",
            description = "testDescription",
            price = 10_000,
            stock = 100,
            tags = listOf("testTag1", "testTag2")
        )

        every { productSellerRepository.findAllBySellerId(any()) } returns listOf(productSeller1.id, productSeller2.id)
        every { productRepository.findAllById(any()) } returns listOf(product1, product2)

        // when
        val response = productService.getAllBySellerId(sellerId = sellerId)

        // then
        verify(exactly = 1) { productSellerRepository.findAllBySellerId(any()) }
        verify(exactly = 1) { productRepository.findAllById(any()) }
        assertEquals(2, response.size)
        assertEquals(listOf("testName1", "testName2"), response.map { it.name })
    }
}