package com.seif.booksislandapp.utils

class Constants {
    companion object {
        // Pagination
        const val PAGE_SIZE = 15

        // FireStore
        const val USER_FIRESTORE_COLLECTION = "users"
        const val GOVERNORATES_FIRESTORE_COLLECTION = "governorate"
        const val DISTRICTS_FIRESTORE_COLLECTION = "districts"
        const val SELL_ADVERTISEMENT_FIRESTORE_COLLECTION = "sell advertisements"
        const val DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION = "donate advertisement"

        // Storage
        const val FIREBASE_STORAGE_ROOT_DIRECTORY = "app"

        // Shared Preference Keys
        const val USER_ID_KEY = "user_id"
        const val USERNAME_KEY = "user_name"
        const val USER_GOVERNORATE_KEY = "user_governorate"
        const val USER_DISTRICT_KEY = "user_district"
        const val USER_AVATAR_KEY = "user_avatar"
        const val IS_FIRST_TIME_KEY = "is_first_time"
        const val IS_LOGGED_IN_KEY = "is_logged_in"

        // Upload
        const val MAX_UPLOADED_IMAGES_NUMBER = 5

        // Splash
        const val HANDLER_DELAY = 2000L
        const val MAX_PROGRESS_BAR = 1000
        const val CURRENT_PROGRESS_ANIMATION = 1500
        const val ANIMATION_DURATION = 1000L
    }
}
