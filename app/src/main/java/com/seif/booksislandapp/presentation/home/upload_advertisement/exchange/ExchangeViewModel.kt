package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import com.seif.booksislandapp.domain.usecase.usecase.upload_adv.UploadBookForExchangeUseCase
import com.seif.booksislandapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val uploadBookForExchangeUseCase: UploadBookForExchangeUseCase
) : ViewModel() {
    private val mutableLiveData = MutableLiveData<BooksToExchange?>()
    val liveData: LiveData<BooksToExchange?> get() = mutableLiveData

    private val _uploadState = MutableStateFlow<ExchangeSheetState>(ExchangeSheetState.Init)
    val uploadState: StateFlow<ExchangeSheetState> = _uploadState

    fun addBook(booksToExchangeItem: BooksToExchange) {
        viewModelScope.launch(Dispatchers.IO) {
            _uploadState.value = ExchangeSheetState.IsLoading(true)
            when (val result = uploadBookForExchangeUseCase.invoke(booksToExchangeItem)) {
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _uploadState.value = ExchangeSheetState.IsLoading(false)
                        _uploadState.value = ExchangeSheetState.ShowError(result.message)
                    }
                }
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _uploadState.value = ExchangeSheetState.IsLoading(false)
                        _uploadState.value = ExchangeSheetState.ValidBookToExchangeData(result.data)
                        mutableLiveData.value = result.data
                    }
                }
            }
        }
    }

    fun resetBottomSheetState() {
        _uploadState.value = ExchangeSheetState.Init
    }

    fun resetBooksToExchangeLiveData() {
        mutableLiveData.value = null
    }
}
