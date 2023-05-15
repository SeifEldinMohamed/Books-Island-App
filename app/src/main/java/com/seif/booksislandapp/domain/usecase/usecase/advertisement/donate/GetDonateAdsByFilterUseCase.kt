package com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate
import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetDonateAdsByFilterUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(
        filterBy: FilterBy
    ): Resource<ArrayList<DonateAdvertisement>, String> {

        return advertisementRepositoryImp.getDonateAdsByFilter(filterBy)
    }
}