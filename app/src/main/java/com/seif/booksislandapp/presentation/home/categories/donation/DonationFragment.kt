package com.seif.booksislandapp.presentation.home.categories.donation

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentDonationBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.sort.SortBottomSheetFragment
import com.seif.booksislandapp.presentation.home.categories.donation.adapter.DonateAdapter
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.presentation.home.categories.filter.FilterViewModel
import com.seif.booksislandapp.presentation.home.categories.recommendation.RecommendationState
import com.seif.booksislandapp.presentation.home.categories.recommendation.RecommendationViewModel
import com.seif.booksislandapp.presentation.home.categories.sort.SortViewModel
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class DonationFragment : Fragment(), OnAdItemClick<DonateAdvertisement> {
    private var _binding: FragmentDonationBinding? = null
    private val binding get() = _binding!!
    private val donateViewModel: DonateViewModel by viewModels()
    private val recommendationViewModel: RecommendationViewModel by viewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog
    private val donateAdapter by lazy { DonateAdapter() }
    private var donateAdvertisements: List<DonateAdvertisement> = emptyList()
    private val sortViewModel: SortViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonationBinding.inflate(inflater, container, false)
        donateViewModel.resetState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        donateAdapter.onAdItemClick = this
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        firstTimeFetch()
        listenForSearchEditTextClick()
        listenForSearchEditTextChange()
        observeOnRecommendation()
        binding.ivBack.setOnClickListener {
            sortViewModel.setLastSort("")
            findNavController().navigateUp()
        }

        binding.btnFilter.setOnClickListener {
            if (!recommendationViewModel.getFromSP(Constants.IS_SUSPENDED_KEY, Boolean::class.java)) {
                findNavController().navigate(R.id.action_donationFragment_to_filterFragment)
            } else {
                handleErrorState("Sorry but your account is suspended")
            }
        }
        filterViewModel.liveData.observe(viewLifecycleOwner) {
            if (it != null) {
                fetchByFilter(it)
            }
        }
        sortViewModel.liveData.observe(viewLifecycleOwner) {
            if (it != null) {
                handleSort(it)
            }
        }
        binding.tvSortBy.setOnClickListener {
            if (!recommendationViewModel.getFromSP(Constants.IS_SUSPENDED_KEY, Boolean::class.java)) {
                val bottomSheet = SortBottomSheetFragment()
                bottomSheet.show(parentFragmentManager, "")
            } else {
                handleErrorState("Sorry but your account is suspended")
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            donateViewModel.fetchAllDonateAdvertisement()
            observe()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvDonate.adapter = donateAdapter
        binding.rvDonate.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun listenForSearchEditTextChange() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.d("onTextChanged: $p1 - $p2 - $p3")
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    text?.let {
                        if (donateViewModel.isSearching) {
                            if (it.toString().isEmpty()) {
                                Timber.d("onTextChanged: text changed")
                                donateAdapter.updateList(donateAdvertisements)
                            } else {
                                donateViewModel.searchDonateAdvertisements(
                                    searchQuery = it.toString()
                                )
                                observe()
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun handleSort(sortBy: String) {
        when (sortBy) {
            "Added Recently" -> {
                donateAdapter.updateList(
                    donateAdvertisements
                )
            }
        }
    }

    private fun firstTimeFetch() {
        if (donateViewModel.firstTime) {
            Timber.d("onViewCreated: fetch....")
            donateViewModel.fetchAllDonateAdvertisement()
            observe()
            donateViewModel.firstTime = false
        }
    }

    private fun listenForSearchEditTextClick() {
        binding.etSearch.setOnClickListener {
            donateViewModel.isSearching = true
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus -> // use it to detect first click of user on editText
            if (hasFocus)
                donateViewModel.isSearching = true
        }
    }

    private fun observeOnRecommendation() {
        viewLifecycleOwner.lifecycleScope.launch {
            recommendationViewModel.recommendationState.collect {
                when (it) {
                    RecommendationState.Init -> Unit
                    is RecommendationState.RecommendedSuccessfully -> {
                        Timber.d(it.recommendation.topCategory)
                        val recommendedForYou: ArrayList<DonateAdvertisement> =
                            donateAdvertisements.filter { ad -> ad.book.category == it.recommendation.topCategory } as ArrayList
                        val other: ArrayList<DonateAdvertisement> =
                            donateAdvertisements.filter { ad -> ad.book.category != it.recommendation.topCategory } as ArrayList
                        recommendedForYou.addAll(other)
                        donateAdapter.updateList(recommendedForYou)
                    }
                    is RecommendationState.ShowError -> {
                        donateAdapter.updateList(donateAdvertisements)
                        handleUi(donateAdvertisements as ArrayList)
                    }
                }
            }
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            donateViewModel.donateState.collectLatest {
                when (it) {
                    DonateState.Init -> Unit
                    is DonateState.FetchAllDonateAdvertisementSuccessfully -> {
                        recommendationViewModel.fetchRecommendation(
                            recommendationViewModel.getFromSP(
                                Constants.USER_ID_KEY, String::class.java
                            )
                        )
                        donateAdvertisements = it.donateAds
                        // donateAdapter.updateList(it.donateAds)
                        // handleUi(it.donateAds)
                    }
                    is DonateState.SearchDonateAdvertisementSuccessfully -> {
                        // donateAdvertisements = it.searchedDonateAds
                        donateAdapter.updateList(it.searchedDonateAds)
                        // handleUi(it.searchedDonateAds)
                    }
                    is DonateState.IsLoading -> handleLoadingState(it.isLoading)
                    is DonateState.NoInternetConnection -> handleNoInternetConnectionState()
                    is DonateState.ShowError -> handleErrorState(it.message)
                }
            }
        }
    }

    private fun handleUi(donateAds: ArrayList<DonateAdvertisement>) {
        if (donateAds.isEmpty()) {
            binding.rvDonate.hide()
            binding.noBooksAnimationDonation.show()
            binding.tvNoAdsYet.show()
            binding.ivNoAdsYet.show()
        } else {
            binding.rvDonate.show()
            binding.noBooksAnimationDonation.hide()
            binding.tvNoAdsYet.hide()
            binding.ivNoAdsYet.hide()
        }
    }

    private fun handleNoInternetConnectionState() {
        NoInternetDialogPendulum.Builder(
            requireActivity(),
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        when (hasActiveConnection) {
                            true -> {
                                binding.root.showInfoSnackBar("Internet connection is back")
                                donateViewModel.fetchAllDonateAdvertisement()
                            }
                            false -> Unit
                        }
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional
                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()
    }

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                startLoadingDialog()
            }
            false -> dismissLoadingDialog()
        }
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    override fun onAdItemClick(item: DonateAdvertisement, position: Int) {

        if (!recommendationViewModel.getFromSP(Constants.IS_SUSPENDED_KEY, Boolean::class.java)) {
            val action =
                DonationFragmentDirections.actionDonationFragmentToDonateAdDetailsFragment(item)
            findNavController().navigate(action)
        } else {
            handleErrorState("Sorry but your account is suspended")
        }
    }

    private fun fetchByFilter(filterBy: FilterBy) {
        donateViewModel.fetchDonateAdvertisementByFilter(
            filterBy
        )
        observe()
    }

    override fun onDestroyView() {
        donateViewModel.isSearching = false
        binding.rvDonate.adapter = null
        dialog.setView(null)
        sortViewModel.setLastSort("")
        _binding = null
        filterViewModel.reset()
        super.onDestroyView()
    }
}