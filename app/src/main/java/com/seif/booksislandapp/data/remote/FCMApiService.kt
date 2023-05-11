package com.seif.booksislandapp.data.remote

import com.seif.booksislandapp.data.remote.dto.notification.NotificationDto
import com.seif.booksislandapp.utils.Constants
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMApiService {
    @Headers(
        "Authorization: key=${Constants.FCM_API_KEY}",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    suspend fun sendNotification(@Body notificationDto: NotificationDto): ResponseBody
}