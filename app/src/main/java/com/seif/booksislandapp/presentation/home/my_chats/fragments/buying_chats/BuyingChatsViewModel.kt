package com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.my_chats.GetMyBuyingChatsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BuyingChatsViewModel @Inject constructor(
    private val getMyBuyingChatsUseCase: GetMyBuyingChatsUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _buyingChatsState = MutableStateFlow<MyBuyingChatsState>(MyBuyingChatsState.Init)
    val buyingChatsState get() = _buyingChatsState.asStateFlow()

    fun getMyBuyingChats(myId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getMyBuyingChatsUseCase.invoke(myId).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            showError(it.message)
                        }
                    }
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                        }
                        _buyingChatsState.value =
                            MyBuyingChatsState.FetchMyBuyingChatsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _buyingChatsState.value = MyBuyingChatsState.IsLoading(true)
            }
            false -> {
                _buyingChatsState.value = MyBuyingChatsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _buyingChatsState.value = MyBuyingChatsState.NoInternetConnection(message)
            }
            else -> {
                _buyingChatsState.value = MyBuyingChatsState.ShowError(message)
            }
        }
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}