package com.seif.booksislandapp.di

import android.content.Context
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun resourceProvider(@ApplicationContext context: Context): ResourceProvider {
        return ResourceProvider.Base(context = context)
    }
}