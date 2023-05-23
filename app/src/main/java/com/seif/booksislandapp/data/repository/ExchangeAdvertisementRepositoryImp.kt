package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toExchangeAdvertisement
import com.seif.booksislandapp.data.mapper.toExchangeAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.exchange.ExchangeAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.repository.ExchangeAdvertisementRepository
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ExchangeAdvertisementRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val resourceProvider: ResourceProvider,
    private val storageReference: StorageReference,
    private val connectivityManager: ConnectivityManager
) :
    ExchangeAdvertisementRepository {
    override suspend fun getAllExchangeAdvertisement(): Resource<ArrayList<ExchangeAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                delay(500) // to show loading progress

                val querySnapshot =
                    firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()
                val exchangeAdvertisementsDto = arrayListOf<ExchangeAdvertisementDto>()
                for (document in querySnapshot) {
                    val exchangeAdvertisementDto =
                        document.toObject(ExchangeAdvertisementDto::class.java)
                    exchangeAdvertisementsDto.add(exchangeAdvertisementDto)
                }

                Resource.Success(
                    data = exchangeAdvertisementsDto.map { exchangeAdvertisementDto ->
                        exchangeAdvertisementDto.toExchangeAdvertisement()
                    }.toCollection(ArrayList())

                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun searchExchangeAdv(searchQuery: String): Resource<ArrayList<ExchangeAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {

                val querySnapshot =
                    firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val exchangeAdvertisementsDto = arrayListOf<ExchangeAdvertisementDto>()
                for (document in querySnapshot) {
                    val exchangeAdvertisementDto =
                        document.toObject(ExchangeAdvertisementDto::class.java)
                    exchangeAdvertisementsDto.add(exchangeAdvertisementDto)
                }

                Resource.Success(
                    exchangeAdvertisementsDto.filter { it.book!!.title.contains(searchQuery, true) }
                        .map { it.toExchangeAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchRelatedExchangeAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<ExchangeAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                val querySnapshot =
                    firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val exchangeAdvertisementsDto = arrayListOf<ExchangeAdvertisementDto>()
                for (document in querySnapshot) {
                    val exchangeAdvertisementDto =
                        document.toObject(ExchangeAdvertisementDto::class.java)
                    exchangeAdvertisementsDto.add(exchangeAdvertisementDto)
                }
                Resource.Success(
                    exchangeAdvertisementsDto.filter { it.book!!.category == category && it.id != adId }
                        .map { it.toExchangeAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchMyExchangeAds(userId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection())
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        else {
            try {
                withTimeout(Constants.TIMEOUT) {
                    firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereEqualTo("ownerId", userId)
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .addSnapshotListener { exchangeQuerySnapShot, error ->
                            if (error != null) {
                                trySend(Resource.Error(error.message.toString()))
                            }
                            if (exchangeQuerySnapShot != null) {
                                val exchangeAdvertisementsDto =
                                    arrayListOf<ExchangeAdvertisementDto>()
                                for (document in exchangeQuerySnapShot) {
                                    val exchangeAdvertisementDto =
                                        document.toObject(ExchangeAdvertisementDto::class.java)
                                    exchangeAdvertisementsDto.add(exchangeAdvertisementDto)
                                }
                                trySend(
                                    Resource.Success(
                                        data = exchangeAdvertisementsDto.map { exchangeAdvertisementDto ->
                                            exchangeAdvertisementDto.toExchangeAdvertisement()
                                        }.toCollection(ArrayList())
                                    )
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                Resource.Error(e.message.toString())
            }
        }
        awaitClose {}
    }

    override suspend fun deleteMyExchangeAdv(myExchangeAdId: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .document(myExchangeAdId)
                    .delete()
                    .await()
                Timber.d("Deleted successfully")
                Resource.Success("Advertisement Deleted Successfully")
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun editMyExchangeAdv(exchangeAdvertisement: ExchangeAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        val imagesToUpload =
            exchangeAdvertisement.book.images.filter { !it.toString().contains("https") }
        val oldUploadedImages =
            exchangeAdvertisement.book.images.filter { it.toString().contains("https") }
        return when (
            val result =
                uploadMultipleImages(exchangeAdvertisement.ownerId, imagesToUpload)
        ) {
            is Resource.Error -> {
                Timber.d("uploadDonateAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                val booksImagesToUpload: List<Uri> = exchangeAdvertisement.booksToExchange.filter {
                    !it.imageUri.toString().contains("https")
                }.map { it.imageUri!! }
                val oldUpdatedBookImages: List<Uri> = exchangeAdvertisement.booksToExchange.filter {
                    it.imageUri.toString().contains("https")
                }.map { it.imageUri!! }
                return when (
                    val result2 =
                        uploadMultipleImages(exchangeAdvertisement.ownerId, booksImagesToUpload)
                ) {
                    is Resource.Error -> {
                        Resource.Error(result2.message)
                    }
                    is Resource.Success -> {
                        try {
                            withTimeout(Constants.TIMEOUT_UPLOAD) {
                                val document =
                                    firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                        .document(exchangeAdvertisement.id)
                                exchangeAdvertisement.book.images =
                                    oldUploadedImages + result.data // for user book images
                                val bookToExchangeImages =
                                    oldUpdatedBookImages + result2.data // for books to exchange images
                                exchangeAdvertisement.booksToExchange.mapIndexed { index, booksToExchange ->
                                    booksToExchange.imageUri = bookToExchangeImages[index]
                                }
                                document.set(exchangeAdvertisement.toExchangeAdvertisementDto())
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
        }
    }

    private suspend fun uploadMultipleImages(
        ownerId: String,
        imagesUri: List<Uri>
    ): Resource<List<Uri>, String> {
        return try {
            withTimeout(Constants.TIMEOUT_UPLOAD) {
                Timber.d("uploadMultipleImages: $imagesUri")
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

    override suspend fun fetchExchangeWishListAds(exchangeIdList: List<String>): Resource<ArrayList<ExchangeAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val exchangeAdvertisementsDto = arrayListOf<ExchangeAdvertisementDto>()
                for (item in exchangeIdList) {
                    val querySnapshot =
                        firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                            .document(item)
                            .get()
                            .await()
                    val exchangeAdvertisementDto =
                        querySnapshot.toObject(ExchangeAdvertisementDto::class.java)
                    if (exchangeAdvertisementDto!!.status.toString() == "Opened")
                        exchangeAdvertisementsDto.add(exchangeAdvertisementDto)
                }
                Resource.Success(
                    data = exchangeAdvertisementsDto.map { exchangeAdvertisementDto ->
                        exchangeAdvertisementDto.toExchangeAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun getExchangeAdsByFilter(filterBy: FilterBy): Resource<ArrayList<ExchangeAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot =
                    firestore.collection(Constants.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()
                val exchangeAdvertisementsDto = arrayListOf<ExchangeAdvertisementDto>()
                for (document in querySnapshot) {
                    val exchangeAdvertisementDto =
                        document.toObject(ExchangeAdvertisementDto::class.java)
                    exchangeAdvertisementsDto.add(exchangeAdvertisementDto)
                }
                Resource.Success(
                    exchangeAdvertisementsDto.filter { ad ->
                        (filterBy.category == null || ad.book?.category == filterBy.category) &&
                                (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                            (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                            (filterBy.condition == null || ad.book?.condition == filterBy.condition)
                    }
                        .map { it.toExchangeAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}