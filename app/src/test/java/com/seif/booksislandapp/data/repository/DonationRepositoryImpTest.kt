package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.data.mapper.toDonateAdvertisement
import com.seif.booksislandapp.data.remote.dto.adv.donation.DonateAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.utils.Constants.Companion.DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class DonationRepositoryImpTest {
    private lateinit var advertisementRepositoryImp: AdvertisementRepositoryImp

    // Mock dependencies
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: StorageReference
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var donateAdvertisementDto1: DonateAdvertisementDto
    private lateinit var donateAdvertisementDto2: DonateAdvertisementDto
    private lateinit var querySnapshot: QuerySnapshot // used in case of get() function and put it in
    private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
    private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot

    private lateinit var donateAdvertisement1: DonateAdvertisement
    private lateinit var donateAdvertisement2: DonateAdvertisement

    private lateinit var collectionReference: CollectionReference
    private lateinit var query: Query

    @Before
    fun setUp() {
        mockkStatic("com.seif.booksislandapp.data.mapper.MapperKt") // add this so we can mock the mapper extension functions inside this file

        firestore = mockk()
        storage = mockk()
        resourceProvider = mockk()
        connectivityManager =
            mockk(relaxed = true) // allows you to create a mock object that ignores any method calls that are not explicitly stubbed

        // mock dto ads
        donateAdvertisementDto1 = mockk()
        donateAdvertisementDto2 = mockk()

        // mock ads
        donateAdvertisement1 = mockk()
        donateAdvertisement2 = mockk()

        // querySnapshot and queryDocumentSnapshot
        querySnapshot = mockk()
        queryDocumentSnapshot1 = mockk()
        queryDocumentSnapshot2 = mockk()

        // collection reference
        collectionReference = mockk()

        // query
        query = mockk()

        advertisementRepositoryImp = AdvertisementRepositoryImp(
            firestore = firestore,
            connectivityManager = connectivityManager,
            storageReference = storage,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `getAllDonateAds(), when there is an internet connection, then return ResourceSuccess with Array List of DonateAdvertisement`() =
        runBlocking {
            /** Arrange **/
            // Mock network connectivity
            every { connectivityManager.checkInternetConnection() } returns true

            // Mock Firestore query snapshot and QueryDocumentSnapshot
            every { queryDocumentSnapshot1.toObject(DonateAdvertisementDto::class.java) } returns donateAdvertisementDto1 // ==> document.toObject(DonateAdvertisementDto::class.java)
            every { queryDocumentSnapshot2.toObject(DonateAdvertisementDto::class.java) } returns donateAdvertisementDto2
            //  every { querySnapshot.documents } returns listOf(queryDocumentSnapshot1, queryDocumentSnapshot2)

            every { querySnapshot.iterator() } returns mutableListOf(
                queryDocumentSnapshot1,
                queryDocumentSnapshot2
            ).iterator() // ==> for (document in querySnapshot.iterator())

            // Mock Firestore collection reference
            // Mock Firestore query

            every { firestore.collection(any()) } returns collectionReference // ==>  firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
            every {
                collectionReference.whereNotEqualTo(
                    any<String>(),
                    any()
                )
            } returns query // ==> .whereNotEqualTo("status", AdvStatus.Closed.toString())
            every { query.orderBy(any<String>()) } returns query // ==> .orderBy("status")
            every {
                query.orderBy(
                    any<String>(),
                    any()
                )
            } returns query // ==> .orderBy("publishDate", Query.Direction.DESCENDING)
            every { query.get() } returns Tasks.forResult(querySnapshot) // ==> .get()

            // Mock DonateAdvertisement mapping
            every { donateAdvertisementDto1.toDonateAdvertisement() } returns donateAdvertisement1
            every { donateAdvertisementDto2.toDonateAdvertisement() } returns donateAdvertisement2

            /** Act **/
            val result = advertisementRepositoryImp.getAllDonateAds()

            /** Assert **/
            // Verify the expected interactions
            coVerify { firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION) }
            coVerify { collectionReference.whereNotEqualTo("status", AdvStatus.Closed.toString()) }
            coVerify { query.orderBy("status") }
            coVerify { query.orderBy("publishDate", Query.Direction.DESCENDING) }
            coVerify { query.get() }

            // Assert the result
            println(result.toString())
            assertTrue(result is Resource.Success)
            val successResult = result as Resource.Success
            assertEquals(2, successResult.data.size)
            assertEquals(donateAdvertisement1, successResult.data[0])
            assertEquals(donateAdvertisement2, successResult.data[1])
        }

    @Test
    fun `getAllDonateAds(), when there is no internet connection, then return ResourceFailure with no internet connection message`() =
        runBlocking {
            /** Arrange **/
            val errorMessage = "No internet connection"
            // Mock network connectivity
            every { connectivityManager.checkInternetConnection() } returns false
            // Mock resource strings
            every { resourceProvider.string(any()) } returns errorMessage

            /** Act **/
            val result = advertisementRepositoryImp.getAllDonateAds()

            /** Assert **/
            println(result.toString())
            assertTrue(result is Resource.Error)
            val successResult = result as Resource.Error
            assertEquals(errorMessage, successResult.message)
        }

    @Test
    fun `getAllDonateAds(), when there is internet connection but there is a thrown exception, then return ResourceFailure with no internet connection message`() =
        runBlocking {
            /** Arrange **/
            val firebaseErrorMessage = "firebase exception"
            every { connectivityManager.checkInternetConnection() } returns true
            every { firestore.collection(any()) } throws FirebaseException(firebaseErrorMessage)

            /** Act **/
            val result = advertisementRepositoryImp.getAllDonateAds()

            /** Assert **/
            println(result.toString())
            assertTrue(result is Resource.Error)
            val successResult = result as Resource.Error
            assertEquals(firebaseErrorMessage, successResult.message)
        }
}