package com.seif.booksislandapp.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.seif.booksislandapp.utils.Constants.Companion.TOKENS_FIIRESTORE_COLLECTION
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseInstanceIdService : FirebaseMessagingService() {
    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var fcm: FirebaseMessaging

    // Override onNewToken to get new token then save it in firestore for later usage
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Timber.d("onNewToken: called $s")
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fcm.token.addOnSuccessListener { token ->
                Timber.d("fcm token: success $token")
                // update token
                firestore.collection(TOKENS_FIIRESTORE_COLLECTION).document(currentUser.uid)
                    .set(hashMapOf("token" to token))
            }.addOnFailureListener { e ->
                Timber.d("fcm token:: Error: $e")
            }
        }
    }
}