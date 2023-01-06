package com.seif.booksislandapp.data.mapper

import com.seif.booksislandapp.data.remote.dto.AdvertisementDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.domain.model.Advertisement
import com.seif.booksislandapp.domain.model.User

fun User.toUserDto(): UserDto {
    return UserDto(
        id = this.id,
        avatarImage = this.avatarImage,
        username = this.username,
        email = this.email,
        password = this.password,
        governorate = this.governorate,
        district = this.district,
        gender = this.gender
    )
}

fun UserDto.toUser(): User {
    return User(
        id = this.id,
        avatarImage = this.avatarImage,
        username = this.username,
        email = this.email,
        password = this.password,
        governorate = this.governorate,
        district = this.district,
        gender = this.gender
    )
}

fun AdvertisementDto.toAdvertisement(): Advertisement {
    return Advertisement(
        id = id,
        owner = owner,
        book = book,
        isDonateAdv = isDonateAdv,
        status = status,
        publishTime = publishTime
    )
}
fun Advertisement.toAdvertisementDto(): AdvertisementDto {
    return AdvertisementDto(
        id = id,
        owner = owner,
        book = book,
        isDonateAdv = isDonateAdv,
        status = status,
        publishTime = publishTime
    )
}