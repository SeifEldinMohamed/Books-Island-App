package com.seif.booksislandapp.presentation.home.categories.auction

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentAuctionBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.auction.adapter.AuctionAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class AuctionFragment : Fragment(), OnAdItemClick<AuctionAdvertisement> {
    private var _binding: FragmentAuctionBinding? = null
    private val binding get() = _binding!!
    private val auctionViewModel: AuctionViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val auctionAdapter by lazy { AuctionAdapter() }
    private var auctionsAdvertisements: List<AuctionAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAuctionBinding.inflate(inflater, container, false)
        auctionViewModel.resetState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        auctionAdapter.onAdItemClick = this

        firstTimeFetch()
        listenForSearchEditTextClick()
        listenForSearchEditTextChange()

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnFilter.setOnClickListener {
            findNavController().navigate(R.id.action_auctionFragment_to_filterFragment)
        }

        binding.swipeRefresh.setOnRefreshListener {
            auctionViewModel.fetchAllAuctionsAdvertisements()
            observe()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvAuctions.adapter = auctionAdapter
    }

    private fun listenForSearchEditTextChange() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewLifecycleOwner.lifecycleScope.launch() {
                    delay(1000)
                    text?.let {
                        if (auctionViewModel.isSearching) {
                            if (it.toString().isEmpty()) {
                                Timber.d("onTextChanged: text changed")
                                auctionAdapter.updateList(auctionsAdvertisements)
                            } else {
                                auctionViewModel.searchAuctionsAdvertisements(
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

    private fun firstTimeFetch() {
        if (auctionViewModel.firstTime) {
            Timber.d("onViewCreated: fetch....")
            auctionViewModel.fetchAllAuctionsAdvertisements()
            observe()
            auctionViewModel.firstTime = false
        }
    }

    private fun listenForSearchEditTextClick() {
        binding.etSearch.setOnClickListener {
            auctionViewModel.isSearching = true
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus -> // use it to detect first click of user on editText
            if (hasFocus)
                auctionViewModel.isSearching = true
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            auctionViewModel.auctionState.collectLatest {
                when (it) {
                    AuctionState.Init -> Unit
                    is AuctionState.FetchAllAuctionsAdsSuccessfully -> {
                        auctionsAdvertisements = it.auctionAds
                        auctionAdapter.updateList(it.auctionAds)

                        handleUi(it.auctionAds)

                        Timber.d("observe: fetched")
                    }
                    is AuctionState.SearchAuctionsAdsSuccessfully -> {
                        // auctionsAdvertisements = it.searchedAuctionsAds
                        auctionAdapter.updateList(it.searchedAuctionsAds)

                        // handleUi(it.searchedAuctionsAds)
                    }
                    is AuctionState.IsLoading -> handleLoadingState(it.isLoading)
                    is AuctionState.NoInternetConnection -> handleNoInternetConnectionState()
                    is AuctionState.ShowError -> handleErrorState(it.message)
                }
            }
        }
    }

    private fun handleUi(auctionAds: ArrayList<AuctionAdvertisement>) {
        if (auctionAds.isEmpty()) {
            binding.rvAuctions.hide()
            binding.noBooksAnimation.show()
        } else {
            binding.rvAuctions.show()
            binding.noBooksAnimation.hide()
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
                                auctionViewModel.fetchAllAuctionsAdvertisements()
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

    override fun onAdItemClick(item: AuctionAdvertisement, position: Int) {
        val action = AuctionFragmentDirections.actionAuctionFragmentToAuctionAdDetailsFragment(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        auctionViewModel.isSearching = false
        _binding = null
        super.onDestroyView()
    }
}