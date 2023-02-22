package com.seif.booksislandapp.presentation.home.categories.exchange

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
import com.seif.booksislandapp.databinding.FragmentExchangeBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.exchange.adapter.ExchangeAdapter
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ExchangeFragment : Fragment(), OnAdItemClick<ExchangeAdvertisement> {
    private var _binding: FragmentExchangeBinding? = null
    private val binding get() = _binding!!
    private val exchangeViewModel: ExchangeViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val exchangeAdapter by lazy { ExchangeAdapter() }
    private var exchangeAdvertisements: List<ExchangeAdvertisement> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_exchange, container, false)
        _binding = FragmentExchangeBinding.inflate(inflater, container, false)
        exchangeViewModel.resetState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstTimeFetch()
        listenForSearchEditTextClick()
        listenForSearchEditTextChange()
        exchangeAdapter.onAdItemClick = this
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            exchangeViewModel.fetchAllExchangeAds()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.rvExchange.adapter = exchangeAdapter
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            exchangeViewModel.exchangeState.collectLatest {
                when (it) {
                    ExchangeState.Init -> Unit
                    is ExchangeState.FetchAllExchangeAdsSuccessfully -> {
                        exchangeAdvertisements = it.exchangeAds
                        exchangeAdapter.updateList(it.exchangeAds)
                    }
                    is ExchangeState.SearchExchangeAdsSuccessfully -> {
                        exchangeAdapter.updateList(it.searchExchangeAds)
                    }
                    is ExchangeState.IsLoading -> handleLoadingState(it.isLoading)
                    is ExchangeState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is ExchangeState.ShowError -> handleErrorState(it.error)
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

    private fun firstTimeFetch() {
        if (exchangeViewModel.firstTime) {
            Timber.d("onViewCreated: fetch....")
            exchangeViewModel.fetchAllExchangeAds()
            observe()
            exchangeViewModel.firstTime = false
        }
    }

    private fun listenForSearchEditTextClick() {
        binding.etSearch.setOnClickListener {
            exchangeViewModel.isSearching = true
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus -> // use it to detect first click of user on editText
            if (hasFocus)
                exchangeViewModel.isSearching = true
        }
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
                        if (exchangeViewModel.isSearching) {
                            if (it.toString().isEmpty()) {
                                Timber.d("onTextChanged: text changed")
                                //   exchangeAdapter.updateList(exchangeAdvertisements)
                            } else {
                                exchangeViewModel.searchExchangeAds(
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
    override fun onDestroyView() {
        exchangeViewModel.isSearching = false
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: ExchangeAdvertisement, position: Int) {
        val action = ExchangeFragmentDirections.actionExchangeFragmentToExchangeAdDetailsFragment(item)
        toast("done")
        findNavController().navigate(action)
    }
}