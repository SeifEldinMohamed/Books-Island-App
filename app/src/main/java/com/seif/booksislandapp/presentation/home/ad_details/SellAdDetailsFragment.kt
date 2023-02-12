package com.seif.booksislandapp.presentation.home.ad_details

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
import com.seif.booksislandapp.databinding.FragmentSellAdDetailsBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class SellAdDetailsFragment : Fragment() {
    lateinit var binding: FragmentSellAdDetailsBinding
    private val args: SellAdDetailsFragmentArgs by navArgs()
    private val sellAdDetailsViewModel: SellAdDetailsViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val relatedAdsAdapter: RelatedAdsAdapter by lazy { RelatedAdsAdapter() }
    private var owner: User? = null
    private var relatedAds: List<SellAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSellAdDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        fetchOwnerData()
        showAdDetails()
        observe()
        fetchRelatedSellAds()
        binding.ivBackSellDetails.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvRelatedAds.adapter = relatedAdsAdapter
    }

    private fun fetchRelatedSellAds() {
        if (relatedAds.isEmpty())
            sellAdDetailsViewModel.fetchRelatedAds(
                args.buyAdvertisement.id,
                args.buyAdvertisement.book.category
            )
    }

    private fun fetchOwnerData() {
        if (owner == null)
            sellAdDetailsViewModel.getUserById(args.buyAdvertisement.ownerId)
    }

    private fun observe() {
        lifecycleScope.launch {
            sellAdDetailsViewModel.sellDetailsState.collect {
                when (it) {
                    SellDetailsState.Init -> Unit
                    is SellDetailsState.IsLoading -> handleLoadingState(it.isLoading)
                    is SellDetailsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is SellDetailsState.ShowError -> handleErrorState(it.message)
                    is SellDetailsState.GetUserByIdSuccessfully -> {
                        owner = it.user
                        binding.ivOwnerAvatar.setImageResource(it.user.avatarImage.toInt())
                        binding.tvOwnerName.text = it.user.username
                    }
                    is SellDetailsState.FetchRelatedSellAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedAdsAdapter.updateList(it.relatedAds)
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
                                fetchRelatedSellAds()
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
        val buyAdvertisement = args.buyAdvertisement
        val bookCondition: String = when (buyAdvertisement.book.isUsed) {
            true -> "Used"
            false -> "New"
            null -> ""
        }
        binding.tvTitle.text = buyAdvertisement.book.title
        binding.tvPrice.text = getString(R.string.egypt_pound, buyAdvertisement.price)
        binding.ivBook.load(buyAdvertisement.book.images.first())
        binding.tvLocation.text = buyAdvertisement.location
        binding.tvDate.text = buyAdvertisement.publishTime.formatDateInDetails()
        binding.tvBookDescription.text = buyAdvertisement.book.description
        binding.tvAuthorName.text = buyAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvCategoryStatus.text = buyAdvertisement.book.category
    }
}