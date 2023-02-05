package com.seif.booksislandapp.data.mapper

import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.adv.SellAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.auth.DistrictDto
import com.seif.booksislandapp.data.remote.dto.auth.GovernorateDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate

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

fun GovernorateDto.toGovernorate(): Governorate {
    return Governorate(
        id = id,
        name = name
    )
}

fun Governorate.toGovernorateDto(): GovernorateDto {
    return GovernorateDto(
        id = id,
        name = name
    )
}
fun DistrictDto.toDistricts(): District {
    return District(
        id = id,
        name = name,
        governorateId = governorateId
    )
}