package com.seif.booksislandapp.utils

class Constants {
    companion object {
        const val PAGE_SIZE = 15

        // firebase
        const val USER_FireStore_Collection = "users"
        const val Governments_FireStore_Collection = "governments"
        const val qesm_FireStore_Collection = "Districts"
        const val FIREBASE_STORAGE_ROOT_DIRECTORY = "app"

        // shared preference keys
        const val USER_KEY = "user"
        const val IS_FIRST_TIME_KEY = "is_first_time"
        const val IS_LOGGED_IN_KEY = "is_logged_in"

        //
        const val SELL_ADVERTISEMENT_COLLECTION = "sell advertisements"

        const val MAX_UPLOADED_IMAGES_NUMBER = 5
    }
}
