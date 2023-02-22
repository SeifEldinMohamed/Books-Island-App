package com.seif.booksislandapp.presentation.home.categories.auction

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentAuctionBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.auction.adapter.AuctionAdapter
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

        binding.swipeRefresh.setOnRefreshListener {
            auctionViewModel.fetchAllAuctionsAdvertisements()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvAuctions.adapter = auctionAdapter
    }

    private fun listenForSearchEditTextChange() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.d("onTextChanged: $p1 - $p2 - $p3")
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
                        Timber.d("observe: fetched")
                    }
                    is AuctionState.SearchAuctionsAdsSuccessfully -> {
                        auctionAdapter.updateList(it.searchedAuctionsAds)
                    }
                    is AuctionState.IsLoading -> handleLoadingState(it.isLoading)
                    is AuctionState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is AuctionState.ShowError -> handleErrorState(it.message)
                }
            }
        }
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