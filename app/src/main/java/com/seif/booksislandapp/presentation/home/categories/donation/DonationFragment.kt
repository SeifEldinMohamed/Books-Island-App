package com.seif.booksislandapp.presentation.home.categories.donation

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
import com.seif.booksislandapp.databinding.FragmentDonationBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.donation.adapter.DonateAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
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
    private lateinit var dialog: AlertDialog
    private val donateAdapter by lazy { DonateAdapter() }
    private var donateAdvertisements: List<DonateAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_donation, container, false)
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

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            donateViewModel.fetchAllDonateAdvertisement()
            observe()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvDonate.adapter = donateAdapter
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

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            donateViewModel.donateState.collectLatest {
                when (it) {
                    DonateState.Init -> Unit
                    is DonateState.FetchAllDonateAdvertisementSuccessfully -> {
                        donateAdvertisements = it.donateAds
                        donateAdapter.updateList(it.donateAds)
                        handleUi(it.donateAds)
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
        } else {
            binding.rvDonate.show()
            binding.noBooksAnimationDonation.hide()
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
        val action =
            DonationFragmentDirections.actionDonationFragmentToDonateAdDetailsFragment(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        donateViewModel.isSearching = false
        _binding = null
        super.onDestroyView()
    }
}