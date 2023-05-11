package com.seif.booksislandapp.data.remote.dto.notification

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("notification") val fcmMessageDto: FCMMessageDto,
    @SerializedName("to") val token: String = ""
)
