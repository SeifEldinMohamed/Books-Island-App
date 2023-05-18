package com.seif.booksislandapp.presentation.home.categories.exchange

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
import com.seif.booksislandapp.databinding.FragmentExchangeBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.exchange.adapter.ExchangeAdapter
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.presentation.home.categories.filter.FilterViewModel
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class ExchangeFragment : Fragment(), OnAdItemClick<ExchangeAdvertisement> {
    private var _binding: FragmentExchangeBinding? = null
    private val binding get() = _binding!!
    private val exchangeViewModel: ExchangeViewModel by viewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
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

        binding.btnFilter.setOnClickListener {
            findNavController().navigate(R.id.action_exchangeFragment_to_filterFragment)
        }
        filterViewModel.liveData.observe(viewLifecycleOwner) {
            if (it != null) {
                fetchByFilter(it)
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            exchangeViewModel.fetchAllExchangeAds()
            observe()
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
                        // Log.d("testTwo", it.exchangeAds[0].booksToExchange.toString())
                        exchangeAdvertisements = it.exchangeAds
                        exchangeAdapter.updateList(it.exchangeAds)
                        handleUi(it.exchangeAds)
                    }
                    is ExchangeState.SearchExchangeAdsSuccessfully -> {
                        exchangeAdvertisements = it.searchExchangeAds
                        exchangeAdapter.updateList(it.searchExchangeAds)
                        handleUi(it.searchExchangeAds)
                    }
                    is ExchangeState.IsLoading -> handleLoadingState(it.isLoading)
                    is ExchangeState.NoInternetConnection -> handleNoInternetConnectionState()
                    is ExchangeState.ShowError -> handleErrorState(it.error)
                }
            }
        }
    }

    private fun handleUi(exchangeAds: ArrayList<ExchangeAdvertisement>) {
        if (exchangeAds.isEmpty()) {
            binding.rvExchange.hide()
            binding.noBooksAnimationExchange.show()
        } else {
            binding.rvExchange.show()
            binding.noBooksAnimationExchange.hide()
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
                                exchangeViewModel.fetchAllExchangeAds()
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
                viewLifecycleOwner.lifecycleScope.launch() {
                    delay(1000)
                    text?.let {
                        if (exchangeViewModel.isSearching) {
                            if (it.toString().isEmpty()) {
                                exchangeAdapter.updateList(exchangeAdvertisements)
                            } else {
                                exchangeViewModel.searchExchangeAds(
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
    private fun fetchByFilter(filterBy: FilterBy) {
        exchangeViewModel.fetchExchangeAdvertisementByFilter(
            filterBy
        )
        observe()
    }

    override fun onDestroyView() {
        exchangeViewModel.isSearching = false
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: ExchangeAdvertisement, position: Int) {
        val action = ExchangeFragmentDirections.actionExchangeFragmentToExchangeAdDetailsFragment(item)
        findNavController().navigate(action)
    }
}