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
import com.seif.booksislandapp.data.mapper.toAuctionAdvertisement
import com.seif.booksislandapp.data.remote.dto.adv.auction.AuctionAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
import com.seif.booksislandapp.domain.repository.UserRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuctionAdvertisementRepositoryImpTest {
    private lateinit var auctionAdvertisemntRepositoryImp: AuctionAdvertisementRepositoryImp

    // Mock dependencies
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: StorageReference
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var auctionAdvertisementDto1: AuctionAdvertisementDto
    private lateinit var auctionAdvertisementDto2: AuctionAdvertisementDto
    private lateinit var querySnapshot: QuerySnapshot // used in case of get() function and put it in
    private lateinit var queryDocumentSnapshot1: QueryDocumentSnapshot
    private lateinit var queryDocumentSnapshot2: QueryDocumentSnapshot

    private lateinit var userRepository: UserRepository

    private lateinit var auctionAdvertisement: AuctionAdvertisement
    private lateinit var auctionAdvertisement2: AuctionAdvertisement

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
        auctionAdvertisementDto1 = mockk()
        auctionAdvertisementDto2 = mockk()

        // mock ads
        auctionAdvertisement = mockk()
        auctionAdvertisement2 = mockk()

        // querySnapshot and queryDocumentSnapshot
        querySnapshot = mockk()
        queryDocumentSnapshot1 = mockk()
        queryDocumentSnapshot2 = mockk()

        // collection reference
        collectionReference = mockk()

        // query
        query = mockk()

        // user repository
        userRepository = mockk(relaxed = true)

        auctionAdvertisemntRepositoryImp = AuctionAdvertisementRepositoryImp(
            firestore = firestore,
            connectivityManager = connectivityManager,
            storageReference = storage,
            resourceProvider = resourceProvider,
            userRepository = userRepository
        )
    }


    @Test
    fun `searchAuctionsAdv(), when there is no internet connection, then return ResourceFailure with no internet connection message`() =
        runBlocking {
            /** Arrange **/
            /** Arrange **/
            val errorMessage = "No internet connection"
            // Mock network connectivity
            every { connectivityManager.checkInternetConnection() } returns false
            // Mock resource strings
            every { resourceProvider.string(any()) } returns errorMessage

            /** Act **/
            /** Act **/
            val result = auctionAdvertisemntRepositoryImp.searchAuctionsAdv("dfsdf")

            /** Assert **/

            /** Assert **/
            println(result.toString())
            assertTrue(result is Resource.Error)
            val successResult = result as Resource.Error
            assertEquals(errorMessage, successResult.message)
        }

    @Test
    fun `searchAuctionsAdv(), when there is internet connection but there is a thrown exception, then return ResourceFailure with no internet connection message`() =
        runBlocking {
            /** Arrange **/
            /** Arrange **/
            val firebaseErrorMessage = "firebase exception"
            every { connectivityManager.checkInternetConnection() } returns true
            every { firestore.collection(any()) } throws FirebaseException(firebaseErrorMessage)

            /** Act **/
            /** Act **/
            val result = auctionAdvertisemntRepositoryImp.searchAuctionsAdv("sadf")

            /** Assert **/

            /** Assert **/
            println(result.toString())
            assertTrue(result is Resource.Error)
            val successResult = result as Resource.Error
            assertEquals(firebaseErrorMessage, successResult.message)
        }


    @Test
    fun `searchAuctionsAdv(), when there is an internet connection, then return ResourceSuccess with Array List of SellAdvertisement`() =
        runBlocking {
            /** Arrange **/
            /** Arrange **/

            // Mock network connectivity
            every { connectivityManager.checkInternetConnection() } returns true

            // Mock Firestore query snapshot and QueryDocumentSnapshot
            every { queryDocumentSnapshot1.toObject(AuctionAdvertisementDto::class.java) } returns auctionAdvertisementDto1 // ==> document.toObject(SellAdvertisementDto::class.java)
            every { queryDocumentSnapshot2.toObject(AuctionAdvertisementDto::class.java) } returns auctionAdvertisementDto2
            //  every { querySnapshot.documents } returns listOf(queryDocumentSnapshot1, queryDocumentSnapshot2)

            every { querySnapshot.iterator() } returns mutableListOf(
                queryDocumentSnapshot1,
                queryDocumentSnapshot2
            ).iterator() // ==> for (document in querySnapshot.iterator())

            // Mock Firestore collection reference
            // Mock Firestore query

            every { firestore.collection(any()) } returns collectionReference // ==>  firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
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

            // Mock SellAdvertisement mapping
            every { auctionAdvertisementDto1.toAuctionAdvertisement() } returns auctionAdvertisement
            every { auctionAdvertisementDto2.toAuctionAdvertisement() } returns auctionAdvertisement2

            val filteredAuctions = arrayListOf(auctionAdvertisementDto1)
            every {
                arrayListOf(
                    auctionAdvertisementDto1,
                    auctionAdvertisementDto1
                ).filter(any())
            } returns filteredAuctions
            every { auctionAdvertisementDto1.toAuctionAdvertisement() } returns auctionAdvertisement

            /** Act **/
            /** Act **/
            val result = auctionAdvertisemntRepositoryImp.searchAuctionsAdv("sld")
            println(result)
            /** Assert **/
            /** Assert **/
            // Verify the expected interactions
            coVerify { firestore.collection(Constants.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION) }
            coVerify {
                collectionReference.whereNotEqualTo(
                    "auctionStatus",
                    AuctionStatus.CLOSED.toString()
                )
            }
            coVerify { query.orderBy("auctionStatus") }
            coVerify { query.orderBy("publishDate", Query.Direction.DESCENDING) }
            coVerify { query.get() }

            // Assert the result
            println(result.toString())
            assertTrue(result is Resource.Success)
            val successResult = result as Resource.Success
            assertEquals(2, successResult.data.size)
            assertEquals(auctionAdvertisement, successResult.data[0])
            assertEquals(auctionAdvertisement2, successResult.data[1])
        }
}