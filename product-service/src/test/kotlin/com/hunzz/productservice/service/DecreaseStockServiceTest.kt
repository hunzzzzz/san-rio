package com.hunzz.productservice.service

import com.hunzz.productservice.model.cassandra.Product
import com.hunzz.productservice.repository.ProductRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class DecreaseStockServiceTest {
    @MockK
    private lateinit var productRepository: ProductRepository

    @InjectMockKs
    private lateinit var decreaseStockService: DecreaseStockService

    @Test
    fun `수량 감소 요청 시 stock을 count만큼 감소시키고 Product 객체를 ProductResponse로 변환하여 반환한다`() {
        // given
        val initStock = 100
        val productId = UUID.randomUUID()
        val decreaseCount = 1
        val savedProduct = Product(
            id = productId,
            sellerId = 1L,
            name = "testName1",
            description = "testDescription",
            price = 10_000,
            stock = initStock,
            tags = listOf("testTag1", "testTag2")
        )

        every { productRepository.findByIdOrNull(any()) } returns savedProduct

        // when
        val response = decreaseStockService.decreaseStock(productId = productId, count = decreaseCount)

        // then
        verify(exactly = 1) { productRepository.findByIdOrNull(any()) }

        assertEquals(initStock - decreaseCount, response.stock)
    }
}