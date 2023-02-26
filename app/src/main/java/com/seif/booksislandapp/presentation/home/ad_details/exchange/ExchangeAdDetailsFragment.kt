package com.seif.booksislandapp.presentation.home.ad_details.exchange

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentExchangeAdDetailsBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.ad_details.exchange.adapter.RelatedExchangeAdsAdapter
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.exchange.adapter.BooksToExchangeAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class ExchangeAdDetailsFragment : Fragment(), OnAdItemClick<ExchangeAdvertisement> {
    lateinit var binding: FragmentExchangeAdDetailsBinding
    private val args: ExchangeAdDetailsFragmentArgs by navArgs()
    private val exchangeAdDetailsViewModel: ExchangeDetailsViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val relatedExchangeAdsAdapter: RelatedExchangeAdsAdapter by lazy { RelatedExchangeAdsAdapter() }
    private val booksToExchangeAdapter: BooksToExchangeAdapter by lazy { BooksToExchangeAdapter() }
    private var owner: User? = null
    private var relatedAds: List<ExchangeAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_exchange_ad_details, container, false)
        binding = FragmentExchangeAdDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        relatedExchangeAdsAdapter.onRelatedAdItemClick = this

        fetchOwnerData()
        showAdDetails()
        observe()
        fetchRelatedExchangeAds()
        ownerAdLimitations()

        binding.ivBackExchangeDetails.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvExchangeFor.adapter = booksToExchangeAdapter
        binding.rvRelatedAds.adapter = relatedExchangeAdsAdapter
    }

    private fun ownerAdLimitations() {
        if (args.exchangeAdv.ownerId == exchangeAdDetailsViewModel.readFromSP(
                Constants.USER_ID_KEY,
                String::class.java
            )
        ) {
            binding.ivChat.disable()
            binding.ivChat.setColorFilter(binding.root.context.getColor(R.color.gray_light))
        }
    }

    private fun fetchRelatedExchangeAds() {
        if (relatedAds.isEmpty())
            exchangeAdDetailsViewModel.fetchRelatedAds(
                args.exchangeAdv.id,
                args.exchangeAdv.book.category
            )
    }

    private fun fetchOwnerData() {
        if (owner == null)
            exchangeAdDetailsViewModel.getUserById(args.exchangeAdv.ownerId)
    }
    private fun observe() {
        lifecycleScope.launch {
            exchangeAdDetailsViewModel.exchangeDetailsState.collect {
                when (it) {
                    ExchangeDetailsState.Init -> Unit
                    is ExchangeDetailsState.IsLoading -> handleLoadingState(it.isLoading)
                    is ExchangeDetailsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is ExchangeDetailsState.ShowError -> handleErrorState(it.message)
                    is ExchangeDetailsState.GetUserByIdSuccessfully -> {
                        owner = it.user
                        binding.ivOwnerAvatar.load(it.user.avatarImage)
                        binding.tvOwnerName.text = it.user.username
                    }
                    is ExchangeDetailsState.FetchRelatedExchangeAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedExchangeAdsAdapter.updateList(it.relatedAds)
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
                                fetchRelatedExchangeAds()
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
        val exchangeAdvertisement = args.exchangeAdv
        val bookCondition: String = when (exchangeAdvertisement.book.isUsed) {
            true -> "Used"
            false -> "New"
            null -> ""
        }
        binding.tvTitle.text = exchangeAdvertisement.book.title
        binding.ivBook.load(exchangeAdvertisement.book.images.first())
        binding.tvLocation.text = exchangeAdvertisement.location
        binding.tvPublishDate.text = exchangeAdvertisement.publishDate.formatDateInDetails()
        binding.tvBookDescription.text = exchangeAdvertisement.book.description
        binding.tvAuthorName.text = exchangeAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvCategoryStatus.text = exchangeAdvertisement.book.category
        booksToExchangeAdapter.updateList(exchangeAdvertisement.booksToExchange)
    }

    override fun onAdItemClick(item: ExchangeAdvertisement, position: Int) {
        val action = ExchangeAdDetailsFragmentDirections.actionExchangeAdDetailsFragmentSelf(item)
        findNavController().navigate(action)
    }
}