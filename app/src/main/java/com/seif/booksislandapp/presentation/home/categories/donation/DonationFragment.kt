package com.seif.booksislandapp.presentation.home.categories.donation

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
import com.seif.booksislandapp.databinding.FragmentDonationBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.donation.adapter.DonateAdapter
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                lifecycleScope.launch() {
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
        lifecycleScope.launch {
            donateViewModel.donateState.collect {
                when (it) {
                    DonateState.Init -> Unit
                    is DonateState.FetchAllDonateAdvertisementSuccessfully -> {
                        donateAdapter.updateList(it.donateAds)
                    }
                    is DonateState.SearchDonateAdvertisementSuccessfully -> {
                        donateAdapter.updateList(it.searchedDonateAds)
                    }
                    is DonateState.IsLoading -> handleLoadingState(it.isLoading)
                    is DonateState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is DonateState.ShowError -> handleErrorState(it.message)
                }
            }
        }
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
        val action = DonationFragmentDirections.actionDonationFragmentToDonateAdDetailsFragment(item)
        findNavController().navigate(action)
    }
    override fun onDestroyView() {
        donateViewModel.isSearching = false
        _binding = null
        super.onDestroyView()
    }
}