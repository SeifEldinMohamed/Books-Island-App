package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toAuctionAdvertisement
import com.seif.booksislandapp.data.mapper.toAuctionAdvertisementDto
import com.seif.booksislandapp.data.mapper.toBidderDto
import com.seif.booksislandapp.data.remote.dto.adv.auction.AuctionAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.domain.repository.AuctionAdvertisementRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.Exception
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
            withTimeout(Constants.TIMEOUT) {
                val docReference = firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                val querySnapshot = docReference.orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val auctionsAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
                for (document in querySnapshot) {
                    val auctionAdvertisementDto =
                        document.toObject(AuctionAdvertisementDto::class.java)
                    // update auction status
                    auctionAdvertisementDto.auctionStatus = calculateAuctionStatus(
                        auctionAdvertisementDto.postDuration.toInt(),
                        auctionAdvertisementDto.closeDate!!
                    )
                    val updateMap: MutableMap<String, Any> = HashMap()
                    updateMap["auctionStatus"] = auctionAdvertisementDto.auctionStatus.toString()
                    docReference.document(document.id).update(updateMap).await()
                    // add ad after updating its auctionStatusValue
                    auctionsAdvertisementsDto.add(auctionAdvertisementDto)
                }
                Timber.d("getAllAuctionsAds: $auctionsAdvertisementsDto")

                Resource.Success(
                    data = auctionsAdvertisementsDto.map { auctionAdvertisementDto ->
                        auctionAdvertisementDto.toAuctionAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            if (e is TimeoutCancellationException) {
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))
            } else {
                Resource.Error(e.message.toString())
            }
        }
    }

    private fun calculateAuctionStatus(postDuration: Int, closeDate: Date): AuctionStatus {
        val currentDate = Date()
        val diff = (closeDate.time - currentDate.time).toDouble()
        Timber.d("calculateAuctionStatus: diff in milliseconds$diff")
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val daysRemaining = hours / 24
        Timber.d("calculateAuctionStatus: days remaining$daysRemaining")
        val auctionLifeTime = postDuration - daysRemaining
        Timber.d("calculateAuctionStatus: auctionLifeTime$auctionLifeTime")
        val firstPeriod = postDuration / 3
        Timber.d("calculateAuctionStatus: first period$firstPeriod")
        val secondPeriod = firstPeriod * 2
        Timber.d("calculateAuctionStatus: second period $secondPeriod")

        return if (auctionLifeTime <= firstPeriod) {
            // started
            AuctionStatus.STARTED
        } else if (auctionLifeTime > firstPeriod && auctionLifeTime <= secondPeriod) {
            // middle
            AuctionStatus.MIDDLE
        } else if (auctionLifeTime > secondPeriod && auctionLifeTime < postDuration) {
            // closing
            AuctionStatus.CLOSING
        } else {
            // closed
            AuctionStatus.CLOSED
        }
    }

    override suspend fun searchAuctionsAdv(searchQuery: String): Resource<ArrayList<AuctionAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot =
                    firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val auctionsAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
                for (document in querySnapshot) {
                    val auctionAdvertisementDto =
                        document.toObject(AuctionAdvertisementDto::class.java)
                    auctionsAdvertisementsDto.add(auctionAdvertisementDto)
                }
                Timber.d("searchAuctionsAdv: $auctionsAdvertisementsDto")
                val s =
                    auctionsAdvertisementsDto.filter { it.book!!.title.contains(searchQuery, true) }
                        .map { it.toAuctionAdvertisement() }
                        .toCollection(ArrayList())
                Timber.d("searchAuctionsAdv: $s")
                Resource.Success(
                    s
                )
            }
        } catch (e: Exception) {
            if (e is TimeoutCancellationException) {
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))
            } else {
                Resource.Error(e.message.toString())
            }
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
                    withTimeout(Constants.TIMEOUT) {
                        val document =
                            firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                .document()
                        auctionAdvertisement.id = document.id
                        auctionAdvertisement.book.images = result.data
                        document.set(auctionAdvertisement.toAuctionAdvertisementDto()).await()
                        Timber.d("uploaded successfully")
                        Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                    }
                } catch (e: Exception) {
                    if (e is TimeoutCancellationException) {
                        Resource.Error(resourceProvider.string(R.string.no_internet_connection))
                    } else {
                        Resource.Error(e.message.toString())
                    }
                }
            }
        }
    }

    private suspend fun uploadMultipleImages(
        ownerId: String,
        imagesUri: List<Uri>
    ): Resource<List<Uri>, String> {
        return try {
            withTimeout(Constants.TIMEOUT) {
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
            }
        } catch (e: Exception) {
            Timber.d("upload images Error: ${e.message}")
            if (e is TimeoutCancellationException) {
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))
            } else {
                Resource.Error(e.message.toString())
            }
        }
    }

    override suspend fun fetchRelatedAuctionAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<AuctionAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot =
                    firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val auctionAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
                for (document in querySnapshot) {
                    val auctionAdvertisementDto =
                        document.toObject(AuctionAdvertisementDto::class.java)
                    auctionAdvertisementsDto.add(auctionAdvertisementDto)
                }
                Resource.Success(
                    auctionAdvertisementsDto.filter { it.book!!.category == category && it.id != adId }
                        .map { it.toAuctionAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            if (e is TimeoutCancellationException) {
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))
            } else {
                Resource.Error(e.message.toString())
            }
        }
    }

    override suspend fun fetchAuctionAdByIdQuerySnapShot(adId: String) =
        callbackFlow {
            try {
                withTimeout(Constants.TIMEOUT) {
                    firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereEqualTo("id", adId)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                Timber.d("fetchAuctionAdByIdQuerySnapShot: listen failed $error")
                                return@addSnapshotListener
                            }
                            if (value != null) {
                                var auction: AuctionAdvertisementDto? = null
                                for (document in value) {
                                    auction = document.toObject(AuctionAdvertisementDto::class.java)
                                }
                                Timber.d("fetchAuctionAdByIdQuerySnapShot: auction retrieved: $auction")
                                trySend(
                                    Resource.Success(
                                        auction!!.toAuctionAdvertisement()
                                    )
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                trySend(
                    if (e is TimeoutCancellationException) {
                        Resource.Error(resourceProvider.string(R.string.no_internet_connection))
                    } else {
                        Resource.Error(e.message.toString())
                    }
                )
            }

            awaitClose { }
        }

    override suspend fun addBidder(adId: String, bidder: Bidder): Resource<String, String> {
        return try {
            withTimeout(Constants.TIMEOUT) {
                val docReference =
                    firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION).document(adId)
                docReference.update("bidders", FieldValue.arrayUnion(bidder.toBidderDto())).await()
                Resource.Success(resourceProvider.string(R.string.add_bid_successfully))
            }
        } catch (e: Exception) {
            if (e is TimeoutCancellationException) {
                Resource.Error(resourceProvider.string(R.string.no_internet_connection))
            } else {
                Resource.Error(e.message.toString())
            }
        }
    }
}