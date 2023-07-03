package com.seif.booksislandapp

import android.net.Uri
import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.sell.SellAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.utils.Resource
import java.util.Date

val FAKE_SELL_ADS_RESPONSE = Resource.Success(
    arrayListOf(
        SellAdvertisement(
            id = "1",
            ownerId = "seif_1",
            book = Book(
                id = "",
                images = listOf(Uri.parse(""), Uri.parse("")),
                title = "Android App Development For Dummies",
                author = "Barry Burd",
                category = "Technology",
                condition = "New",
                description = "Greate app to learn android app development",
                edition = "2"
            ),
            status = AdvStatus.Opened,
            publishDate = Date(),
            location = "Cairo, Dar ElSalam",
            price = "100",
            confirmationMessageSent = false,
            confirmationRequestId = ""
        ),
        SellAdvertisement(
            id = "2",
            ownerId = "seif_2",
            book = Book(
                id = "",
                images = listOf(Uri.parse(""), Uri.parse("")),
                title = "Clean Code",
                author = "Uncle Bob",
                category = "Technology",
                condition = "New",
                description = "Greate app to learn clean code.",
                edition = "1"
            ),
            status = AdvStatus.Opened,
            publishDate = Date(),
            location = "Cairo, Dar ElSalam",
            price = "200",
            confirmationMessageSent = false,
            confirmationRequestId = ""
        )
    )
)

val FAKE_SELL_AD_1 = SellAdvertisement(
    id = "1",
    ownerId = "seif_1",
    book = Book(
        id = "",
        images = listOf(Uri.parse(""), Uri.parse("")),
        title = "Android App Development For Dummies",
        author = "Barry Burd",
        category = "Technology",
        condition = "New",
        description = "Greate app to learn android app development",
        edition = "2"
    ),
    status = AdvStatus.Opened,
    publishDate = Date(),
    location = "Cairo, Dar ElSalam",
    price = "100",
    confirmationMessageSent = false,
    confirmationRequestId = ""
)

val FAKE_SELL_AD_2 = SellAdvertisement(
    id = "1",
    ownerId = "seif_1",
    book = Book(
        id = "",
        images = listOf(Uri.parse(""), Uri.parse("")),
        title = "Android App Development For Dummies",
        author = "Barry Burd",
        category = "Technology",
        condition = "New",
        description = "Greate app to learn android app development",
        edition = "2"
    ),
    status = AdvStatus.Opened,
    publishDate = Date(),
    location = "Cairo, Dar ElSalam",
    price = "100",
    confirmationMessageSent = false,
    confirmationRequestId = ""
)

val FAKE_SELL_AD_DTO_1 = SellAdvertisementDto(
    id = "1",
    ownerId = "seif_1",
    book = BookDto(
        id = "",
        images = listOf("", ""),
        title = "Android App Development For Dummies",
        author = "Barry Burd",
        category = "Technology",
        condition = "New",
        description = "Greate app to learn android app development",
        edition = "2"
    ),
    status = AdvStatus.Opened,
    publishDate = Date(),
    location = "Cairo, Dar ElSalam",
    price = "100",
    confirmationMessageSent = false,
    confirmationRequestId = ""
)

val FAKE_SELL_AD_DTO_2 = SellAdvertisementDto(
    id = "1",
    ownerId = "seif_1",
    book = BookDto(
        id = "",
        images = listOf("", ""),
        title = "Android App Development For Dummies",
        author = "Barry Burd",
        category = "Technology",
        condition = "New",
        description = "Greate app to learn android app development",
        edition = "2"
    ),
    status = AdvStatus.Opened,
    publishDate = Date(),
    location = "Cairo, Dar ElSalam",
    price = "100",
    confirmationMessageSent = false,
    confirmationRequestId = ""
)