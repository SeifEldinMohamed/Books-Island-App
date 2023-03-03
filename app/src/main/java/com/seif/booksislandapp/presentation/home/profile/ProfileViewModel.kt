package com.seif.booksislandapp.presentation.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.usecase.usecase.auth.GetDistrictsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.auth.GetGovernoratesUseCase
import com.seif.booksislandapp.domain.usecase.usecase.auth.LogoutUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.SaveInSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.UpdateUserProfileUseCase
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
class ProfileViewModel @Inject constructor(
    private val saveInSharedPreferenceUseCase: SaveInSharedPreferenceUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val getGovernoratesUseCase: GetGovernoratesUseCase,
    private val getDistrictsUseCase: GetDistrictsUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _profileState = MutableStateFlow<ProfileState>(ProfileState.Init)
    val profileState = _profileState.asStateFlow()

    fun updateUserProfileData(user: User) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            updateUserProfileUseCase(user).let {
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
                        _profileState.value = ProfileState.UpdateUserProfileSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun getUserById(id: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCase(id).let {
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
                        _profileState.value =
                            ProfileState.GetUserByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _profileState.value = ProfileState.IsLoading(true)
            }
            false -> {
                _profileState.value = ProfileState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _profileState.value = ProfileState.NoInternetConnection(message)
            }
            else -> {
                _profileState.value = ProfileState.ShowError(message)
            }
        }
    }

    fun getGovernorates() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getGovernoratesUseCase().let {
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
                        _profileState.value = ProfileState.GetGovernoratesSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun getDistricts(governorateId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getDistrictsUseCase(governorateId = governorateId).let {
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
                        _profileState.value = ProfileState.GetDistrictsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun <T> saveInSP(key: String, data: T) {
        saveInSharedPreferenceUseCase(key, data)
    }

    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }

    fun requestLogout() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            logoutUseCase().let {
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
                        _profileState.value = ProfileState.LogoutSuccessfully(it.data)
                    }
                }
            }
        }
    }
}