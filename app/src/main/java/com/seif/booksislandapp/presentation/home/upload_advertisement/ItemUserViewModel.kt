package com.seif.booksislandapp.presentation.home.upload_advertisement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.usecase.usecase.FetchUsersIChatWithUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetFirebaseCurrentUserUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ItemUserViewModel @Inject constructor(
    private val fetchUsersIChatWithUseCase: FetchUsersIChatWithUseCase,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private val _selectedUserItem = MutableLiveData<User>()
    val selectedCategoryItem: LiveData<User> get() = _selectedUserItem

    private val _usersIChatWithList =
        MutableStateFlow<UsersBottomSheetState>(UsersBottomSheetState.Init)
    val usersIChatWithList get() = _usersIChatWithList

    fun fetchUsersIChatWith(currentUserId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchUsersIChatWithUseCase(currentUserId).let {
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
                        _usersIChatWithList.value =
                            UsersBottomSheetState.FetchUsersIChatWithSuccessfully(usersIChatWith = it.data)
                    }
                }
            }
        }
    }

    fun getCurrentUser(): FirebaseUser {
        return getFirebaseCurrentUserUseCase()!!
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _usersIChatWithList.value = UsersBottomSheetState.IsLoading(true)
            }
            false -> {
                _usersIChatWithList.value = UsersBottomSheetState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _usersIChatWithList.value = UsersBottomSheetState.NoInternetConnection(message)
            }
            else -> {
                _usersIChatWithList.value = UsersBottomSheetState.ShowError(message)
            }
        }
    }

    fun selectedUser(selectedUser: User) {
        _selectedUserItem.value = selectedUser
    }
}