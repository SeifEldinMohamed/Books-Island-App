package com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange
import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetExchangeAdsByFilterUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(
        filterBy: FilterBy
    ): Resource<ArrayList<ExchangeAdvertisement>, String> {

        return exchangeAdvertisementRepositoryImp.getExchangeAdsByFilter(filterBy)
    }
}