package com.seif.booksislandapp.presentation.home.my_ads.donate

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentMyAdsDonateBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.donation.adapter.DonateAdapter
import com.seif.booksislandapp.presentation.home.my_ads.MyAdsFragmentDirections
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class MyAdsDonateFragment : Fragment(), OnAdItemClick<DonateAdvertisement> {
    private var _binding: FragmentMyAdsDonateBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val donateAdapter by lazy { DonateAdapter() }
    private val myDonateAdsViewModel: MyDonateAdsViewModel by viewModels()
    private lateinit var userId: String
    private var myDonationAds: List<DonateAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyAdsDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        donateAdapter.onAdItemClick = this
        userId = myDonateAdsViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            myDonateAdsViewModel.fetchAllDonateAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchMyDonationAds()

        binding.rvDonateMyAds.adapter = donateAdapter
    }

    private fun fetchMyDonationAds() {
        if (myDonationAds == null) {
            myDonateAdsViewModel.fetchAllDonateAdvertisement(userId)
            observe()
        }
    }

    private fun observe() {
        lifecycleScope.launchWhenCreated {
            myDonateAdsViewModel.myDonateAdsState.collect {
                when (it) {
                    MyDonateAdsState.Init -> Unit
                    is MyDonateAdsState.FetchAllMyDonateAdsSuccessfully -> {
                        myDonationAds = it.donateAds
                        donateAdapter.updateList(it.donateAds)
                        handleUi(it.donateAds)
                    }
                    is MyDonateAdsState.IsLoading -> handleLoadingState(it.isLoading)
                    is MyDonateAdsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is MyDonateAdsState.ShowError -> handleErrorState(it.errorMessage)
                }
            }
        }
    }

    override fun onResume() {
        myDonationAds?.let {
            handleUi(it.toCollection(ArrayList()))
        }
        super.onResume()
    }

    private fun handleUi(donationAds: ArrayList<DonateAdvertisement>) {
        if (_binding != null) {
            if (donationAds.isEmpty()) {
                binding.rvDonateMyAds.hide()
                binding.noBooksAnimationDonationMy.show()
            } else {
                binding.rvDonateMyAds.show()
                binding.noBooksAnimationDonationMy.hide()
            }
        }
    }

    private fun handleNoInternetConnectionState() {
        NoInternetDialogPendulum.Builder(
            requireActivity(),
            viewLifecycleOwner.lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        when (hasActiveConnection) {
                            true -> {
                                binding.root.showInfoSnackBar("Internet connection is back")
                                fetchMyDonationAds()
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
            MyAdsFragmentDirections.actionMyAdsFragmentToUploadDonateFragment(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        donateAdapter.onAdItemClick = null
        dialog.setView(null)
        binding.rvDonateMyAds.adapter = null
        _binding = null
        super.onDestroyView()
    }
}