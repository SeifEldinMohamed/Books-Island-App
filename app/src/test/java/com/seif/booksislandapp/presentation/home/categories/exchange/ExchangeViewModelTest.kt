package com.seif.booksislandapp.presentation.home.categories.exchange
import app.cash.turbine.test
import com.seif.booksislandapp.data.repository.exchangeAdvertisementList
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.GetAllExchangeAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.GetExchangeAdsByFilterUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.SearchExchangeAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
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
class ExchangeViewModelTest {
    private lateinit var classUnderTest: ExchangeViewModel
    private lateinit var getAllExchangeAdvertisementUseCase: GetAllExchangeAdvertisementUseCase
    private lateinit var testDispatcher: TestDispatchers
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase
    private lateinit var searchExchangeAdvertisementUseCase: SearchExchangeAdvertisementUseCase
    private lateinit var getExchangeAdsByFilterUseCase: GetExchangeAdsByFilterUseCase

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    @Before
    fun setup() {
        testDispatcher = TestDispatchers()
        resourceProvider = mockk()
        getAllExchangeAdvertisementUseCase = mockk(relaxed = true)
        searchExchangeAdvertisementUseCase = mockk(relaxed = true)
        getFromSharedPreferenceUseCase = mockk(relaxed = true)
        getExchangeAdsByFilterUseCase = mockk(relaxed = true)
        classUnderTest = ExchangeViewModel(
            getAllExchangeAdvertisementUseCase,
            searchExchangeAdvertisementUseCase,
            getExchangeAdsByFilterUseCase,
            resourceProvider,
            testDispatcher,
            getFromSharedPreferenceUseCase
        )
    }

    @Test
    fun `fetchAllExchangeAdvertisement should emit FetchAllExchangeAdvertisementSuccessfully`() =
        runTest {
            // Given
            val expected = Resource.Success(exchangeAdvertisementList)
            coEvery { getAllExchangeAdvertisementUseCase.invoke() } returns expected

            // When
            classUnderTest.fetchAllExchangeAds()

            // Then
            classUnderTest.exchangeState.test {
                Assert.assertEquals(ExchangeState.IsLoading(true), awaitItem())
                Assert.assertEquals(ExchangeState.IsLoading(false), awaitItem())
                Assert.assertEquals(
                    ExchangeState.FetchAllExchangeAdsSuccessfully(exchangeAdvertisementList), awaitItem()
                )
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchAllExchangeAdvertisement(), when there is no internet connection, then should emit NoInternetConnection state with message`() =
        runTest {
            // Arrange
            val noInternetConnectionMessage = "Check your Internet connection and try again."
            every { resourceProvider.string(any()) } returns noInternetConnectionMessage
            coEvery { getAllExchangeAdvertisementUseCase.invoke() } returns Resource.Error(
                noInternetConnectionMessage
            )

            // Act
            classUnderTest.fetchAllExchangeAds()
            // Assert
            classUnderTest.exchangeState.test {
                Assert.assertEquals(ExchangeState.IsLoading(true), awaitItem())
                Assert.assertEquals(ExchangeState.IsLoading(false), awaitItem())
                Assert.assertEquals(
                    ExchangeState.NoInternetConnection(noInternetConnectionMessage),
                    awaitItem()
                )
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `fetchAllExchangeAdvertisement(), when there is is internet connection but there is an error, then should emit ShowError state with error message`() =
        runTest {
            // Arrange
            val firebaseErrorMessage = "Firebase Error Message"
            every { resourceProvider.string(any()) } returns "Check your Internet connection and try again."
            coEvery { getAllExchangeAdvertisementUseCase.invoke() } returns Resource.Error(
                firebaseErrorMessage
            )

            // Act
            classUnderTest.fetchAllExchangeAds()
            // Assert
            classUnderTest.exchangeState.test {
                Assert.assertEquals(ExchangeState.IsLoading(true), awaitItem())
                Assert.assertEquals(ExchangeState.IsLoading(false), awaitItem())
                Assert.assertEquals(ExchangeState.ShowError(firebaseErrorMessage), awaitItem())
                // Finish the test
                cancelAndIgnoreRemainingEvents()
            }
        }
}