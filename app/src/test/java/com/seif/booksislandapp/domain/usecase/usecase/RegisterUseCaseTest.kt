package com.seif.booksislandapp.domain.usecase.usecase

import com.google.common.truth.Truth.assertThat
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.domain.model.User
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
    fun `registerUseCase(), when email,  password and username are valid, then return success`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif123", "Maadi, Cairo", "Male")
        whenever(authRepositoryImp.register(any())).thenReturn(Resource.Success(testUser))
        val expected = Resource.Success(testUser)

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(1)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when email and password are valid but username is not valid, then return error`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seif", "sm@gmail.com", "Seif123", "Maadi, Cairo", "Male")
        val expected = Resource.Error("title is too short min char = 5 !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when username and password are valid but email is not valid, then return error`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "smgmail.com", "Seif123", "Maadi, Cairo", "Male")
        val expected = Resource.Error("please enter a valid email !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `registerUseCase(), when username and email are valid but password is not valid, then return error`() = runBlocking {
        // Arrange
        val testUser = User("", "image", "seifM", "sm@gmail.com", "seif", "Maadi, Cairo", "Male")
        val expected = Resource.Error("Not Valid Password Format !")

        // Act
        val actual = registerUseCase.invoke(testUser)

        // Assert
        verify(authRepositoryImp, times(0)).register(testUser)
        assertThat(actual).isEqualTo(expected)
    }
}