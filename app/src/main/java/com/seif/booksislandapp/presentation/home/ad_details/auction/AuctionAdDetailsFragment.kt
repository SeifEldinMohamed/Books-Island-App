package com.seif.booksislandapp.presentation.home.ad_details.auction

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentAuctionAdDetailsBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.ad_details.auction.adapter.RelatedAuctionAdsAdapter
import com.seif.booksislandapp.presentation.home.ad_details.auction.sheet.AuctionSheetFragment
import com.seif.booksislandapp.presentation.home.ad_details.auction.sheet.AuctionSheetViewModel
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class AuctionAdDetailsFragment : Fragment() {
    lateinit var binding: FragmentAuctionAdDetailsBinding
    private val args: AuctionAdDetailsFragmentArgs by navArgs()
    private val auctionAdDetailsViewModel: AuctionAdDetailsViewModel by viewModels()
    private val auctionSheetViewModel: AuctionSheetViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog
    private val relatedAuctionAdsAdapter: RelatedAuctionAdsAdapter by lazy { RelatedAuctionAdsAdapter() }
    private var owner: User? = null
    private var relatedAds: List<AuctionAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAuctionAdDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        fetchOwnerData()
        showAdDetails()
        observe()
        fetchRelatedAuctionAds()
        binding.ivBackAuctionDetails.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnParticipate.setOnClickListener {
            auctionSheetViewModel.sendAdvertisement(auctionAdvertisement = args.auctionAdvertisement)
            AuctionSheetFragment().show(parentFragmentManager, "AuctionSheet")
        }
        binding.rvRelatedAds.adapter = relatedAuctionAdsAdapter
    }

    private fun fetchRelatedAuctionAds() {
        if (relatedAds.isEmpty())
            auctionAdDetailsViewModel.fetchRelatedAds(
                args.auctionAdvertisement.id,
                args.auctionAdvertisement.book.category
            )
    }

    private fun fetchOwnerData() {
        if (owner == null)
            auctionAdDetailsViewModel.getUserById(args.auctionAdvertisement.ownerId)
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            auctionAdDetailsViewModel.auctionDetailsState.collect {
                when (it) {
                    AuctionDetailsState.Init -> Unit
                    is AuctionDetailsState.IsLoading -> handleLoadingState(it.isLoading)
                    is AuctionDetailsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is AuctionDetailsState.ShowError -> handleErrorState(it.message)
                    is AuctionDetailsState.GetUserByIdSuccessfully -> {
                        owner = it.user
                        binding.ivOwnerAvatar.load(it.user.avatarImage)
                        binding.tvOwnerName.text = it.user.username
                    }
                    is AuctionDetailsState.FetchRelatedAuctionAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedAuctionAdsAdapter.updateList(it.relatedAds)
                        if (it.relatedAds.isEmpty())
                            binding.tvNoRelatedAds.show()
                        else
                            binding.tvNoRelatedAds.hide()
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
                                fetchOwnerData()
                                fetchRelatedAuctionAds()
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
        binding.root.showErrorSnackBar(message)
    }

    private fun showAdDetails() {
        val auctionAdvertisement = args.auctionAdvertisement
        val bookCondition: String = when (auctionAdvertisement.book.isUsed) {
            true -> "Used"
            false -> "New"
            null -> ""
        }
        binding.tvTitle.text = auctionAdvertisement.book.title
        binding.tvCurrentPriceValue.text = getString(
            R.string.egypt_pound,
            (
                args.auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice }?.suggestedPrice
                    ?: args.auctionAdvertisement.startPrice
                ).toString()
        )
        binding.tvStartPriceValue.text =
            getString(R.string.egypt_pound, auctionAdvertisement.startPrice.toString())
        binding.ivBook.load(auctionAdvertisement.book.images.first())
        binding.tvLocation.text = auctionAdvertisement.location
        binding.tvPublishDate.text = auctionAdvertisement.publishDate.formatDateInDetails()
        binding.tvBookDescription.text = auctionAdvertisement.book.description
        binding.tvAuthorName.text = auctionAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvCategoryStatus.text = auctionAdvertisement.book.category
        binding.tvEditionValue.text = auctionAdvertisement.book.edition
        // if last bidder is current user then show "You" else show "Other"
        binding.tvLastBidder.text = getString(
            R.string.last_bidder_value,
            auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice }?.bidderName ?: getString(
                R.string.no_one
            )
        )
    }
}