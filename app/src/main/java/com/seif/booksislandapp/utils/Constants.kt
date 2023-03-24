package com.seif.booksislandapp.utils

class Constants {
    companion object {

        // Pagination
        const val PAGE_SIZE = 15

        // Timout
        const val TIMEOUT = 10000L // 8 seconds to fetch
        const val TIMEOUT_AUTH = 12000L // 10 seconds to fetch
        const val TIMEOUT_UPLOAD = 12000L // 10 seconds to upload

        // FireStore
        const val USER_FIRESTORE_COLLECTION = "users"
        const val GOVERNORATES_FIRESTORE_COLLECTION = "governorate"
        const val DISTRICTS_FIRESTORE_COLLECTION = "districts"
        const val SELL_ADVERTISEMENT_FIRESTORE_COLLECTION = "sell advertisements"
        const val EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION = "exchange advertisements"
        const val DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION = "donate advertisement"
        const val AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION = "auctions advertisements"

        const val CHATS_FIIRESTORE_COLLECTION: String = "Chats"
        const val MESSAGES_FIIRESTORE_COLLECTION: String = "Messages"

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
        const val MAX_UPLOADED_EXCHANGE_FOR_IMAGES_NUMBER = 4

        // Splash
        const val HANDLER_DELAY = 2000L
        const val MAX_PROGRESS_BAR = 1000
        const val CURRENT_PROGRESS_ANIMATION = 1500
        const val ANIMATION_DURATION = 1000L

        // AVATARS
        val AVATAR_MEN_LIST = arrayListOf(
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_1.png?alt=media&token=06750e12-87c7-480a-a32e-373be9dc8d1f",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_2.png?alt=media&token=eb2cfef6-686f-4354-9d0e-6daf8b800741",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fmen_avatar_3.png?alt=media&token=87de7013-b2a8-4f0c-8bf4-0d7bfed35364",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_4.png?alt=media&token=9cbe0cea-162f-4203-996f-d82957d693e3",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_5.png?alt=media&token=0c13a257-a9df-4ff4-aeff-b8ba0747d379",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_6.png?alt=media&token=700b4ba8-0799-4295-bbf3-6619e1f26802",
        )
        val AVATAR_WOMEN_LIST = arrayListOf(
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_1.png?alt=media&token=3fe755a4-9e34-4806-ad15-885d2a3e0971",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_2.png?alt=media&token=8b314aac-a237-45d8-97a5-e64f910a1297",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_3.png?alt=media&token=329d6fa9-b636-4ad2-8bde-b6914737a329",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_4.png?alt=media&token=b8eb1c6d-4ad6-41d2-8309-a1a12cc4f9fe",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_5.png?alt=media&token=7ab3ec81-a0bb-40ba-8e92-eb1d9fa6d14a",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_6.png?alt=media&token=33205a23-1c55-4a53-83e2-b05663625ca5",
        )
    }
}
