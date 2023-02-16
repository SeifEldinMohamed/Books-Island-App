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
import com.seif.booksislandapp.presentation.home.categories.donation.adapter.DonateAdapter
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@AndroidEntryPoint
class DonationFragment : Fragment() {
    lateinit var binding: FragmentDonationBinding
    private val donateViewModel: DonateViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val donateAdapter by lazy { DonateAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_donation, container, false)
        binding = FragmentDonationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        observe()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                lifecycleScope.launch() {
                    delay(1000)
                    text?.let {
                        if (it.toString().isEmpty()) {
                            donateViewModel.fetchAllDonateAdvertisement()
                        } else {
                            donateViewModel.searchDonateAdvertisements(
                                searchQuery = it.toString()
                            )
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.rvBuy.adapter = donateAdapter
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
}