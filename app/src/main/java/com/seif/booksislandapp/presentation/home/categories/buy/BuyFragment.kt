package com.seif.booksislandapp.presentation.home.categories.buy

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
import com.seif.booksislandapp.databinding.FragmentBuyBinding
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.buy.adapter.BuyAdapter
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class BuyFragment : Fragment(), OnAdItemClick<SellAdvertisement> {
    private var _binding: FragmentBuyBinding? = null
    private val binding get() = _binding!!
    private val buyViewModel: BuyViewModel by viewModels()
    private lateinit var dialog: AlertDialog
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

        firstTimeFetch()
        listenForSearchEditTextClick()
        listenForSearchEditTextChange()

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            buyViewModel.fetchAllSellAdvertisement()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvBuy.adapter = buyAdapter
    }

    private fun listenForSearchEditTextChange() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.d("onTextChanged: $p1 - $p2 - $p3")
                lifecycleScope.launch() {
                    delay(1000)
                    text?.let {
                        if (buyViewModel.isSearching) {
                            if (it.toString().isEmpty()) {
                                Timber.d("onTextChanged: text changed")
                                buyAdapter.updateList(sellAdvertisements)
                            } else {
                                buyViewModel.searchSellAdvertisements(
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            buyViewModel.buyState.collectLatest {
                when (it) {
                    BuyState.Init -> Unit
                    is BuyState.FetchAllSellAdvertisementSuccessfully -> {
                        sellAdvertisements = it.sellAds
                        buyAdapter.updateList(it.sellAds)
                        Timber.d("observe: fetched")
                    }
                    is BuyState.SearchSellAdvertisementSuccessfully -> {
                        buyAdapter.updateList(it.searchedSellAds)
                    }
                    is BuyState.IsLoading -> handleLoadingState(it.isLoading)
                    is BuyState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is BuyState.ShowError -> handleErrorState(it.message)
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

    override fun onAdItemClick(item: SellAdvertisement, position: Int) {
        val action = BuyFragmentDirections.actionBuyFragmentToAdDetailsFragment(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        buyViewModel.isSearching = false
        _binding = null
        super.onDestroyView()
    }
}