package com.seif.booksislandapp.presentation.home.categories.buy

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentBuyBinding
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.buy.adapter.BuyAdapter
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.presentation.home.categories.filter.FilterViewModel
import com.seif.booksislandapp.presentation.home.categories.sort.BuyBottomSheetFragment
import com.seif.booksislandapp.presentation.home.categories.sort.SortViewModel
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class BuyFragment : Fragment(), OnAdItemClick<SellAdvertisement> {
    private var _binding: FragmentBuyBinding? = null
    private val binding get() = _binding!!
    private val buyViewModel: BuyViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val filterViewModel: FilterViewModel by activityViewModels()
    private val sortViewModel: SortViewModel by activityViewModels()
    private val buyAdapter by lazy { BuyAdapter() }
    private var sellAdvertisements: List<SellAdvertisement> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyBinding.inflate(inflater, container, false)
        buyViewModel.resetState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        buyAdapter.onAdItemClick = this
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

        firstTimeFetch()
        // observeOnRecommendation()
        listenForSearchEditTextClick()
        listenForSearchEditTextChange()

        binding.ivBack.setOnClickListener {
            sortViewModel.setLastSort("")
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            buyViewModel.fetchAllSellAdvertisement()
            observe()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.tvSortBy.setOnClickListener {

            if (!buyViewModel.getFromSP(
                    Constants.IS_SUSPENDED_KEY,
                    Boolean::class.java
                )
            ) {
                val bottomSheet = BuyBottomSheetFragment()
                bottomSheet.show(parentFragmentManager, "")
            } else {
                handleErrorState("Sorry but your account is suspended")
            }
        }

        binding.btnFilter.setOnClickListener {
            if (!buyViewModel.getFromSP(
                    Constants.IS_SUSPENDED_KEY,
                    Boolean::class.java
                )
            ) {
                findNavController().navigate(R.id.action_buyFragment_to_filterFragment)
            } else {
                handleErrorState("Sorry but your account is suspended")
            }
        }

        binding.rvBuy.adapter = buyAdapter
        binding.rvBuy.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun handleSort(sortBy: String) {
        when (sortBy) {
            "Added Recently" -> {
                buyAdapter.updateList(
                    sellAdvertisements
                )
            }
            "Lowest Price" -> {
                buyAdapter.updateList(
                    sellAdvertisements.sortedBy { it.price.toInt() }
                )
            }
            "Highest Price" -> {
                buyAdapter.updateList(
                    sellAdvertisements.sortedByDescending {
                        it.price.toInt()
                    }
                )
            }
        }
    }

    private fun fetchByFilter(filterBy: FilterBy) {
        buyViewModel.fetchSellAdvertisementByFilter(
            filterBy
        )
        observe()
    }

    //    override fun onResume() {
//        super.onResume()
//        listenForSearchEditTextClick()
//        listenForSearchEditTextChange()
//    }
  /*  private fun observeOnRecommendation() {
        viewLifecycleOwner.lifecycleScope.launch {
            recommendationViewModel.recommendationState.collect {
                when (it) {
                    RecommendationState.Init -> Unit
                    is RecommendationState.RecommendedSuccessfully -> {
                        // recommendationViewModel.recommend(sellAdvertisements as ArrayList,it.recommendation.topCategory)
                        //  Timber.d(recommendedForYou[0].toString())
                        // buyAdapter.updateList(recommendedForYou)
                    }
                    is RecommendationState.ShowError -> {
                        buyAdapter.updateList(sellAdvertisements)
                        handleUi(sellAdvertisements as ArrayList)
                    }
                }
            }
        }
    }*/

    private fun listenForSearchEditTextChange() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.d("onTextChanged: searchQuery $text")
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    text?.let {
                        Timber.d("onTextChanged: isSearching:  ${buyViewModel.isSearching}")
                        if (buyViewModel.isSearching) {
                            if (it.toString().isEmpty()) {
                                Timber.d("onTextChanged: empty search query")
                                buyAdapter.updateList(sellAdvertisements)
                            } else {
                                Timber.d("onTextChanged: searching")
                                buyViewModel.searchSellAdvertisements(
                                    searchQuery = it.toString()
                                )
                                observe() // handle issue of not working search when navigate back from details ads
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun firstTimeFetch() {
        if (buyViewModel.firstTime) {
            Timber.d("onViewCreated: fetch....")
            buyViewModel.fetchAllSellAdvertisement()
            observe()
            buyViewModel.firstTime = false
        }
    }

    private fun listenForSearchEditTextClick() {
        binding.etSearch.setOnClickListener {
            buyViewModel.isSearching = true
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus -> // use it to detect first click of user on editText
            if (hasFocus)
                buyViewModel.isSearching = true
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                buyViewModel.buyState.collectLatest {
                    when (it) {
                        BuyState.Init -> Unit
                        is BuyState.FetchAllSellAdvertisementSuccessfully -> {
                           /*recommendationViewModel.fetchRecommendation(
                                recommendationViewModel.getFromSP(
                                    Constants.USER_ID_KEY, String::class.java
                                )
                            )*/
                            sellAdvertisements = it.sellAds
                            buyAdapter.updateList(it.sellAds)
                            handleUi(it.sellAds)
                            Timber.d("observe: fetched")
                        }
                        is BuyState.SearchSellAdvertisementSuccessfully -> {
                            // sellAdvertisements = it.searchedSellAds
                            Timber.d("observe: searched list: ${it.searchedSellAds}")
                            buyAdapter.updateList(it.searchedSellAds)
                            // handleUi(it.searchedSellAds)
                        }
                        is BuyState.IsLoading -> handleLoadingState(it.isLoading)
                        is BuyState.NoInternetConnection -> handleNoInternetConnectionState()
                        is BuyState.ShowError -> handleErrorState(it.message)
                    }
                }
            }
        }
    }

    private fun handleUi(sellAds: ArrayList<SellAdvertisement>) {
        if (sellAds.isEmpty()) {
            binding.rvBuy.hide()
            binding.noBooksAnimationBuy.show()
            binding.tvNoAdsYet.show()
            binding.ivNoAdsYet.show()
        } else {
            binding.rvBuy.show()
            binding.noBooksAnimationBuy.hide()
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
                                buyViewModel.fetchAllSellAdvertisement()
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
        if (message != getString(R.string.filter_error))
            binding.root.showErrorSnackBar(message)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        Timber.d("handleLoadingState: loading $isLoading")
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

    override fun onAdItemClick(item: SellAdvertisement, position: Int) {

        if (!buyViewModel.getFromSP(Constants.IS_SUSPENDED_KEY, Boolean::class.java)) {
            val action = BuyFragmentDirections.actionBuyFragmentToAdDetailsFragment(item)
            findNavController().navigate(action)
        } else {
            handleErrorState("Sorry but your account is suspended")
        }
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView")
        buyViewModel.isSearching = false
        buyAdapter.onAdItemClick = null
        binding.rvBuy.adapter = null
        dialog.setView(null)
        _binding = null
        filterViewModel.reset()
        super.onDestroyView()
    }
}