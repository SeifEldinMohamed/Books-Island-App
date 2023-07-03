package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.*
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.adv.donation.DonateAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.sell.SellAdvertisementDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.SELL_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
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
import javax.inject.Inject

class AdvertisementRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : AdvertisementRepository {

    override suspend fun getAllSellAds(): Resource<ArrayList<SellAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot = firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereNotEqualTo("status", AdvStatus.Closed.toString())
                    .orderBy("status")
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val sellAdvertisementsDto = arrayListOf<SellAdvertisementDto>()
                for (document in querySnapshot) {
                    val sellAdvertisementDto = document.toObject(SellAdvertisementDto::class.java)
                    sellAdvertisementsDto.add(sellAdvertisementDto)
                }
                Resource.Success(
                    data = sellAdvertisementsDto.map { sellAdvertisementDto ->
                        sellAdvertisementDto.toSellAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun getSellAdsByFilter(
        filterBy: FilterBy
    ): Resource<ArrayList<SellAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot = firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereNotEqualTo("status", AdvStatus.Closed.toString())
                    .orderBy("status")
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val sellAdvertisementsDto = arrayListOf<SellAdvertisementDto>()
                for (document in querySnapshot) {
                    val sellAdvertisementDto = document.toObject(SellAdvertisementDto::class.java)
                    sellAdvertisementsDto.add(sellAdvertisementDto)
                }
                Resource.Success(
                    buyFilterResult(sellAdvertisementsDto, filterBy)
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun uploadSellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return when (
            val result =
                uploadMultipleImages(sellAdvertisement.ownerId, sellAdvertisement.book.images)
        ) {
            is Resource.Error -> {
                Timber.d("uploadSellAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    withTimeout(Constants.TIMEOUT_UPLOAD) {
                        val document =
                            firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION).document()
                        sellAdvertisement.id = document.id
                        sellAdvertisement.book.images = result.data
                        document.set(sellAdvertisement.toSellAdvertisementDto()).await()
                        Timber.d("uploaded successfully")
                        Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                    }
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    override suspend fun editMySellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        val imagesToUpload =
            sellAdvertisement.book.images.filter { !it.toString().contains("https") }
        val oldUploadedImages =
            sellAdvertisement.book.images.filter { it.toString().contains("https") }
        return when (val result = uploadMultipleImages(sellAdvertisement.ownerId, imagesToUpload)) {
            is Resource.Error -> {
                Timber.d("uploadSellAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    withTimeout(Constants.TIMEOUT_UPLOAD) {
                        val document =
                            firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                .document(sellAdvertisement.id)
                        sellAdvertisement.book.images = oldUploadedImages + result.data
                        Timber.d("all images ${sellAdvertisement.book.images}")
                        document.set(sellAdvertisement.toSellAdvertisementDto())
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

    override suspend fun deleteMySellAdv(mySellAdId: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .document(mySellAdId)
                    .delete()
                    .await()
                Timber.d("Deleted successfully")
                Resource.Success("Advertisement Deleted Successfully")
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun searchSellAdv(searchQuery: String): Resource<ArrayList<SellAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot =
                    firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val sellAdvertisementsDto = arrayListOf<SellAdvertisementDto>()
                for (document in querySnapshot) {
                    val sellAdvertisementDto = document.toObject(SellAdvertisementDto::class.java)
                    sellAdvertisementsDto.add(sellAdvertisementDto)
                }

                Timber.d("searchSellAdv: $sellAdvertisementsDto")
                Resource.Success(
                    sellAdvertisementsDto.filter { it.book!!.title.contains(searchQuery, true) }
                        .map { it.toSellAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun getUserById(id: String): Resource<User, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                delay(500) // to show loading progress
                val querySnapshot = firestore.collection(USER_FIRESTORE_COLLECTION).document(id)
                    .get()
                    .await()
                val user = querySnapshot.toObject(UserDto::class.java)
                Resource.Success(
                    data = user!!.toUser()
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchRelatedSellAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<SellAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot =
                    firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val sellAdvertisementsDto = arrayListOf<SellAdvertisementDto>()
                for (document in querySnapshot) {
                    val sellAdvertisementDto = document.toObject(SellAdvertisementDto::class.java)
                    sellAdvertisementsDto.add(sellAdvertisementDto)
                }
                Resource.Success(
                    sellAdvertisementsDto.filter { it.book!!.category == category && it.id != adId }
                        .map { it.toSellAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    /** Donation ( extract donation functions to another repository ) **/

    override suspend fun getAllDonateAds(): Resource<ArrayList<DonateAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                delay(500) // to show loading progress

                val querySnapshot = firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereNotEqualTo("status", AdvStatus.Closed.toString())
                    .orderBy("status")
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
                for (document in querySnapshot) {
                    val donateAdvertisementDto =
                        document.toObject(DonateAdvertisementDto::class.java)
                    donateAdvertisementsDto.add(donateAdvertisementDto)
                }
                // Log.d(TAG, "getAllDonateAds: ${donateAdvertisementsDto.first()}")
                Resource.Success(
                    data = donateAdvertisementsDto.map { donateAdvertisementDto ->
                        donateAdvertisementDto.toDonateAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun searchDonateAdv(searchQuery: String): Resource<ArrayList<DonateAdvertisement>, String> {

        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {

                val querySnapshot =
                    firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
                for (document in querySnapshot) {
                    val donateAdvertisementDto =
                        document.toObject(DonateAdvertisementDto::class.java)
                    donateAdvertisementsDto.add(donateAdvertisementDto)
                }
                Timber.d("searchDonateAdv: $donateAdvertisementsDto")
                Resource.Success(
                    donateAdvertisementsDto.filter { it.book!!.title.contains(searchQuery, true) }
                        .map { it.toDonateAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchRelatedDonateAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<DonateAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                val querySnapshot =
                    firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                        .whereNotEqualTo("status", AdvStatus.Closed.toString())
                        .orderBy("status")
                        .orderBy("publishDate", Query.Direction.DESCENDING)
                        .get()
                        .await()

                val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
                for (document in querySnapshot) {
                    val donateAdvertisementDto =
                        document.toObject(DonateAdvertisementDto::class.java)
                    donateAdvertisementsDto.add(donateAdvertisementDto)
                }
                Resource.Success(
                    donateAdvertisementsDto.filter { it.book!!.category == category && it.id != adId }
                        .map { it.toDonateAdvertisement() }
                        .toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
    private fun donateFilterResult(
        donateAdvertisementsDto: ArrayList<DonateAdvertisementDto>,
        filterBy: FilterBy
    ): ArrayList<DonateAdvertisement> {
        return if (filterBy.condition != null && filterBy.condition.split('&').size> 1) {
            donateAdvertisementsDto.filter { ad ->
                (filterBy.category == null || ad.book?.category == filterBy.category) &&
                    (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                    (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                    (filterBy.condition.split('&').first() == ad.book?.condition || ad.book?.condition == filterBy.condition.split('&')[1])
            }
                .map { it.toDonateAdvertisement() }
                .toCollection(ArrayList())
        } else {
            donateAdvertisementsDto.filter { ad ->
                (filterBy.category == null || ad.book?.category == filterBy.category) &&
                    (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                    (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                    (filterBy.condition == null || ad.book?.condition == filterBy.condition)
            }
                .map { it.toDonateAdvertisement() }
                .toCollection(ArrayList())
        }
    }

    private fun buyFilterResult(
        sellAdvertisementsDto: ArrayList<SellAdvertisementDto>,
        filterBy: FilterBy
    ): ArrayList<SellAdvertisement> {
        return if (filterBy.condition != null && filterBy.condition.split('&').size> 1) {
            sellAdvertisementsDto.filter { ad ->
                (filterBy.category == null || ad.book?.category == filterBy.category) &&
                    (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                    (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                    (filterBy.condition.split('&').first() == ad.book?.condition || ad.book?.condition == filterBy.condition.split('&')[1])
            }
                .map { it.toSellAdvertisement() }
                .toCollection(ArrayList())
        } else {
            sellAdvertisementsDto.filter { ad ->
                (filterBy.category == null || ad.book?.category == filterBy.category) &&
                    (filterBy.governorate == null || ad.location.startsWith("${filterBy.governorate}")) &&
                    (filterBy.district == null || ad.location == "${filterBy.governorate} - ${filterBy.district}") &&
                    (filterBy.condition == null || ad.book?.condition == filterBy.condition)
            }
                .map { it.toSellAdvertisement() }
                .toCollection(ArrayList())
        }
    }
    override suspend fun getDonateAdsByFilter(
        filterBy: FilterBy
    ): Resource<ArrayList<DonateAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val querySnapshot = firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereNotEqualTo("status", AdvStatus.Closed.toString())
                    .orderBy("status")
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
                for (document in querySnapshot) {
                    val donateAdvertisementDto =
                        document.toObject(DonateAdvertisementDto::class.java)
                    donateAdvertisementsDto.add(donateAdvertisementDto)
                }
                Resource.Success(
                    donateFilterResult(donateAdvertisementsDto, filterBy)
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun uploadDonateAdv(donateAdvertisement: DonateAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return when (
            val result =
                uploadMultipleImages(donateAdvertisement.ownerId, donateAdvertisement.book.images)
        ) {
            is Resource.Error -> {
                Timber.d("uploadSellAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    withTimeout(Constants.TIMEOUT_UPLOAD) {

                        val document =
                            firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                .document()
                        donateAdvertisement.id = document.id
                        donateAdvertisement.book.images = result.data
                        document.set(donateAdvertisement.toDonateAdvertisementDto()).await()
                        Timber.d("uploaded successfully")
                        Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                    }
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    override suspend fun uploadExchangeAdv(exchangeAdvertisement: ExchangeAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return when (
            val result = uploadMultipleImages(
                exchangeAdvertisement.ownerId,
                exchangeAdvertisement.book.images
            )
        ) {
            is Resource.Error -> {
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                // exchangeAdvertisement.booksToExchange.map { it.imageUri!! }
                try {
                    when (
                        val booksForExchangeResult = uploadMultipleImages(
                            exchangeAdvertisement.ownerId,
                            exchangeAdvertisement.booksToExchange.map { it.imageUri!! }
                        )
                    ) {
                        is Resource.Error -> {
                            Resource.Error(booksForExchangeResult.message)
                        }
                        is Resource.Success -> {
                            val document =
                                firestore.collection(EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                    .document()
                            exchangeAdvertisement.id = document.id
                            exchangeAdvertisement.book.images = result.data
//                            exchangeAdvertisement.booksToExchange.map { it.imageUri= booksForExchangeResult.data}
                            for (i in 0 until booksForExchangeResult.data.size) {
                                exchangeAdvertisement.booksToExchange[i].imageUri =
                                    booksForExchangeResult.data[i]
                            }
                            document.set(exchangeAdvertisement.toExchangeAdvertisementDto()).await()
                            Timber.d("uploaded successfully")
                            Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                        }
                    }
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    override suspend fun fetchMyDonateAds(userId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection())
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereEqualTo("ownerId", userId)
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .addSnapshotListener { donationSnapShot, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.message.toString()))
                        }
                        if (donationSnapShot != null) {
                            val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
                            for (document in donationSnapShot) {
                                val donateAdvertisementDto =
                                    document.toObject(DonateAdvertisementDto::class.java)
                                donateAdvertisementsDto.add(donateAdvertisementDto)
                            }
                            trySend(
                                Resource.Success(
                                    data = donateAdvertisementsDto.map { donateAdvertisementDto ->
                                        donateAdvertisementDto.toDonateAdvertisement()
                                    }.toCollection(ArrayList())
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

    override suspend fun editMyDonateAdv(donateAdvertisement: DonateAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        val imagesToUpload =
            donateAdvertisement.book.images.filter { !it.toString().contains("https") }
        val oldUploadedImages =
            donateAdvertisement.book.images.filter { it.toString().contains("https") }
        return when (
            val result =
                uploadMultipleImages(donateAdvertisement.ownerId, imagesToUpload)
        ) {
            is Resource.Error -> {
                Timber.d("uploadDonateAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    withTimeout(Constants.TIMEOUT_UPLOAD) {
                        val document =
                            firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                                .document(donateAdvertisement.id)
                        donateAdvertisement.book.images = oldUploadedImages + result.data
                        Timber.d("all images ${donateAdvertisement.book.images}")
                        document.set(donateAdvertisement.toDonateAdvertisementDto())
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

    override suspend fun deleteMyDonateAdv(myDonateAdId: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .document(myDonateAdId)
                    .delete()
                    .await()
                Timber.d("Deleted successfully")
                Resource.Success("Advertisement Deleted Successfully")
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchMySellAds(userId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection())
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        else {
            try {
                firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .whereEqualTo("ownerId", userId)
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .addSnapshotListener { mySellAdsQuerySnapshot, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.message.toString()))
                        }
                        if (mySellAdsQuerySnapshot != null) {
                            val sellAdvertisementsDto = arrayListOf<SellAdvertisementDto>()
                            for (document in mySellAdsQuerySnapshot) {
                                val sellAdvertisementDto =
                                    document.toObject(SellAdvertisementDto::class.java)
                                sellAdvertisementsDto.add(sellAdvertisementDto)
                            }
                            trySend(
                                Resource.Success(
                                    data = sellAdvertisementsDto.map { sellAdvertisementDto ->
                                        sellAdvertisementDto.toSellAdvertisement()
                                    }.toCollection(ArrayList())
                                )
                            )
                        }
                    }
            } catch (e: Exception) {
                trySend(Resource.Error(e.message.toString()))
            }
        }
        awaitClose { }
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

    override suspend fun fetchBuyWishListAds(buyIdList: List<String>): Resource<ArrayList<SellAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val sellAdvertisementsDto = arrayListOf<SellAdvertisementDto>()
                for (item in buyIdList) {
                    val querySnapshot =
                        firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
                            .document(item)
                            .get()
                            .await()
                    val sellAdvertisementDto =
                        querySnapshot.toObject(SellAdvertisementDto::class.java)
                    if (sellAdvertisementDto!!.status.toString() == "Opened")
                        sellAdvertisementsDto.add(sellAdvertisementDto)
                }
                Resource.Success(
                    data = sellAdvertisementsDto.map { sellAdvertisementDto ->
                        sellAdvertisementDto.toSellAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun fetchDonateWishListAds(donateIdList: List<String>): Resource<ArrayList<DonateAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            withTimeout(Constants.TIMEOUT) {
                val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
                for (item in donateIdList) {
                    val querySnapshot =
                        firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                            .document(item)
                            .get()
                            .await()
                    val donateAdvertisementDto =
                        querySnapshot.toObject(DonateAdvertisementDto::class.java)
                    if (donateAdvertisementDto!!.status.toString() == "Opened")
                        donateAdvertisementsDto.add(donateAdvertisementDto)
                }
                Resource.Success(
                    data = donateAdvertisementsDto.map { donateAdvertisementDto ->
                        donateAdvertisementDto.toDonateAdvertisement()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}
