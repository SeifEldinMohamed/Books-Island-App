package com.seif.booksislandapp.domain.usecase.usecase

import com.google.common.truth.Truth.assertThat
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.domain.usecase.usecase.auth.LogoutUseCase
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class LogoutUseCaseTest {
    private lateinit var logoutUseCase: LogoutUseCase

    @Mock
    private lateinit var authRepositoryImp: AuthRepositoryImp

    @Before
    fun setup() {
        logoutUseCase = LogoutUseCase(authRepositoryImp)
    }

    @Test
    fun `logoutUseCase(), when there is internet connection, then should return success with message`() = runBlocking {
        // Arrange
        whenever(authRepositoryImp.logout()).thenReturn(Resource.Success("Logged Out Successfully"))
        val expected = Resource.Success("Logged Out Successfully")
        // Act
        val actual = logoutUseCase.invoke()

        // Assert
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `logoutUseCase(), when there is no internet connection, then should return error with message`() = runBlocking {
        // Arrange
        whenever(authRepositoryImp.logout()).thenReturn(Resource.Error("Check your Internet connection and try again."))
        val expected = Resource.Error("Check your Internet connection and try again.")
        // Act
        val actual = logoutUseCase.invoke()

        // Assert
        assertThat(actual).isEqualTo(expected)
    }
}