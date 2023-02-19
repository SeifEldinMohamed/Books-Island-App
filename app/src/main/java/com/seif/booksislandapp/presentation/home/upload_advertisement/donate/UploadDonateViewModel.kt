package com.seif.booksislandapp.presentation.home.upload_advertisement.donate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreference
import com.seif.booksislandapp.domain.usecase.usecase.upload_adv.UploadDonateAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetFirebaseCurrentUserUseCase
import com.seif.booksislandapp.presentation.home.upload_advertisement.UploadState
import com.seif.booksislandapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UploadDonateViewModel @Inject constructor(
    private val uploadDonateAdvertisementUseCase: UploadDonateAdvertisementUseCase,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val getFromSharedPreference: GetFromSharedPreference
) : ViewModel() {
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Init)
    val uploadState: StateFlow<UploadState> = _uploadState

    private fun showError(message: String) {
        _uploadState.value = UploadState.ShowError(message)
    }

    private fun setLoading(isLoading: Boolean) {
        when (isLoading) {
            true -> _uploadState.value = UploadState.IsLoading(true)
            false -> _uploadState.value = UploadState.IsLoading(false)
        }
    }

    fun uploadDonateAdvertisement(donateAdvertisement: DonateAdvertisement) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = uploadDonateAdvertisementUseCase(donateAdvertisement)) {
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        showError(result.message)
                    }
                }
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                    }
                    _uploadState.value = UploadState.UploadedSuccessfully(result.data)
                }
            }
        }
    }

    fun resetUploadStatus() {
        _uploadState.value = UploadState.Init
    }

    fun getFirebaseCurrentUser(): FirebaseUser? {
        return getFirebaseCurrentUserUseCase()
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreference(key, clazz)
    }
}