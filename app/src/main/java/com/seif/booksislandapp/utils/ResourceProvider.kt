package com.seif.booksislandapp.utils

import android.content.Context
import androidx.annotation.StringRes

interface ResourceProvider {
    fun string(@StringRes id: Int): String

    class Base(private val context: Context) : ResourceProvider {
        override fun string(id: Int) = context.getString(id)
    }
}