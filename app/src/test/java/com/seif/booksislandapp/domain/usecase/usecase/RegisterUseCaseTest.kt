package com.seif.booksislandapp.domain.usecase.usecase

import com.google.common.truth.Truth.assertThat
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.usecase.usecase.auth.RegisterUseCase
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class RegisterUseCaseTest {
    private lateinit var registerUseCase: RegisterUseCase

    @Mock
    lateinit var authRepositoryImp: AuthRepositoryImp

    @Before
    fun setUp() {
        registerUseCase = RegisterUseCase(authRepositoryImp)
    }

    @Test
    fun `registerUseCase(), when email, password and username are valid, then return Success_WithSuccessMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif$123", "Cairo", "Maadi", "Male")
        whenever(authRepositoryImp.register(any())).thenReturn(Resource.Success("user Added Successfully"))
        val expected = Resource.Success("user Added Successfully")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(1)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except username, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seif", "sm@gmail.com", "Seif$123", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("username is too short min char = 5 !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except email, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "smgmail.com", "Seif$123", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("please enter a valid email !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except email is empty, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "", "Seif$123", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("email can't be empty !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except password, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "seif", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("Not Valid Password Format !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except password is empty, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "", "Cairo", "Maadi", "Male")
        val expected = Resource.Error("password can't be empty !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except government is empty, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif$123", "", "Maadi", "Male")
        val expected = Resource.Error("please choose your government !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when all inputs are valid except district is empty, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif$123", "Cairo", "", "Male")
        val expected = Resource.Error("please choose your district !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }
    @Test
    fun `registerUseCase(), when all inputs are valid except gender is empty, then return Error_withErrorMessage`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif$123", "Cairo", "Maadi", "")
        val expected = Resource.Error("please choose your gender !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }
}