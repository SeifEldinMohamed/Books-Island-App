package com.seif.booksislandapp.di

import android.content.Context
import android.net.ConnectivityManager
import com.seif.booksislandapp.data.remote.FCMApiService
import com.seif.booksislandapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun checkInternetConnection(context: Context): ConnectivityManager {
        return context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
    }

    @Singleton
    @Provides
    fun provideRetrofitInstance(): FCMApiService {
        return Retrofit.Builder().baseUrl(Constants.FCM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(FCMApiService::class.java)
    }
}