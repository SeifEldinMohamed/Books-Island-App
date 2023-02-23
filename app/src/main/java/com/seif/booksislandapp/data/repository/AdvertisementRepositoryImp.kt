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
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.SELL_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.Exception
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

            val querySnapshot = firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
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
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun uploadSellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return when (val result = uploadMultipleImages(sellAdvertisement.book.images)) {
            is Resource.Error -> {
                Timber.d("uploadSellAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    val document =
                        firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION).document()
                    sellAdvertisement.id = document.id
                    sellAdvertisement.book.images = result.data
                    document.set(sellAdvertisement.toSellAdvertisementDto()).await()
                    Timber.d("uploaded successfully")
                    Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    override suspend fun searchSellAdv(searchQuery: String): Resource<ArrayList<SellAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            val querySnapshot =
                firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
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
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun getUserById(id: String): Resource<User, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress
            val querySnapshot = firestore.collection(USER_FIRESTORE_COLLECTION)
                .whereEqualTo("id", id)
                .get()
                .await()
            val users = arrayListOf<UserDto>()
            for (document in querySnapshot) {
                val user = document.toObject(UserDto::class.java)
                users.add(user)
            }
            Resource.Success(
                data = users.map { it.toUser() }.first()
            )
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
            val querySnapshot =
                firestore.collection(SELL_ADVERTISEMENT_FIRESTORE_COLLECTION)
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
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
    override suspend fun getAllDonateAds(): Resource<ArrayList<DonateAdvertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(500) // to show loading progress

            val querySnapshot = firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                .orderBy("publishDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
            for (document in querySnapshot) {
                val donateAdvertisementDto = document.toObject(DonateAdvertisementDto::class.java)
                donateAdvertisementsDto.add(donateAdvertisementDto)
            }
            // Log.d(TAG, "getAllDonateAds: ${donateAdvertisementsDto.first()}")
            Resource.Success(
                data = donateAdvertisementsDto.map { donateAdvertisementDto ->
                    donateAdvertisementDto.toDonateAdvertisement()
                }.toCollection(ArrayList())
            )
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
    override suspend fun searchDonateAdv(searchQuery: String): Resource<ArrayList<DonateAdvertisement>, String> {

        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            val querySnapshot =
                firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

            val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
            for (document in querySnapshot) {
                val donateAdvertisementDto = document.toObject(DonateAdvertisementDto::class.java)
                donateAdvertisementsDto.add(donateAdvertisementDto)
            }
            Timber.d("searchDonateAdv: $donateAdvertisementsDto")
            Resource.Success(
                donateAdvertisementsDto.filter { it.book!!.title.contains(searchQuery, true) }
                    .map { it.toDonateAdvertisement() }
                    .toCollection(ArrayList())
            )
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
            val querySnapshot =
                firestore.collection(Constants.DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION)
                    .orderBy("publishDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

            val donateAdvertisementsDto = arrayListOf<DonateAdvertisementDto>()
            for (document in querySnapshot) {
                val donateAdvertisementDto = document.toObject(DonateAdvertisementDto::class.java)
                donateAdvertisementsDto.add(donateAdvertisementDto)
            }
            Resource.Success(
                donateAdvertisementsDto.filter { it.book!!.category == category && it.id != adId }
                    .map { it.toDonateAdvertisement() }
                    .toCollection(ArrayList())
            )
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
    override suspend fun uploadDonateAdv(donateAdvertisement: DonateAdvertisement): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return when (val result = uploadMultipleImages(donateAdvertisement.book.images)) {
            is Resource.Error -> {
                Timber.d("uploadSellAdv: Error  ${result.message}")
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    val document =
                        firestore.collection(DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION).document()
                    donateAdvertisement.id = document.id
                    donateAdvertisement.book.images = result.data
                    document.set(donateAdvertisement.toDonateAdvertisementDto()).await()
                    Timber.d("uploaded successfully")
                    Resource.Success("Advertisement Added Successfully with id : ${document.id}")
                } catch (e: Exception) {
                    Resource.Error(e.message.toString())
                }
            }
        }
    }

    private suspend fun uploadMultipleImages(imagesUri: List<Uri>): Resource<List<Uri>, String> {
        return try {
            val uris: List<Uri> = withContext(Dispatchers.IO) {
                // 1,2,3,4
                // 4 async blocks (upload first then download it's url then upload second ....)
                imagesUri.map { imageUri -> // we will map the whole list into the async blocks
                    async {
                        storageReference.child(
                            imageUri.lastPathSegment ?: "${System.currentTimeMillis()}"
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
