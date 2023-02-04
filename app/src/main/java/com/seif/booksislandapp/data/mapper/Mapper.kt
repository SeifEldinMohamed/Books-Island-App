package com.seif.booksislandapp.data.mapper

import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.adv.SellAdvertisementDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement

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
        id = id,
        avatarImage = avatarImage,
        username = username,
        email = email,
        password = password,
        governorate = governorate,
        district = district,
        gender = gender
    )
}

fun SellAdvertisementDto.toSellAdvertisement(): SellAdvertisement {
    return SellAdvertisement(
        id = id,
        ownerId = ownerId,
        book = book,
        status = status,
        publishTime = publishTime,
        location = location,
        price = price
    )
}
fun SellAdvertisement.toSellAdvertisementDto(): SellAdvertisementDto {
    return SellAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book,
        status = status,
        publishTime = publishTime,
        location = location,
        price = price
    )
}