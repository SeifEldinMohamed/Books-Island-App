package com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate

import com.seif.booksislandapp.data.repository.donateAdvertisementList
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GetAllDonateAdvertisementUseCaseTest {
    private lateinit var donateAdvertisementRepository: AdvertisementRepository
    private lateinit var getAllDonateAdvertisementUseCase: GetAllDonateAdvertisementUseCase

    @Before
    fun setUp() {
        donateAdvertisementRepository = mockk(relaxed = true)
        getAllDonateAdvertisementUseCase =
            GetAllDonateAdvertisementUseCase(donateAdvertisementRepository)
    }

    @Test
    fun `when invoke is called and return a list exchange ads`() =
        runBlocking {
            // Given
            val expected = Resource.Success(donateAdvertisementList)
            coEvery { donateAdvertisementRepository.getAllDonateAds() } returns expected

            // When
            val actual = getAllDonateAdvertisementUseCase()

            // Then
            Assert.assertEquals(expected, actual)
        }

    @Test
    fun `when invoke is called and return an error`() =
        runBlocking {
            // Given
            val expected = Resource.Error("Error")
            coEvery { donateAdvertisementRepository.getAllDonateAds() } returns expected

            // When
            val actual = getAllDonateAdvertisementUseCase()

            // Then
            Assert.assertEquals(expected, actual)
        }
}