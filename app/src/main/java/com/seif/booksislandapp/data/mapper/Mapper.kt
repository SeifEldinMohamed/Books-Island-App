package com.seif.booksislandapp.data.mapper

import android.net.Uri
import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.adv.auction.AuctionAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.auction.BidderDto
import com.seif.booksislandapp.data.remote.dto.adv.donation.DonateAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.exchange.BooksToExchangeDto
import com.seif.booksislandapp.data.remote.dto.adv.exchange.ExchangeAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.sell.SellAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.auth.DistrictDto
import com.seif.booksislandapp.data.remote.dto.auth.GovernorateDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import com.seif.booksislandapp.domain.model.chat.Message

fun User.toUserDto(): UserDto {
    return UserDto(
        id = this.id,
        avatarImage = this.avatarImage,
        username = this.username,
        email = this.email,
        password = this.password,
        governorate = this.governorate,
        district = this.district,
        gender = this.gender,
        wishListBuy = this.wishListBuy as List<String>,
        wishListDonate = this.wishListDonate as List<String>,
        wishListExchange = this.wishListExchange as List<String>,
        wishListAuction = this.wishListAuction as List<String>,
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
        gender = gender,
        wishListBuy = wishListBuy as ArrayList<String>,
        wishListDonate = wishListDonate as ArrayList<String>,
        wishListExchange = wishListExchange as ArrayList<String>,
        wishListAuction = wishListAuction as ArrayList<String>,
    )
}

fun SellAdvertisementDto.toSellAdvertisement(): SellAdvertisement {
    return SellAdvertisement(
        id = id,
        ownerId = ownerId,
        book = book!!.toBook(),
        status = status!!,
        publishDate = publishDate!!,
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
        publishDate = publishDate!!,
        location = location,
    )
}

fun DonateAdvertisement.toDonateAdvertisementDto(): DonateAdvertisementDto {
    return DonateAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book.toBookDto(),
        status = status,
        publishDate = publishDate,
        location = location,
    )
}
fun AuctionAdvertisement.toAuctionAdvertisementDto(): AuctionAdvertisementDto {
    return AuctionAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book.toBookDto(),
        status = status,
        publishDate = publishDate,
        location = location,
        startPrice = startPrice!!,
        endPrice = endPrice,
        closeDate = closeDate!!,
        postDuration = postDuration,
        auctionStatus = auctionStatus,
        bidders = bidders.map { it.toBidderDto() },
    )
}
fun AuctionAdvertisementDto.toAuctionAdvertisement(): AuctionAdvertisement {
    return AuctionAdvertisement(
        id = id,
        ownerId = ownerId,
        book = book!!.toBook(),
        status = status!!,
        publishDate = publishDate!!,
        location = location,
        startPrice = startPrice,
        endPrice = endPrice,
        closeDate = closeDate!!,
        postDuration = postDuration,
        auctionStatus = auctionStatus!!,
        bidders = bidders!!.map { it.toBidder() }
    )
}

fun ExchangeAdvertisementDto.toExchangeAdvertisement(): ExchangeAdvertisement {
    return ExchangeAdvertisement(
        id = id,
        ownerId = ownerId,
        book = book!!.toBook(),
        status = status!!,
        publishDate = publishDate!!,
        location = location,
        booksToExchange = booksToExchange.map {
            it.toBooksToExchange()
        }

    )
}
fun ExchangeAdvertisement.toExchangeAdvertisementDto(): ExchangeAdvertisementDto {
    return ExchangeAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book.toBookDto(),
        status = status,
        publishDate = publishDate,
        location = location,
        booksToExchange = booksToExchange.map {
            it.toBooksToExchange()
        }
    )
}

fun BooksToExchangeDto.toBooksToExchange(): BooksToExchange {
    return BooksToExchange(
        title = title,
        imageUri = Uri.parse(imageUri),
        author = author

    )
}
fun BooksToExchange.toBooksToExchange(): BooksToExchangeDto {
    return BooksToExchangeDto(
        title = title,
        imageUri = imageUri.toString(),
        author = author

    )
}

fun Bidder.toBidderDto(): BidderDto {
    return BidderDto(
        bidderId = bidderId,
        bidderName = bidderName,
        suggestedPrice = suggestedPrice.toInt()
    )
}

fun BidderDto.toBidder(): Bidder {
    return Bidder(
        bidderId = bidderId,
        bidderName = bidderName,
        suggestedPrice = suggestedPrice.toString()
    )
}

fun SellAdvertisement.toSellAdvertisementDto(): SellAdvertisementDto {
    return SellAdvertisementDto(
        id = id,
        ownerId = ownerId,
        book = book.toBookDto(),
        status = status,
        publishDate = publishDate,
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
        condition = condition.toString(),
        description = description,
        edition = edition
    )
}

fun BookDto.toBook(): Book {
    return Book(
        id = id,
        images = images.map { Uri.parse(it) },
        title = title,
        author = author,
        category = category,
        condition = condition,
        description = description,
        edition = edition
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

fun MessageDto.toMessage(): Message {
    return Message(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        imageUrl = imageUrl,
        date = date!!
    )
}

fun Message.toMessageDto(): MessageDto {
    return MessageDto(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        imageUrl = imageUrl,
        date = date
    )
}