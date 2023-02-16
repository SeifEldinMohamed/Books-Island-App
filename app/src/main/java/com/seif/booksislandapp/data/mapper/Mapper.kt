package com.seif.booksislandapp.data.mapper

import android.net.Uri
import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.adv.DonateAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.SellAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.auth.DistrictDto
import com.seif.booksislandapp.data.remote.dto.auth.GovernorateDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.domain.model.book.Book

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
        book = book!!.toBook(),
        status = status!!,
        publishTime = publishTime!!,
        location = location,
        price = price
    )
}
fun DonateAdvertisementDto.toDonateAdvertisement(): DonateAdvertisement {
    return DonateAdvertisement(
        id = id,
        ownerId = ownerId,
        book = book!!.toBook(),
        status = status!!,
        publishTime = publishTime!!,
        location = location,
    )
}

fun DonateAdvertisement.toDonateAdvertisementDto(): DonateAdvertisementDto {
    return DonateAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book.toBookDto(),
        status = status,
        publishTime = publishTime,
        location = location,
    )
}

fun SellAdvertisement.toSellAdvertisementDto(): SellAdvertisementDto {
    return SellAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book.toBookDto(),
        status = status,
        publishTime = publishTime,
        location = location,
        price = price
    )
}

fun Book.toBookDto(): BookDto {
    return BookDto(
        id = id,
        images = images.map { it.toString() },
        title = title,
        author = author,
        category = category,
        condition = isUsed.toString(),
        description = description
    )
}

fun BookDto.toBook(): Book {
    val isUsed: Boolean = when (condition) {
        "Used" -> true
        "New" -> false
        else -> {
            false
        }
    }
    return Book(
        id = id,
        images = images.map { Uri.parse(it) },
        title = title,
        author = author,
        category = category,
        isUsed = isUsed,
        description = description
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