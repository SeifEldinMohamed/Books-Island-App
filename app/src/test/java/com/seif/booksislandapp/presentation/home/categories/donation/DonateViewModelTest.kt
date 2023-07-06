package com.seif.booksislandapp.presentation.home.categories.donation

import app.cash.turbine.test
import com.seif.booksislandapp.data.repository.donateAdvertisementList
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.GetAllDonateAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.SearchDonateAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.GetDonateAdsByFilterUseCase
import com.seif.booksislandapp.utils.MainCoroutineRule
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.TestDispatchers
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
@OptIn(ExperimentalCoroutinesApi::class)
class DonateViewModelTest {
    private lateinit var classUnderTest: DonateViewModel
    private lateinit var getAllDonateAdvertisementUseCase: GetAllDonateAdvertisementUseCase
    private lateinit var testDispatcher: TestDispatchers
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var searchDonateAdvertisementUseCase: SearchDonateAdvertisementUseCase
    private lateinit var getDonateAdsByFilterUseCase: GetDonateAdsByFilterUseCase

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    @Before
    fun setup() {
        testDispatcher = TestDispatchers()
        resourceProvider = mockk()
        getAllDonateAdvertisementUseCase = mockk(relaxed = true)
        searchDonateAdvertisementUseCase = mockk(relaxed = true)
        getDonateAdsByFilterUseCase = mockk(relaxed = true)
        classUnderTest = DonateViewModel(
            getAllDonateAdvertisementUseCase,
            searchDonateAdvertisementUseCase,
            getDonateAdsByFilterUseCase,
            resourceProvider,
            testDispatcher
        )
    }

    @Test
    fun `fetchAllDonateAdvertisement should emit FetchAllDonateAdvertisementSuccessfully`() =
        runTest {
            // Given
            val expected = Resource.Success(donateAdvertisementList)
            coEvery { getAllDonateAdvertisementUseCase.invoke() } returns expected

            // When
            classUnderTest.fetchAllDonateAdvertisement()

            // Then
            classUnderTest.donateState.test {
                Assert.assertEquals(DonateState.IsLoading(true), awaitItem())
                Assert.assertEquals(DonateState.IsLoading(false), awaitItem())
                Assert.assertEquals(
                    DonateState.FetchAllDonateAdvertisementSuccessfully(donateAdvertisementList), awaitItem()
                )
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchAllDonateAdvertisement(), when there is no internet connection, then should emit NoInternetConnection state with message`() =
        runTest {
            // Arrange
            val noInternetConnectionMessage = "Check your Internet connection and try again."
            every { resourceProvider.string(any()) } returns noInternetConnectionMessage
            coEvery { getAllDonateAdvertisementUseCase.invoke() } returns Resource.Error(
                noInternetConnectionMessage
            )

            // Act
            classUnderTest.fetchAllDonateAdvertisement()
            // Assert
            classUnderTest.donateState.test {
                Assert.assertEquals(DonateState.IsLoading(true), awaitItem())
                Assert.assertEquals(DonateState.IsLoading(false), awaitItem())
                Assert.assertEquals(
                    DonateState.NoInternetConnection(noInternetConnectionMessage),
                    awaitItem()
                )
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchAllDonateAdvertisement(), when there is is internet connection but there is an error, then should emit ShowError state with error message`() =
        runTest {
            // Arrange
            val firebaseErrorMessage = "Firebase Error Message"
            every { resourceProvider.string(any()) } returns "Check your Internet connection and try again."
            coEvery { getAllDonateAdvertisementUseCase.invoke() } returns Resource.Error(
                firebaseErrorMessage
            )

            // Act
            classUnderTest.fetchAllDonateAdvertisement()
            // Assert
            classUnderTest.donateState.test {
                Assert.assertEquals(DonateState.IsLoading(true), awaitItem())
                Assert.assertEquals(DonateState.IsLoading(false), awaitItem())
                Assert.assertEquals(DonateState.ShowError(firebaseErrorMessage), awaitItem())
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }
}