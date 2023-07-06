package com.seif.booksislandapp.presentation.home.categories.buy

import app.cash.turbine.test
import com.seif.booksislandapp.FAKE_SELL_ADS_RESPONSE
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.GetAllSellAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.GetSellAdsByFilterUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.SearchSellAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.utils.MainCoroutineRule
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.TestDispatchers
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BuyViewModelTest {
    private lateinit var buyViewModel: BuyViewModel

    private lateinit var testDispatcher: TestDispatchers

    private lateinit var getAllSellAdvertisementUseCase: GetAllSellAdvertisementUseCase
    private lateinit var getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase
    private lateinit var resourceProvider: ResourceProvider

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    @Before
    fun setup() {
        testDispatcher = TestDispatchers()
        // Mock dependencies
        resourceProvider = mockk()
        getAllSellAdvertisementUseCase = mockk(relaxed = true)
        getFromSharedPreferenceUseCase = mockk(relaxed = true)
        val getSellAdsByFilterUseCase = mockk<GetSellAdsByFilterUseCase>(relaxed = true)
        val searchSellAdvertisementUseCase = mockk<SearchSellAdvertisementUseCase>(relaxed = true)

        // Create an instance of the ViewModel with the mocked dependencies
        buyViewModel = BuyViewModel(
            getAllSellAdvertisementUseCase,
            getSellAdsByFilterUseCase,
            searchSellAdvertisementUseCase,
            resourceProvider,
            getFromSharedPreferenceUseCase,
            testDispatcher,
        )
    }

    @Test
    fun `fetchAllSellAdvertisement should emit FetchAllSellAdvertisementSuccessfully`() =
        runTest {
            // Arrange
            coEvery { getAllSellAdvertisementUseCase.invoke() } returns FAKE_SELL_ADS_RESPONSE
            //  every { resourceProvider.string(any()) } returns "errorMessage"

            // Act
            buyViewModel.fetchAllSellAdvertisement()

            // Assert
            buyViewModel.buyState.test {
                assertEquals(BuyState.IsLoading(true), awaitItem())
                assertEquals(BuyState.IsLoading(false), awaitItem())
                assertEquals(
                    BuyState.FetchAllSellAdvertisementSuccessfully(FAKE_SELL_ADS_RESPONSE.data),
                    awaitItem()
                )
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchAllSellAdvertisement(), when there is no internet connection, then should emit NoInternetConnection state with message`() =
        runTest {
            // Arrange
            val noInternetConnectionMessage = "Check your Internet connection and try again."
            every { resourceProvider.string(any()) } returns noInternetConnectionMessage
            coEvery { getAllSellAdvertisementUseCase.invoke() } returns Resource.Error(
                noInternetConnectionMessage
            )

            // Act
            buyViewModel.fetchAllSellAdvertisement()
            // Assert
            buyViewModel.buyState.test {
                assertEquals(BuyState.IsLoading(true), awaitItem())
                assertEquals(BuyState.IsLoading(false), awaitItem())
                assertEquals(
                    BuyState.NoInternetConnection(noInternetConnectionMessage),
                    awaitItem()
                )
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchAllSellAdvertisement(), when there is is internet connection but there is an error, then should emit ShowError state with error message`() =
        runTest {
            // Arrange
            val firebaseErrorMessage = "Firebase Error Message"
            every { resourceProvider.string(any()) } returns "Check your Internet connection and try again."
            coEvery { getAllSellAdvertisementUseCase.invoke() } returns Resource.Error(
                firebaseErrorMessage
            )

            // Act
            buyViewModel.fetchAllSellAdvertisement()
            // Assert
            buyViewModel.buyState.test {
                assertEquals(BuyState.IsLoading(true), awaitItem())
                assertEquals(BuyState.IsLoading(false), awaitItem())
                assertEquals(BuyState.ShowError(firebaseErrorMessage), awaitItem())
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }
}