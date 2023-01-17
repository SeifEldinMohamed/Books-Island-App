package com.seif.booksislandapp.domain.usecase.usecase

import com.google.common.truth.Truth
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.domain.usecase.usecase.auth.ForgetPasswordUseCase
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class ForgetPasswordUseCaseTest {
    private lateinit var forgetPasswordUseCase: ForgetPasswordUseCase

    @Mock
    lateinit var authRepositoryImp: AuthRepositoryImp

    @Before
    fun setUp() {
        forgetPasswordUseCase = ForgetPasswordUseCase(authRepositoryImp)
    }

    @Test
    fun `forgetPasswordUseCase(), when email is valid, then return success with message`() = runBlocking {
        // Arrange
        val userEmail = "sm@gmail.com"
        whenever(authRepositoryImp.forgetPassword(userEmail)).thenReturn(Resource.Success("Mail send successfully to reset your password"))
        val expected = Resource.Success("Mail send successfully to reset your password")

        // Act
        val actual = forgetPasswordUseCase.invoke(userEmail)

        // Assert
        verify(authRepositoryImp, times(1)).forgetPassword(anyString())
        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `forgetPasswordUseCase(), when email is not valid, then return success with message`() = runBlocking {
        // Arrange
        val userEmail = "smgmail.com"
        val expected = Resource.Error("please enter a valid email !")

        // Act
        val actual = forgetPasswordUseCase.invoke(userEmail)

        // Assert
        verify(authRepositoryImp, times(0)).forgetPassword(anyString())
        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `forgetPasswordUseCase(), when email is empty, then return success with message`() = runBlocking {
        // Arrange
        val userEmail = ""
        val expected = Resource.Error("email can't be empty !")

        // Act
        val actual = forgetPasswordUseCase.invoke(userEmail)

        // Assert
        verify(authRepositoryImp, times(0)).forgetPassword(anyString())
        Truth.assertThat(actual).isEqualTo(expected)
    }
}