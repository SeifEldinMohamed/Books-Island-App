package com.seif.booksislandapp.data.repository

import android.net.Uri
import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.donation.DonateAdvertisementDto
import com.seif.booksislandapp.data.remote.dto.adv.exchange.BooksToExchangeDto
import com.seif.booksislandapp.data.remote.dto.adv.exchange.ExchangeAdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import java.util.*

val exchangeAdvertisementDtoList = arrayListOf(
    ExchangeAdvertisementDto(
        "test1", "test1",
        BookDto("test1", listOf("test1", "test1", "test1"), "test1"), AdvStatus.Opened,
        Date(), "test1", false, listOf(BooksToExchangeDto("test1", "test1", "test1")), "test1"
    ),
    ExchangeAdvertisementDto(
        "test2", "test2",
        BookDto("test2", listOf("test2", "test2", "test2"), "test2"), AdvStatus.Opened,
        Date(), "test2", false, listOf(BooksToExchangeDto("test2", "test2", "test2")), "test2"
    )
)
val exchangeAdvertisementList = arrayListOf(
    ExchangeAdvertisement(
        "test1", "test1",
        Book("test1", listOf(Uri.parse("test1")), "test1"), AdvStatus.Opened,
        Date(), listOf(BooksToExchange(Uri.parse("test1"), "test1", "test1")), "test1",
        false, "test1"
    ),
    ExchangeAdvertisement(
        "test2", "test2",
        Book("test2", listOf(Uri.parse("test2")), "test2"), AdvStatus.Opened,
        Date(), listOf(BooksToExchange(Uri.parse("test2"), "test2", "test2")), "test2",
        false, "test2"
    )
)

val donateAdvertisementDtoList = arrayListOf(
    DonateAdvertisementDto(
        "test1", "test1",
        BookDto("test1", listOf("test1", "test1", "test1"), "test1"), AdvStatus.Opened,
        Date(), "test1", false, "test1"
    ),
    DonateAdvertisementDto(
        "test2", "test2",
        BookDto("test2", listOf("test2", "test2", "test2"), "test2"), AdvStatus.Opened,
        Date(), "test2", false, "test2"
    )
)
val donateAdvertisementList = arrayListOf(
    DonateAdvertisement(
        "test1", "test1",
        Book("test1", listOf(Uri.parse("test1")), "test1"), AdvStatus.Opened,
        Date(), "test1",
        false, "test1"
    ),
    DonateAdvertisement(
        "test2", "test2",
        Book("test2", listOf(Uri.parse("test2")), "test2"), AdvStatus.Opened,
        Date(), "test2",
        false, "test2"
    )
)