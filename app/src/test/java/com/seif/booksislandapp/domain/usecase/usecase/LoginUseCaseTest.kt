package com.seif.booksislandapp.domain.usecase.usecase

import com.google.common.truth.Truth
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.usecase.usecase.auth.LoginUseCase
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class LoginUseCaseTest {
    private lateinit var loginUseCase: LoginUseCase

    @Mock
    lateinit var authRepositoryImp: AuthRepositoryImp

    @Before
    fun setUp() {
        loginUseCase = LoginUseCase(authRepositoryImp)
    }

    @Test
    fun `loginUseCase(), when email and password are valid, then return success with message`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif$123", "Cairo", "Maadi", "Male")
        whenever(authRepositoryImp.login(testUser.email, testUser.password)).thenReturn(Resource.Success("Welcome Back"))
        val expected = Resource.Success("Welcome Back")

        // Act
        val actual = loginUseCase.invoke(testUser.email, testUser.password)

        // Assert
        verify(authRepositoryImp, times(1)).login(testUser.email, testUser.password)
        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `loginUseCase(), when email is valid and password is not valid, then return error with message`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "seif", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("please enter a valid password !")

        // Act
        val actual = loginUseCase.invoke(testUser.email, testUser.password)

        // Assert
        verify(authRepositoryImp, times(0)).login(testUser.email, testUser.password)
        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `loginUseCase(), when email is not valid and password is valid, then return error with message`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "smgmail.com", "Seif$123", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("please enter a valid email !")

        // Act
        val actual = loginUseCase.invoke(testUser.email, testUser.password)

        // Assert
        verify(authRepositoryImp, times(0)).login(testUser.email, testUser.password)
        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `loginUseCase(), when email is empty, then return error with message`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "", "Seif$123", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("email can't be empty !")

        // Act
        val actual = loginUseCase.invoke(testUser.email, testUser.password)

        // Assert
        verify(authRepositoryImp, times(0)).login(testUser.email, testUser.password)
        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `loginUseCase(), when password is empty, then return error with message`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gamil.com", "", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("password can't be empty !")

        // Act
        val actual = loginUseCase.invoke(testUser.email, testUser.password)

        // Assert
        verify(authRepositoryImp, times(0)).login(testUser.email, testUser.password)
        Truth.assertThat(actual).isEqualTo(expected)
    }
}