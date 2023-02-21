package com.seif.booksislandapp.presentation.home.ad_details.auction.sheet

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musfickjamil.snackify.Snackify
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentAuctionSheetBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.USERNAME_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class AuctionSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAuctionSheetBinding? = null
    private val binding get() = _binding!!
    private val auctionSheetViewModel: AuctionSheetViewModel by activityViewModels()
    private var auctionAdvertisement: AuctionAdvertisement? = null
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAuctionSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        binding.btnBid.setOnClickListener {
            // ad bid logic
            val bidValue = binding.etBid.text.toString()

            auctionAdvertisement?.let { auctionAd ->
                val currentAuctionValue = auctionAd.bidders.let { bidders ->
                    bidders.maxByOrNull { it.suggestedPrice.toInt() }?.suggestedPrice?.toInt() ?: 0
                }
                val bidder = Bidder(
                    bidderId = auctionSheetViewModel.readFromSP(USER_ID_KEY, String::class.java),
                    bidderName = auctionSheetViewModel.readFromSP(USERNAME_KEY, String::class.java),
                    suggestedPrice = bidValue
                )
                auctionSheetViewModel.addBidder(
                    auctionAd.id,
                    bidder,
                    currentAuctionValue
                )
            }
        }
        auctionSheetViewModel.auctionAdvertisement.observe(viewLifecycleOwner) {
            it?.let { sendAuctionAdvertisement ->
                auctionAdvertisement = sendAuctionAdvertisement
                auctionSheetViewModel.fetchAuctionAdById(sendAuctionAdvertisement.id)
                observe()
            }
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            auctionSheetViewModel.auctionSheetState.collect {
                when (it) {
                    AuctionSheetState.Init -> Unit
                    is AuctionSheetState.IsLoading -> handleLoadingState(it.isLoading)
                    is AuctionSheetState.NoInternetConnection -> handleNoInternetConnectionState()
                    is AuctionSheetState.ShowError -> handleErrorState(it.message)
                    is AuctionSheetState.FetchAuctionAdByIdSuccessfully -> {
                        updateUi(it.auctionAd)
                    }
                    is AuctionSheetState.AddBidderSuccessfully -> {
                        binding.etBid.text?.clear()
                        Snackify.success(binding.root, it.message, Snackify.LENGTH_SHORT)
                            .setAnchorView(binding.root).show()
                    }
                }
            }
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
                                auctionAdvertisement?.let {
                                    auctionSheetViewModel.fetchAuctionAdById(it.id)
                                }
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

    private fun handleErrorState(message: String) {
        Snackify.error(binding.root, message, Snackify.LENGTH_SHORT).setAnchorView(binding.root)
            .show()
    }

    private fun updateUi(auctionAdvertisement: AuctionAdvertisement) {
        binding.tvCurrentPriceValue.text = getString(
            R.string.egypt_pound,
            (
                auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice }?.suggestedPrice
                    ?: auctionAdvertisement.startPrice
                ).toString()
        )
        binding.tvStatus.text =
            getString(R.string.status_value, auctionAdvertisement.auctionStatus.toString())
        binding.tvBiddersNumber.text =
            getString(R.string.bidders_number, auctionAdvertisement.bidders.distinctBy { it.bidderId }.size.toString())
        binding.tvLastBidderValue.text =
            auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice }?.bidderName ?: getString(
                R.string.no_one
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}