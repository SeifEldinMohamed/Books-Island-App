package com.seif.booksislandapp.presentation.home.categories.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.domain.usecase.usecase.recommendation.GetRecommendationForUserUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.utils.DispatcherProvider
import com.seif.booksislandapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getRecommendationForUserUseCase: GetRecommendationForUserUseCase,
    private val dispatcher: DispatcherProvider,
    private val getFromSharedPrefUseCase: GetFromSharedPreferenceUseCase
) : ViewModel() {
    private var _recommendationState =
        MutableStateFlow<RecommendationState>(RecommendationState.Init)
    val recommendationState = _recommendationState.asStateFlow()
    fun fetchRecommendation(userId: String) {
        viewModelScope.launch(dispatcher.io) {
            getRecommendationForUserUseCase.invoke(userId).let {
                when (it) {
                    is Resource.Error ->
                        _recommendationState.value =
                            RecommendationState.ShowError(it.message)

                    is Resource.Success -> {
                        if (it.data.topCategory.isNotEmpty())
                            _recommendationState.value =
                                RecommendationState.RecommendedSuccessfully(it.data)
                        else
                            _recommendationState.value =
                                RecommendationState.ShowError("no ads in wish list")
                    }
                }
            }
        }
    }
    fun getFromSP(userId: String): String {
        return getFromSharedPrefUseCase(userId, String::class.java)
    }
}