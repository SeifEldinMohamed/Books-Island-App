package com.seif.booksislandapp.presentation.home.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.SaveInSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdRealTime
import com.seif.booksislandapp.presentation.admin.report_details.GetUserByIdState
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val setInSharedPreferenceUseCase: SaveInSharedPreferenceUseCase,
    private val getUserByIdUseCaseRealTime: GetUserByIdRealTime,
    private val resourceProvider: ResourceProvider

) : ViewModel() {
    private var _userState = MutableStateFlow<GetUserByIdState>(GetUserByIdState.Init)
    val userState = _userState.asStateFlow()
    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }

    fun saveInSP(key: String, value: Boolean) {
        setInSharedPreferenceUseCase(key, value)
    }

    fun currentUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCaseRealTime.invoke(userId).collect {
                when (it) {
                    is Resource.Error -> showError(it.message)
                    is Resource.Success -> {
                        _userState.value =
                            GetUserByIdState.GetUserByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _userState.value = GetUserByIdState.NoInternetConnection(message)
            }
            else -> {
                _userState.value = GetUserByIdState.ShowError(message)
            }
        }
    }
}