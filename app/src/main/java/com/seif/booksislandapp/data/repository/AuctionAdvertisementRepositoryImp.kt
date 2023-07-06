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
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

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
                val querySnapshot =
                    docReference.whereNotEqualTo("auctionStatus", AuctionStatus.CLOSED.toString())
                        .orderBy("auctionStatus")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val auctionsAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
                for (document in querySnapshot) {
                    val auctionAdvertisementDto =
                        document.toObject(AuctionAdvertisementDto::class.java)
                    // update auction status
                    auctionAdvertisementDto.auctionStatus
                    val newStatus = calculateAuctionStatus(
                        auctionAdvertisementDto.postDuration.toInt(),
                        auctionAdvertisementDto.closeDate!!
                    )
                    // to only update the changed once ( dec response time to 1/2 )
                    if (newStatus != auctionAdvertisementDto.auctionStatus) {
                        auctionAdvertisementDto.auctionStatus = newStatus
                        val updateMap: MutableMap<String, Any> = HashMap()
                        updateMap["auctionStatus"] =
                            auctionAdvertisementDto.auctionStatus.toString()
                        docReference.document(document.id).update(updateMap).await()
                    }
                    // add ad if it's not closed
                    if (auctionAdvertisementDto.auctionStatus != AuctionStatus.CLOSED)
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
            Resource.Error(e.message.toString())
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
                        .whereNotEqualTo("auctionStatus", AuctionStatus.CLOSED.toString())
                        .orderBy("auctionStatus")
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
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun uploadAuctionAdv(auctionAdvertisement: AuctionAdvertisement): Resource<String, String> {
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
                    withTimeout(Constants.TIMEOUT_UPLOAD) {
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
            withTimeout(Constants.TIMEOUT_UPLOAD) {
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
            Resource.Error(e.message.toString())
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
                        .whereNotEqualTo("auctionStatus", AuctionStatus.CLOSED.toString())
                        .orderBy("auctionStatus")
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
            Resource.Error(e.message.toString())
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
                trySend(Resource.Error(e.message.toString()))
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
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchMyAuctionAds(userId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection())
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        else {
            try {
                withTimeout(Constants.TIMEOUT) {
                    firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereEqualTo("ownerId", userId)
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .addSnapshotListener { auctionQuerySnapshot, error ->
                            if (error != null) {
                                trySend(Resource.Error(error.message.toString()))
                            }
                            if (auctionQuerySnapshot != null) {
                                val auctionAdvertisementsDto =
                                    arrayListOf<AuctionAdvertisementDto>()
                                for (document in auctionQuerySnapshot) {
                                    val auctionAdvertisementDto =
                                        document.toObject(AuctionAdvertisementDto::class.java)
                                    auctionAdvertisementsDto.add(auctionAdvertisementDto)
                                }
                                trySend(
                                    Resource.Success(
                                        data = auctionAdvertisementsDto.map { auctionAdvertisementDto ->
                                            auctionAdvertisementDto.toAuctionAdvertisement()
                                        }.toCollection(ArrayList())
                                    )
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                trySend(Resource.Error(e.message.toString()))
            }
        }
        awaitClose { }
    }

    override suspend fun editMyAuctionAdv(auctionAdvertisement: AuctionAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        val imagesToUpload =
            auctionAdvertisement.book.images.filter { !it.toString().contains("https") }
        val oldUploadedImages =
            auctionAdvertisement.book.images.filter { it.toString().contains("https") }
        return when (
            val result =
                uploadMultipleImages(auctionAdvertisement.ownerId, imagesToUpload)
        ) {
            is Resource.Error -> {
                Timber.d("uploadAuctionAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    withTimeout(Constants.TIMEOUT_UPLOAD) {
                        val document =
                            firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                .document(auctionAdvertisement.id)
                        auctionAdvertisement.book.images = oldUploadedImages + result.data
                        Timber.d("all images ${auctionAdvertisement.book.images}")
                        document.set(auctionAdvertisement.toAuctionAdvertisementDto())
                            .await()
                        Timber.d("Updated successfully")
                        Resource.Success("Advertisement Updated Successfully")
                    }
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    override suspend fun deleteMyAuctionAdv(myAuctionAdId: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .document(myAuctionAdId)
                    .delete()
                    .await()
                Timber.d("Deleted successfully")
                Resource.Success("Advertisement Deleted Successfully")
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchAuctionWishListAds(auctionIdList: List<String>): Resource<ArrayList<AuctionAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val auctionAdvertisementsDto = arrayListOf<AuctionAdvertisementDto>()
                for (item in auctionIdList) {
                    val querySnapshot =
                        firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                            .document(item)
                            .get()
                            .await()
                    val auctionAdvertisementDto =
                        querySnapshot.toObject(AuctionAdvertisementDto::class.java)
                    if (auctionAdvertisementDto!!.status.toString() == "Opened")
                        auctionAdvertisementsDto.add(auctionAdvertisementDto)
                }
                Resource.Success(
                    data = auctionAdvertisementsDto.map { auctionAdvertisementDto ->
                        auctionAdvertisementDto.toAuctionAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private fun filterResult(
        auctionAdvertisementsDto: ArrayList<AuctionAdvertisementDto>,
        filterBy: FilterBy
    ): ArrayList<AuctionAdvertisement> {
        return if (filterBy.condition != null && filterBy.condition.split('&').size> 1) {
            auctionAdvertisementsDto.filter { ad ->
                (filterBy.category == null || ad.book?.category == filterBy.category) &&
                    (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                    (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                    (filterBy.condition.split('&').first() == ad.book?.condition || ad.book?.condition == filterBy.condition.split('&')[1])
            }
                .map { it.toAuctionAdvertisement() }
                .toCollection(ArrayList())
        } else {
            auctionAdvertisementsDto.filter { ad ->
                (filterBy.category == null || ad.book?.category == filterBy.category) &&
                    (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                    (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                    (filterBy.condition == null || ad.book?.condition == filterBy.condition)
            }
                .map { it.toAuctionAdvertisement() }
                .toCollection(ArrayList())
        }
    }

    override suspend fun getAuctionAdsByFilter(filterBy: FilterBy): Resource<ArrayList<AuctionAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot = firestore.collection(AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereNotEqualTo("auctionStatus", AuctionStatus.CLOSED.toString())
                    .orderBy("auctionStatus")
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
                    filterResult(auctionAdvertisementsDto, filterBy)
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}