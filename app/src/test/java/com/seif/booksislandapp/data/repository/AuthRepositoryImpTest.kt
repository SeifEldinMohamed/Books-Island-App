package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.USER_FireStore_Collection
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryImpTest {
    lateinit var authRepositoryImp: AuthRepositoryImp

    @Mock
    lateinit var firestore: FirebaseFirestore

    @Mock
    lateinit var firebaseAuth: FirebaseAuth

    @Mock
    lateinit var resourceProvider: ResourceProvider

    @Mock
    lateinit var sharedPrefs: SharedPrefs

    private var connectivityManager: ConnectivityManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        authRepositoryImp = AuthRepositoryImp(
            firestore,
            firebaseAuth,
            resourceProvider,
            sharedPrefs,
            connectivityManager
        )
        MockKAnnotations.init(this)
    }

    @Test
    fun `register(), when email, password and username are valid, then should return success with registered user`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "seifM", "sm@gmail.com", "Seif$123", "Cairo", "Maadi", "Male")

            val mockExistingUser = mock<FirebaseUser> {
                on { uid } doReturn "1400"
            }
            val mockAuthResult = mock<AuthResult> {
                on { user } doReturn mockExistingUser
            }
            firebaseAuth.stub {
                on {
                    createUserWithEmailAndPassword(any(), any())
                } doReturn Tasks.forResult(mockAuthResult)
            }

            resourceProvider.stub {
                on {
                    string(R.string.user_added_successfully)
                } doReturn "User Added Successfully"
            }

            val documentReference = mock<DocumentReference> {
                on { set(any()) } doReturn Tasks.forResult(null)
            }
            val collectionReference = mock<CollectionReference> {
                on { document(any()) } doReturn documentReference
            }
            firestore.stub {
                on {
                    collection(USER_FireStore_Collection)
                } doReturn collectionReference
            }
            // internet connection
            every {
                connectivityManager.checkInternetConnection()
            } returns true

            val expected = Resource.Success(resourceProvider.string(R.string.user_added_successfully))

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            verify(firebaseAuth, times(1)).createUserWithEmailAndPassword(any(), any())
            verify(firestore, times(1)).collection(any())
            verify(sharedPrefs, times(1)).put(anyString(), anyString())
            assertThat(actual).isEqualTo(expected)
        }

    @Test // not happending bec we check validation of user input in register usecase
    fun `register(), when username in empty and there is internet connection , then should return error with exception message_illegal argument exception`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "", "sm@gmail.com", "Seif123", "Cairo", "Maadi", "Male")
            whenever(firebaseAuth.createUserWithEmailAndPassword(any(), any())).thenThrow(
                IllegalArgumentException("illegal argument exception")
            )
            every {
                connectivityManager.checkInternetConnection()
            } returns true
            val expected = Resource.Error("illegal argument exception")

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `register(), when password is weak and there is internet connection, then should return error with exception message_Error Weak Password`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "seifM", "sm@gmail.com", "seif123", "Cairo", "Maadi", "Male")
            whenever(firebaseAuth.createUserWithEmailAndPassword(any(), any())).thenReturn(
                Tasks.forException(FirebaseAuthException("503", "Error Weak Password"))
            )
            every {
                connectivityManager.checkInternetConnection()
            } returns true
            val expected = Resource.Error("Error Weak Password")

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `register(), when there is no internet connection , then should return error with no internet message`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "seifM", "sm@gmail.com", "seif$123", "Cairo", "Maadi", "Male")

            resourceProvider.stub {
                on {
                    string(R.string.no_internet_connection)
                } doReturn "Check your Internet connection and try again."
            }

            every {
                connectivityManager.checkInternetConnection()
            } returns false

            val expected = Resource.Error(resourceProvider.string(R.string.no_internet_connection))

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `login(), when valid email and password and there is internet connection, then should return success with loggedIn user`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "seifM", "sm@gmail.com", "Seif123", "Cairo", "Maadi", "Male")
            firebaseAuth.stub {
                on {
                    signInWithEmailAndPassword(testUser.email, testUser.password)
                } doReturn Tasks.forResult(null)
            }
            resourceProvider.stub {
                on {
                    string(R.string.welcome_back)
                } doReturn "Welcome Back"
            }
            every {
                connectivityManager.checkInternetConnection()
            } returns true
            val expected = Resource.Success(resourceProvider.string(R.string.welcome_back))

            // Act
            val actual = authRepositoryImp.login(testUser.email, testUser.password)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `login(), when valid email and not valid password and there is internet connection, then should return error with message`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "seifM", "sm@gmail.com", "seif123", "Cairo", "Maadi", "Male")
            firebaseAuth.stub {
                on {
                    signInWithEmailAndPassword(testUser.email, testUser.password)
                } doReturn Tasks.forException(
                    FirebaseAuthInvalidCredentialsException(
                        "",
                        "invalid email or password"
                    )
                )
            }
            // internet connection
            every {
                connectivityManager.checkInternetConnection()
            } returns true

            val expected = Resource.Error("invalid email or password")

            // Act
            val actual = authRepositoryImp.login(testUser.email, testUser.password)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `login(), when there is no internet connection , then should return error with no internet message`() =
        runBlocking {
            // Arrange
            val testUser =
                User("", "image", "seifM", "sm@gmail.com", "seif$123", "Cairo", "Maadi", "Male")

            resourceProvider.stub {
                on {
                    string(R.string.no_internet_connection)
                } doReturn "Check your Internet connection and try again."
            }

            every {
                connectivityManager.checkInternetConnection()
            } returns false

            val expected = Resource.Error(resourceProvider.string(R.string.no_internet_connection))

            // Act
            val actual = authRepositoryImp.login(testUser.email, testUser.password)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `forgetPassword(), when email is valid and there is internet connection, then should return success with message`() =
        runBlocking {
            // Arrange
            val userEmail = "sm@gmail.com"

            resourceProvider.stub {
                on {
                    string(R.string.send_mail_to_reset_password)
                } doReturn "Mail send successfully to reset your password"
            }

            every {
                connectivityManager.checkInternetConnection()
            } returns true

            whenever(firebaseAuth.sendPasswordResetEmail(anyString())).thenReturn(Tasks.forResult(null))

            val expected =
                Resource.Success(resourceProvider.string(R.string.send_mail_to_reset_password))

            // Act
            val actual = authRepositoryImp.forgetPassword(userEmail)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `forgetPassword(), when there is no internet connection, then should return error with message`() =
        runBlocking {
            // Arrange
            val userEmail = "sm@gmail.com"

            resourceProvider.stub {
                on {
                    string(R.string.no_internet_connection)
                } doReturn "Check your Internet connection and try again."
            }

            every {
                connectivityManager.checkInternetConnection()
            } returns false

            val expected =
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))

            // Act
            val actual = authRepositoryImp.forgetPassword(userEmail)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `logout(), when there is internet connection, then should return success with message`() = runBlocking {
        // Arrange
        resourceProvider.stub {
            on {
                string(R.string.logged_out_successfully)
            } doReturn "Logged Out Successfully"
        }
        every {
            connectivityManager.checkInternetConnection()
        } returns true

        doNothing().`when`(firebaseAuth).signOut()
        val expected = Resource.Success(resourceProvider.string(R.string.logged_out_successfully))

        // Act
        val actual = authRepositoryImp.logout()

        // Assert
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `logout(), when there is no internet connection, then should return error with message`() =
        runBlocking {
            // Arrange

            resourceProvider.stub {
                on {
                    string(R.string.no_internet_connection)
                } doReturn "Check your Internet connection and try again."
            }

            every {
                connectivityManager.checkInternetConnection()
            } returns false

            val expected =
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))

            // Act
            val actual = authRepositoryImp.logout()

            // Assert
            assertThat(actual).isEqualTo(expected)
        }
}
