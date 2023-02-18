package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toAuctionAdvertisement
import com.seif.booksislandapp.data.mapper.toAuctionAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.auction.AuctionAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
import com.seif.booksislandapp.domain.repository.AuctionAdvertisementRepository
import com.seif.booksislandapp.utils.Constants.Companion.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AuctionAdvertisementRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : AuctionAdvertisementRepository {
    override suspend fun getAllAuctionsAds(): Resource<ArrayList<AuctionAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress

            val querySnapshot = firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val auctionsAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
            for (document in querySnapshot) {
                val auctionAdvertisementDto = document.toObject(AuctionAdvertisementDto::class.java)
                auctionsAdvertisementsDto.add(auctionAdvertisementDto)
            }
            Timber.d("getAllAuctionsAds: $auctionsAdvertisementsDto")
//            auctionsAdvertisementsDto.map {
//                it.auctionStatus = calculateAuctionStatus(it.postDuration.toInt(), it.closeDate!!)
//            }
            Resource.Success(
                data = auctionsAdvertisementsDto.map { auctionAdvertisementDto ->
                    auctionAdvertisementDto.toAuctionAdvertisement()
                }.toCollection(ArrayList())
            )
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private fun calculateAuctionStatus(postDuration: Int, closeDate: Date): AuctionStatus {
        val currentDate = Date()
        val diff: Long = closeDate.time - currentDate.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val daysRemaining = hours / 24
        Timber.d("calculateAuctionStatus: days remaining$daysRemaining")
        val auctionLifeTime = postDuration - daysRemaining
        Timber.d("calculateAuctionStatus: auctionLifeTime$auctionLifeTime")
        Timber.d("calculateAuctionStatus: postDurationForEachStatus = ${postDuration / 3}")
        return AuctionStatus.STARTED
    }

    override suspend fun searchAuctionsAdv(searchQuery: String): Resource<ArrayList<AuctionAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            val querySnapshot =
                firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .orderBy("publishTime", Query.Direction.DESCENDING)
                    .get()
                    .await()

            val auctionsAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
            for (document in querySnapshot) {
                val auctionAdvertisementDto = document.toObject(AuctionAdvertisementDto::class.java)
                auctionsAdvertisementsDto.add(auctionAdvertisementDto)
            }

            Resource.Success(
                auctionsAdvertisementsDto.filter { it.book!!.title.contains(searchQuery, true) }
                    .map { it.toAuctionAdvertisement() }
                    .toCollection(ArrayList())
            )
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun uploadAuctionAdv(auctionAdvertisement: AuctionAdvertisement): Resource<String, String> {
        // todo save ad id in list of ads in user so we can get those ads of each user in my ads
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return when (
            val result =
                uploadMultipleImages(auctionAdvertisement.ownerId, auctionAdvertisement.book.images)
        ) {
            is Resource.Error -> {
                Timber.d("uploadSellAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    val document =
                        firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                            .document()
                    auctionAdvertisement.id = document.id
                    auctionAdvertisement.book.images = result.data
                    document.set(auctionAdvertisement.toAuctionAdvertisementDto()).await()
                    Timber.d("uploaded successfully")
                    Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    private suspend fun uploadMultipleImages(
        ownerId: String,
        imagesUri: List<Uri>
    ): Resource<List<Uri>, String> {
        return try {
            val uris: List<Uri> = withContext(Dispatchers.IO) {
                // 1,2,3,4
                // 4 async blocks (upload first then download it's url then upload second ....)
                imagesUri.map { imageUri -> // we will map the whole list into the async blocks
                    async {
                        storageReference.child(
                            "$ownerId/${imageUri.lastPathSegment ?: System.currentTimeMillis()}"
                        )
                            .putFile(imageUri)
                            .await()
                            .storage
                            .downloadUrl
                            .await()
                    }
                }.awaitAll()
            }
            Timber.d("upload images Success: $uris")
            Resource.Success(uris)
        } catch (e: Exception) {
            Timber.d("upload images Error: ${e.message}")
            Resource.Error(e.message.toString())
        }
    }
}