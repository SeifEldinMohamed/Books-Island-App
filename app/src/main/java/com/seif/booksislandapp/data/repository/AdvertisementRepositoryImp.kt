package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toAdvertisement
import com.seif.booksislandapp.data.mapper.toAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.AdvertisementDto
import com.seif.booksislandapp.domain.model.Advertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Constants.Companion.SELL_ADVERTISEMENT_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class AdvertisementRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : AdvertisementRepository {
    override suspend fun getAllSellAds(): Resource<List<Advertisement>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            delay(1000) // to show loading progress

            val querySnapshot = firestore.collection(SELL_ADVERTISEMENT_COLLECTION).get().await()
            val sellAdvertisements = arrayListOf<AdvertisementDto>()
            for (document in querySnapshot) {
                val advertisement = document.toObject(AdvertisementDto::class.java)
                sellAdvertisements.add(advertisement)
            }
            Resource.Success(
                data = sellAdvertisements.map { advertisementDto ->
                    advertisementDto.toAdvertisement()
                }.toCollection(ArrayList())
            )
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun addSellAd(advertisement: Advertisement): Resource<String, String> {
        return when (val result = uploadMultipleImages(advertisement.book.images)) {
            is Resource.Error -> {
                Resource.Error(result.message)
            }
            is Resource.Success -> {
                try {
                    val document = firestore.collection(SELL_ADVERTISEMENT_COLLECTION).document()
                    advertisement.id = document.id
                    document.set(advertisement.toAdvertisementDto()).await()
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
            Resource.Success(uris)
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}
