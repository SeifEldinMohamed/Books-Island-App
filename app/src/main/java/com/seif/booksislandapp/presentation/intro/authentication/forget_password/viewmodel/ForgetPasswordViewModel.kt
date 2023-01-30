package com.seif.booksislandapp.presentation.intro.authentication.forget_password.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.seif.booksislandapp.R
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel@Inject constructor(
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private lateinit var auth: FirebaseAuth
    private var _forgetPasswordState = MutableStateFlow<ForgetPasswordState>(ForgetPasswordState.Init)
    val forgetPasswordState = _forgetPasswordState.asStateFlow()
    fun resetPassword(email: String) {
        auth = FirebaseAuth.getInstance()
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                setLoading(false)
                _forgetPasswordState.value =
                    ForgetPasswordState.ResetSuccessfully("Please Check Your Email")
            }
                .addOnFailureListener {
                    setLoading(false)
                    showError(it.message.toString())
                }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> { _forgetPasswordState.value = ForgetPasswordState.IsLoading(true) }
            false -> { _forgetPasswordState.value = ForgetPasswordState.IsLoading(false) }
        }
    }
    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet) -> {
                _forgetPasswordState.value = ForgetPasswordState.NoInternetConnection(message)
            }
            else -> {
                _forgetPasswordState.value = ForgetPasswordState.ShowError(message)
            }
        }
    }
}