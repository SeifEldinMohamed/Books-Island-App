package com.seif.booksislandapp.presentation.home.ad_details.donate

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentDonateAdDetailsBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.ad_details.donate.adapter.RelatedDonateAdsAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class DonateAdDetailsFragment : Fragment() {
    lateinit var binding: FragmentDonateAdDetailsBinding
    private val donateAdDetailsViewModel: DonateAdDetailsViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val args: DonateAdDetailsFragmentArgs by navArgs()
    private val relatedDonateAdsAdapter: RelatedDonateAdsAdapter by lazy { RelatedDonateAdsAdapter() }
    private var owner: User? = null
    private var relatedAds: List<DonateAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.fragment_donate_ad_details, container, false)
        binding = FragmentDonateAdDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        fetchOwnerData()
        showAdDetails()
        observe()
        fetchRelatedDonateAds()
        binding.ivBackSellDetails.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvRelatedAds.adapter = relatedDonateAdsAdapter
    }

    private fun fetchRelatedDonateAds() {
        if (relatedAds.isEmpty())
            donateAdDetailsViewModel.getAllRelatedAds(
                args.donateAdv.id,
                args.donateAdv.book.category
            )
    }

    private fun fetchOwnerData() {
        if (owner == null)
            donateAdDetailsViewModel.getUserByIdSuccessfully(args.donateAdv.ownerId)
    }

    private fun observe() {
        lifecycleScope.launch {
            donateAdDetailsViewModel.donateDetailsState.collect {
                when (it) {
                    DonateAdDetailsState.Int -> Unit
                    is DonateAdDetailsState.IsLoading -> handleLoadingState(it.isLoading)
                    is DonateAdDetailsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is DonateAdDetailsState.ShowError -> handleErrorState(it.message)
                    is DonateAdDetailsState.GetUserByIdSuccessfully -> {
                        owner = it.user
                        binding.ivOwnerAvatar.setImageResource(it.user.avatarImage.toInt())
                        binding.tvOwnerName.text = it.user.username
                    }
                    is DonateAdDetailsState.FetchRelatedDonateAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedDonateAdsAdapter.updateList(it.relatedAds)
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
                                fetchRelatedDonateAds()
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
        val donateAdvertisement = args.donateAdv
        val bookCondition: String = when (donateAdvertisement.book.isUsed) {
            true -> "Used"
            false -> "New"
            null -> ""
        }
        binding.tvTitle.text = donateAdvertisement.book.title
        binding.tvPrice.text = getString(R.string.free)
        binding.ivBook.load(donateAdvertisement.book.images.first())
        binding.tvLocation.text = donateAdvertisement.location
        binding.tvPublishDate.text = donateAdvertisement.publishDate.formatDateInDetails()
        binding.tvBookDescription.text = donateAdvertisement.book.description
        binding.tvAuthorName.text = donateAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvCategoryStatus.text = donateAdvertisement.book.category
    }
}