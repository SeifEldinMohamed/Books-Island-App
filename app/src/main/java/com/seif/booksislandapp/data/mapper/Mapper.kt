package com.seif.booksislandapp.data.mapper

import android.net.Uri
import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.MyChatDto
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
import com.seif.booksislandapp.data.remote.dto.request.RequestDto
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
import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.domain.model.request.MySentRequest

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
        myBuyingChats = myBuyingChats as List<String>,
        mySellingChats = mySellingChats as List<String>
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
//    Timber.d("toSellAdvertisement: $confirmationMessageSent")
    return SellAdvertisement(
        id = id,
        ownerId = ownerId,
        book = book!!.toBook(),
        status = status!!,
        publishDate = publishDate!!,
        location = location,
        price = price,
        confirmationMessageSent = confirmationMessageSent!!
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
        confirmationMessageSent = confirmationMessageSent!!

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
        confirmationMessageSent = confirmationMessageSent

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
        confirmationMessageSent = confirmationMessageSent

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
        bidders = bidders!!.map { it.toBidder() },
        confirmationMessageSent = confirmationMessageSent!!

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
        },
        confirmationMessageSent = confirmationMessageSent!!

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
        },
        confirmationMessageSent = confirmationMessageSent

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
        price = price,
        confirmationMessageSent = confirmationMessageSent
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

fun DistrictDto.toDistricts(): District {
    return District(
        id = id,
        name = name,
        governorateId = governorateId
    )
}

fun MessageDto.toMessage(): Message {
    val image = if (imageUrl == null) imageUrl else Uri.parse(imageUrl)
    return Message(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        imageUrl = image,
        date = date
    )
}

fun Message.toMessageDto(): MessageDto {
    val image = if (imageUrl == null) null else imageUrl.toString()
    return MessageDto(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        imageUrl = image,
        chatUsers = arrayListOf( // remove this attribute (no need for it) but we will need to delete all messages on firestore
            senderId,
            receiverId
        )
    )
}

fun MyChatDto.toMyChat(): MyChat {
    return MyChat(
        senderId = senderId,
        userIChatWith = userIChatWith!!.toUser(),
        lastMessage = lastMessage,
        lastMessageDate = lastMessageDate
    )
}

fun RequestDto.toMyRequest(user: UserDto): MySentRequest {
    return MySentRequest(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        username = user.username,
        advertisementId = advertisementId,
        avatarImage = user.avatarImage,
        bookTitle = bookTitle,
        condition = condition,
        category = category,
        adType = adType,
        edition = edition,
        date = date,
        status = status
    )
}

fun RequestDto.toMyReceivedRequest(user: UserDto): MyReceivedRequest {
    return MyReceivedRequest(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        username = user.username,
        advertisementId = advertisementId,
        avatarImage = user.avatarImage,
        bookTitle = bookTitle,
        condition = condition,
        category = category,
        adType = adType,
        edition = edition,
        date = date
    )
}

fun MySentRequest.toRequestDto(): RequestDto {
    return RequestDto(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        advertisementId = advertisementId,
        username = username,
        avatarImage = avatarImage,
        bookTitle = bookTitle,
        condition = condition,
        category = category,
        adType = adType,
        edition = edition,
        status = status
    )
}