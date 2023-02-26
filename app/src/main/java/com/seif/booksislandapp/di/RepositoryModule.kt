package com.seif.booksislandapp.di

import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.domain.repository.AuctionAdvertisementRepository
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.domain.repository.ExchangeAdvertisementRepository
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.SharedPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        resourceProvider: ResourceProvider,
        sharedPrefs: SharedPrefs,
        connectivityManager: ConnectivityManager
    ): AuthRepository {
        return AuthRepositoryImp(
            firestore,
            auth,
            resourceProvider,
            sharedPrefs,
            connectivityManager
        )
    }

    @Provides
    @Singleton
    fun provideAdvertisementRepository(
        firestore: FirebaseFirestore,
        storageReference: StorageReference,
        resourceProvider: ResourceProvider,
        connectivityManager: ConnectivityManager
    ): AdvertisementRepository {
        return AdvertisementRepositoryImp(
            firestore,
            storageReference,
            resourceProvider,
            connectivityManager
        )
    }

    @Provides
    @Singleton
    fun provideAuctionAdvertisementRepository(
        firestore: FirebaseFirestore,
        storageReference: StorageReference,
        resourceProvider: ResourceProvider,
        connectivityManager: ConnectivityManager
    ): AuctionAdvertisementRepository {
        return AuctionAdvertisementRepositoryImp(
            firestore,
            storageReference,
            resourceProvider,
            connectivityManager
        )
    }
    @Provides
    @Singleton
    fun provideExchangeAdvertisementRepository(
        firestore: FirebaseFirestore,
        resourceProvider: ResourceProvider,
        connectivityManager: ConnectivityManager
    ): ExchangeAdvertisementRepository {
        return ExchangeAdvertisementRepositoryImp(
            firestore,
            resourceProvider,
            connectivityManager
        )
    }
}
