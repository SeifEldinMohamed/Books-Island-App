package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toExchangeAdvertisement
import com.seif.booksislandapp.data.remote.dto.adv.exchange.ExchangeAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.repository.ExchangeAdvertisementRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class ExchangeAdvertisementRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val resourceProvider: ResourceProvider,
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
}