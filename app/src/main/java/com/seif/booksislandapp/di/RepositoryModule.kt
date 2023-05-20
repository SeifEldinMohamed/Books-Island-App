package com.seif.booksislandapp.di

import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.data.remote.FCMApiService
import com.seif.booksislandapp.data.repository.*
import com.seif.booksislandapp.domain.repository.*
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
        connectivityManager: ConnectivityManager,
        fcm: FirebaseMessaging
    ): AuthRepository {
        return AuthRepositoryImp(
            firestore,
            auth,
            resourceProvider,
            sharedPrefs,
            connectivityManager,
            fcm
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
        storageReference: StorageReference,
        connectivityManager: ConnectivityManager
    ): ExchangeAdvertisementRepository {
        return ExchangeAdvertisementRepositoryImp(
            firestore,
            resourceProvider,
            storageReference,
            connectivityManager
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        resourceProvider: ResourceProvider,
        sharedPrefs: SharedPrefs,
        connectivityManager: ConnectivityManager
    ): UserRepository {
        return UserRepositoryImp(
            firestore,
            auth,
            resourceProvider,
            sharedPrefs,
            connectivityManager
        )
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        storageReference: StorageReference,
        resourceProvider: ResourceProvider,
        connectivityManager: ConnectivityManager,
        fcmApiService: FCMApiService
    ): ChatRepository {
        return ChatRepositoryImp(
            firestore,
            storageReference,
            resourceProvider,
            connectivityManager,
            fcmApiService
        )
    }

    @Provides
    @Singleton
    fun provideMyChatsRepository(
        firestore: FirebaseFirestore
    ): MyChatsRepository {
        return MyChatsRepositoryImpl(
            firestore
        )
    }
}
