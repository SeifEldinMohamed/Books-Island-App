package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toAuctionAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.repository.AuctionAdvertisementRepository
import com.seif.booksislandapp.utils.Constants.Companion.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class AuctionAdvertisementRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : AuctionAdvertisementRepository {
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