package com.seif.booksislandapp.data.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import coil.ImageLoader
import coil.request.ImageRequest
import coil.target.Target
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.seif.booksislandapp.R
import com.seif.booksislandapp.presentation.home.HomeActivity
import com.seif.booksislandapp.utils.Constants.Companion.NOTIFICATION_CHANNEL_ID
import com.seif.booksislandapp.utils.Constants.Companion.NOT_IN_MYCHATS_OR_CHATROOM
import com.seif.booksislandapp.utils.SharedPrefs
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
open class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sharedPreferences: SharedPrefs

    override fun handleIntent(intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) { // to know the keys and values of the sent message
                val value = bundle.getString(key)
                Timber.d("handle Intent -> Key: $key Value: $value")
            }
        }

        if (bundle == null) return
        if (!sharedPreferences.get(NOT_IN_MYCHATS_OR_CHATROOM, Boolean::class.java)) return

        val body = bundle.getString("gcm.notification.body")
        val image = bundle.getString("gcm.notification.image")
        val title = bundle.getString("gcm.notification.title")
        val senderId = bundle.getString("gcm.notification.senderId")
        Timber.d("handleIntent: receiverId = $senderId")
        Timber.d("FCM Data = body = $body , title = $title , sentImage= $image")
        if (title != null && body != null && image != null && senderId != null)
            sendNotification(title, body, image, senderId)
        super.handleIntent(intent)
    }

    private fun sendNotification(title: String, body: String, image: String, senderId: String) {

        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_nav_graph)
            .setComponentName(HomeActivity::class.java)
            .setDestination(R.id.chatRoomFragment)
            .setArguments(
                Bundle().apply {
                    putString(
                        "userId",
                        senderId
                    )
                }
            ) // replace with deep links to maintain back stack
            .createPendingIntent()
        // Display notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        /**
         * todo: ask for notification permission start from android 13
         **/
        // Since android Oreo (Api:26) notification channel is needed.
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            title,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        if (image != "null") {
            setBigPictureFromBitmap(
                notificationBuilder,
                image,
                notificationManager,
                channel
            )
        } else {
            // we use the CompletableFuture  instance to wait until we add the large icon then we sent the notification
            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(Random.nextInt(), notificationBuilder.build())
        }
    }

    private fun setBigPictureFromBitmap(
        notificationBuilder: NotificationCompat.Builder,
        image: String,
        notificationManager: NotificationManager,
        channel: NotificationChannel
    ) {
        val request = ImageRequest.Builder(this)
            .data(image)
            .target(object : Target {
                override fun onSuccess(result: Drawable) {
                    val bitmap = (result as BitmapDrawable).bitmap
                    // Set the loaded bitmap as the big picture
                    val bigPictureStyle = NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)

                    notificationBuilder.setStyle(bigPictureStyle)

                    notificationManager.createNotificationChannel(channel)
                    notificationManager.notify(
                        0,
                        notificationBuilder.build()
                    )
                }
            }).build()
        // Start the image request
        ImageLoader(this).enqueue(request)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Get message data
        /*
        remoteMessage.data
        Timber.d("onMessageReceived: data = ${remoteMessage.data}")
        Timber.d("onMessageReceived: messageId = ${remoteMessage.messageId}")
        Timber.d("onMessageReceived: senderId = ${remoteMessage.senderId}")
        Timber.d("onMessageReceived: messageType = ${remoteMessage.messageType}")
        Timber.d("onMessageReceived: rawData = ${remoteMessage.rawData}")
        Timber.d("onMessageReceived: to = ${remoteMessage.to}")

        val title = remoteMessage.data["title"]
        Timber.d("onMessageReceived: title$title")
        val body = remoteMessage.data["body"]
        Timber.d("onMessageReceived: body$body")
        val senderId = remoteMessage.data["senderId"]
        Timber.d("onMessageReceived: senderId$senderId")
        val receiverId = remoteMessage.data["receiverId"]
        Timber.d("onMessageReceived: receiverId$receiverId")

        val channelId = "YOUR_CHANNEL_ID"
        // Display notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        val channel = NotificationChannel(
            channelId,
            title,
            NotificationManager.IMPORTANCE_DEFAULT
        )


        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

         */

        // First case when notifications are received via
        // data event Here, 'title' and 'message' are the assumed names
        // of JSON attributes. Since here we do not have any data
        // payload, This section is commented out. It is
        // here only for reference purposes.
        /*if(remoteMessage.getData().size()>0){
            showNotification(remoteMessage.getData().get("title"),
                          remoteMessage.getData().get("message"));
        }*/

        // Second case when notification payload is
        // received.
//        if (remoteMessage.notification != null) {
//            // Since the notification is received directly from
//            // FCM, the title and the body can be fetched
//            // directly as below.
//            showNotification(
//                remoteMessage.notification!!.title!!,
//                remoteMessage.notification!!.body!!
//            )
//        }
    }

//
//    // Method to get the custom Design for the display of
//    // notification.
//    private fun getCustomDesign(
//        title: String,
//        message: String
//    ): RemoteViews {
//        val remoteViews = RemoteViews(
//            ApplicationProvider.getApplicationContext<Context>().getPackageName(),
//            R.layout.notification
//        )
//        remoteViews.setTextViewText(R.id.title, title)
//        remoteViews.setTextViewText(R.id.message, message)
//        remoteViews.setImageViewResource(
//            R.id.icon,
//            R.drawable.gfg
//        )
//        return remoteViews
//    }
//
//    // Method to display the notifications
//    fun showNotification(
//        title: String,
//        message: String
//    ) {
//        // Pass the intent to switch to the MainActivity
//        val intent = Intent(this, HomeActivity::class.java)
//        // Assign channel ID
//        val channel_id = "notification_channel"
//        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
//        // the activities present in the activity stack,
//        // on the top of the Activity that is to be launched
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        // Pass the intent to PendingIntent to start the
//        // next Activity
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT
//        )
//
//        // Create a Builder object using NotificationCompat
//        // class. This will allow control over all the flags
//        var builder: NotificationCompat.Builder = NotificationCompat.Builder(
//            ApplicationProvider.getApplicationContext<Context>(),
//            channel_id
//        )
//            .setSmallIcon(R.drawable.gfg)
//            .setAutoCancel(true)
//            .setVibrate(
//                longArrayOf(
//                    1000, 1000, 1000,
//                    1000, 1000
//                )
//            )
//            .setOnlyAlertOnce(true)
//            .setContentIntent(pendingIntent)
//
//        // A customized design for the notification can be
//        // set only for Android versions 4.1 and above. Thus
//        // condition for the same is checked here.
//        builder = builder.setContent(
//            getCustomDesign(title, message)
//        )
//        // Create an object of NotificationManager class to
//        // notify the
//        // user of events that happen in the background.
//        val notificationManager = ContextCompat.getSystemService(
//            Context.NOTIFICATION_SERVICE
//        ) as NotificationManager?
//        // Check if the Android Version is greater than Oreo
//        val notificationChannel = NotificationChannel(
//            channel_id, "web_app",
//            NotificationManager.IMPORTANCE_HIGH
//        )
//        notificationManager!!.createNotificationChannel(
//            notificationChannel
//        )
//        notificationManager.notify(0, builder.build())
//    }
}

/**
private fun setLargeIconFromBitmap(
notificationBuilder: NotificationCompat.Builder,
userAvatar: String
): CompletableFuture<Bitmap> {
val completableFuture = CompletableFuture<Bitmap>()
val request = ImageRequest.Builder(this)
.data(userAvatar)
.target(object : Target {
override fun onSuccess(result: Drawable) {
val bitmap = (result as BitmapDrawable).bitmap
notificationBuilder.setLargeIcon(bitmap)
completableFuture.complete(bitmap)
}
}).build()
// Start the image request
ImageLoader(this).enqueue(request)
return completableFuture
}
 **/