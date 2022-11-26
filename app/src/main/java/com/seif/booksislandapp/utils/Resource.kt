package com.seif.booksislandapp.utils

sealed class Resource<out T : Any, out U : Any> {
    data class Success<T : Any>(val data: T) : Resource<T, Nothing>()
    data class Error<U : Any>(val message: U) : Resource<Nothing, U>()
}
