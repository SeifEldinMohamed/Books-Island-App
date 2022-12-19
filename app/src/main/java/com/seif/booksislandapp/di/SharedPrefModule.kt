package com.seif.booksislandapp.di

import android.content.Context
import com.seif.booksislandapp.utils.SharedPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPrefModule {

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context): SharedPrefs {
        return SharedPrefs(context)
    }
}