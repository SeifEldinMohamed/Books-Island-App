package com.seif.booksislandapp.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Constants.Companion.USER_FireStore_Collection
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

    @Before
    fun setUp() {

        authRepositoryImp = AuthRepositoryImp(firestore, firebaseAuth)
    }

    @Test
    fun `register(), when email, password and username are valid, then should return success`() =
        runBlocking {
            // Arrange
            val testUser = User("", "image", "seifM", "sm@gmail.com", "Seif123", "Maadi, Cairo", "Male")

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

            val expected = Resource.Success(testUser)

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            verify(firebaseAuth, times(1)).createUserWithEmailAndPassword(any(), any())
            verify(firestore, times(1)).collection(any())
            assertThat(actual).isEqualTo(expected)
        }
    @Test
    fun `register(), when username in empty , then should return error with exception message_illegal argument exception`() =
        runBlocking {
            // Arrange
            val testUser = User("", "image", "", "sm@gmail.com", "Seif123", "Maadi, Cairo", "Male")
            whenever(firebaseAuth.createUserWithEmailAndPassword(any(), any())).thenThrow(
                IllegalArgumentException("illegal argument exception")
            )
            val expected = Resource.Error("illegal argument exception")

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun `register(), when password is weak , then should return error with exception message_Error Weak Password`() =
        runBlocking {
            // Arrange
            val testUser = User("", "image", "seifM", "sm@gmail.com", "seif123", "Maadi, Cairo", "Male")
            whenever(firebaseAuth.createUserWithEmailAndPassword(any(), any())).thenReturn(
                Tasks.forException(FirebaseAuthException("503", "Error Weak Password"))
            )
            val expected = Resource.Error("Error Weak Password")

            // Act
            val actual = authRepositoryImp.register(testUser)

            // Assert
            assertThat(actual).isEqualTo(expected)
        }
}