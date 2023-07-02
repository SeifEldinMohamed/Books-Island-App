package com.seif.booksislandapp.utils

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil.load
import com.seif.booksislandapp.R

fun ImageView.setBookDrawableImage(@DrawableRes resource: Int) {
    this.load(resource) {
        crossfade(true)
        placeholder(R.drawable.book_placeholder)
    }
}

fun ImageView.setBookUriImage(uri: Uri) {
    this.load(uri) {
        crossfade(true)
        placeholder(R.drawable.book_placeholder)
    }
}