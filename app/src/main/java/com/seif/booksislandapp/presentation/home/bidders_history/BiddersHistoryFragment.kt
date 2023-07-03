package com.seif.booksislandapp.presentation.home.bidders_history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentBiddersHistoryBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class BiddersHistoryFragment : Fragment(), OnAdItemClick<String> {
    private var _binding: FragmentBiddersHistoryBinding? = null
    private val binding get() = _binding!!
    private val bidderHistoryAdapter: BidderHistoryAdapter by lazy { BidderHistoryAdapter() }
    private lateinit var dialog: AlertDialog
    private val args: BiddersHistoryFragmentArgs by navArgs()
    private lateinit var auctionAd: AuctionAdvertisement
    private val biddersHistoryViewModel by viewModels<BiddersHistoryViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBiddersHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        bidderHistoryAdapter.onChatItemClick = this
        fetchAuctionAd()
        observe()

        binding.ivBackBidders.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvBiddersHistory.adapter = bidderHistoryAdapter
        binding.rvBiddersHistory.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun fetchAuctionAd() {
        biddersHistoryViewModel.fetchAuctionAdById(args.auctionAdId)
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                biddersHistoryViewModel.bidderHistoryState.collect {
                    when (it) {
                        BiddersHistoryState.Init -> Unit
                        is BiddersHistoryState.IsLoading -> handleLoadingState(it.isLoading)
                        is BiddersHistoryState.NoInternetConnection -> handleNoInternetConnectionState()
                        is BiddersHistoryState.ShowError -> handleErrorState(it.message)
                        is BiddersHistoryState.FetchAuctionAdByIdSuccessfully -> {
                            auctionAd = it.auctionAd
                            handleUi(auctionAd.bidders)
                        }
                    }
                }
            }
        }
    }

    private fun handleUi(bidders: List<Bidder>) {
        if (bidders.isEmpty()) {
            binding.noBooksAnimationSellMy.show()
            binding.ivNoAdsYet.show()
            binding.tvNoAdsYet.show()
        } else {
            bidderHistoryAdapter.updateList(newBidders = bidders.sortedByDescending { bidder -> bidder.suggestedPrice.toInt() })
            binding.noBooksAnimationSellMy.hide()
            binding.ivNoAdsYet.hide()
            binding.tvNoAdsYet.hide()
        }
        binding.tvNumOfBids.text = getString(
            R.string.num_of_bids,
            auctionAd.bidders.count().toString()
        )
        binding.tvCurrentValue.text = getString(
            R.string.current_price_history,
            auctionAd.bidders.maxByOrNull { bidder -> bidder.suggestedPrice }?.suggestedPrice
                ?: auctionAd.startPrice
        )
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
                                fetchAuctionAd()
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

    override fun onAdItemClick(receiverId: String, position: Int) {
        val action =
            BiddersHistoryFragmentDirections.actionBiddersHistoryFragmentToChatRoomFragment(
                receiverId
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvBiddersHistory.adapter = null
        dialog.setView(null)
        _binding = null
    }
}