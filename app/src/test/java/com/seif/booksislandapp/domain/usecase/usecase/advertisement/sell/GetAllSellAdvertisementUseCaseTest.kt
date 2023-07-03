package com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell

import com.seif.booksislandapp.FAKE_SELL_ADS_RESPONSE
import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllSellAdvertisementUseCaseTest {
    private lateinit var getAllSellAdvertisementUseCase: GetAllSellAdvertisementUseCase

    lateinit var advertisementRepositoryImp: AdvertisementRepositoryImp

    @Before
    fun setUp() {
        advertisementRepositoryImp = mockk()
        getAllSellAdvertisementUseCase = GetAllSellAdvertisementUseCase(advertisementRepositoryImp)
    }

    @Test
    fun `getAllSellAdvertisementUseCase(), when there is no error, then return success with arrayList of Sell Ads`() =
        runBlocking {
            // Arrange
            val expected = FAKE_SELL_ADS_RESPONSE
            coEvery { advertisementRepositoryImp.getAllSellAds() } returns FAKE_SELL_ADS_RESPONSE
            // Act
            val actualResult = getAllSellAdvertisementUseCase.invoke()
            // Assert
            assertEquals(expected, actualResult)
        }

    @Test
    fun `getAllSellAdvertisementUseCase(), when there is error, then return Resource Error with error message`() =
        runBlocking {
            // Arrange
            val expected = Resource.Error("Error Message")
            coEvery { advertisementRepositoryImp.getAllSellAds() } returns expected
            // Act
            val actualResult = getAllSellAdvertisementUseCase.invoke()
            // Assert
            assertEquals(expected, actualResult)
        }
}