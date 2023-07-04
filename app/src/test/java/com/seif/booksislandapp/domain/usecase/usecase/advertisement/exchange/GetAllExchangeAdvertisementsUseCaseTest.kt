package com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange

import com.seif.booksislandapp.data.repository.exchangeAdvertisementList
import com.seif.booksislandapp.domain.repository.ExchangeAdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllExchangeAdvertisementsUseCaseTest {
    private lateinit var exchangeAdvertisementRepository: ExchangeAdvertisementRepository
    private lateinit var getAllExchangeAdvertisementUseCase: GetAllExchangeAdvertisementUseCase

    @Before
    fun setUp() {
        exchangeAdvertisementRepository = mockk(relaxed = true)
        getAllExchangeAdvertisementUseCase = GetAllExchangeAdvertisementUseCase(exchangeAdvertisementRepository)
    }
    @Test
    fun `when invoke is called and return a list exchange ads`() =
        runBlocking {
            // Given
            val expected = Resource.Success(exchangeAdvertisementList)
            coEvery { exchangeAdvertisementRepository.getAllExchangeAdvertisement() } returns expected

            // When
            val actual = getAllExchangeAdvertisementUseCase()

            // Then
            assertEquals(expected, actual)
        }

    @Test
    fun `when invoke is called and return an error`() =
        runBlocking {
            // Given
            val expected = Resource.Error("Error")
            coEvery { exchangeAdvertisementRepository.getAllExchangeAdvertisement() } returns expected

            // When
            val actual = getAllExchangeAdvertisementUseCase()

            // Then
            assertEquals(expected, actual)
        }
}