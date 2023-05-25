package com.seif.booksislandapp.presentation.home.ad_details.exchange

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.seif.booksislandapp.presentation.home.categories.filter.FilterViewModel
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class ExchangeAdDetailsFragment : Fragment(), OnAdItemClick<ExchangeAdvertisement> {
    private var _binding: FragmentExchangeAdDetailsBinding? = null
    private val binding get() = _binding!!
    private val filterViewModel: FilterViewModel by activityViewModels()

    private val args: ExchangeAdDetailsFragmentArgs by navArgs()
    private val exchangeAdDetailsViewModel: ExchangeDetailsViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val relatedExchangeAdsAdapter: RelatedExchangeAdsAdapter by lazy { RelatedExchangeAdsAdapter() }
    private val booksToExchangeAdapter: BooksToExchangeAdapter by lazy {
        BooksToExchangeAdapter()
    }
    private var owner: User? = null
    private var currUser: User? = null
    private var isFavorite: Boolean? = false
    private var relatedAds: List<ExchangeAdvertisement>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExchangeAdDetailsBinding.inflate(inflater, container, false)
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
        isFavouriteAd()

        binding.ivChat.setOnClickListener {
            navigateToChatRoomFragment()
        }
        binding.ivHeart.setOnClickListener {
            handleIsFavorite()
        }
        binding.ivBackExchangeDetails.setOnClickListener {
            filterViewModel.filter(null)
            findNavController().navigateUp()
        }
        binding.clProfile.setOnClickListener {
            owner?.id?.let {
                val action =
                    ExchangeAdDetailsFragmentDirections.actionExchangeAdDetailsFragmentToAdProviderProfile(
                        it
                    )
                findNavController().navigate(action)
            }
        }
        binding.rvExchangeFor.adapter = booksToExchangeAdapter
        binding.rvRelatedAds.adapter = relatedExchangeAdsAdapter
    }

    private fun isFavouriteAd() {
        currUser?.let {
            if (it.wishListExchange.contains(args.exchangeAdv.id)) {
                isFavorite = true
                binding.ivHeart.setImageResource(R.drawable.baseline_favorite_24)
            }
        }
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
        if (relatedAds == null) {
            exchangeAdDetailsViewModel.fetchRelatedAds(
                args.exchangeAdv.id,
                args.exchangeAdv.book.category
            )
        } else {
            relatedAds?.let {
                handleShowRelatedAds(it)
            }
        }
    }

    private fun handleShowRelatedAds(relatedAds: List<ExchangeAdvertisement>) {
        if (relatedAds.isEmpty())
            binding.tvNoRelatedAds.show()
        else {
            binding.tvNoRelatedAds.hide()
            relatedExchangeAdsAdapter.updateList(relatedAds)
        }
    }

    private fun navigateToChatRoomFragment() {
        owner?.let { owner ->
            val action =
                ExchangeAdDetailsFragmentDirections.actionExchangeAdDetailsFragmentToChatRoomFragment(
                    ownerId = owner.id
                )
            findNavController().navigate(action)
        }
    }

    private fun fetchOwnerData() {
        if (owner == null) {
            exchangeAdDetailsViewModel.getUserById(
                args.exchangeAdv.ownerId,
                exchangeAdDetailsViewModel.readFromSP(
                    Constants.USER_ID_KEY,
                    String::class.java
                )
            )
        } else {
            owner?.let {
                showOwnerData(it)
            }
        }
    }

    private fun showOwnerData(owner: User) {
        binding.ivOwnerAvatar.load(owner.avatarImage)
        binding.tvOwnerName.text = owner.username
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
                        showOwnerData(it.user)
                    }
                    is ExchangeDetailsState.FetchRelatedExchangeAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedExchangeAdsAdapter.updateList(it.relatedAds)
                        if (it.relatedAds.isEmpty())
                            binding.tvNoRelatedAds.show()
                        else
                            binding.tvNoRelatedAds.hide()
                    }
                    is ExchangeDetailsState.AddedToFavorite -> {
                    }
                    is ExchangeDetailsState.GetCurrentUserByIdSuccessfully -> {
                        currUser = it.user
                        isFavouriteAd()
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
        val bookCondition: String = when (exchangeAdvertisement.book.condition) {
            "Used" -> "Used"
            "New" -> "New"
            else -> ""
        }
        binding.tvTitle.text = exchangeAdvertisement.book.title
        binding.ivBook.load(exchangeAdvertisement.book.images.first())
        binding.tvLocation.text = exchangeAdvertisement.location
        binding.tvPublishDate.text = exchangeAdvertisement.publishDate.formatDateInDetails()
        binding.tvBookDescription.text = exchangeAdvertisement.book.description
        binding.tvAuthorName.text = exchangeAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvEditionStatus.text = exchangeAdvertisement.book.edition
        binding.tvCategoryStatus.text = exchangeAdvertisement.book.category
        booksToExchangeAdapter.updateList(exchangeAdvertisement.booksToExchange)
    }

    override fun onAdItemClick(item: ExchangeAdvertisement, position: Int) {
        val action = ExchangeAdDetailsFragmentDirections.actionExchangeAdDetailsFragmentSelf(item)
        findNavController().navigate(action)
    }

    private fun handleIsFavorite() {
        isFavorite?.let { isFav ->
            isFavorite = !isFav
            currUser?.wishListExchange?.let { wishList ->
                if ((!isFav) && !wishList.contains(args.exchangeAdv.id)) {
                    binding.ivHeart.setImageResource(R.drawable.baseline_favorite_24)
                    wishList.add(args.exchangeAdv.id)
                } else if (!isFav && wishList.contains(args.exchangeAdv.id)) {
                    binding.ivHeart.setImageResource(R.drawable.baseline_favorite_border_24)
                    wishList.remove(args.exchangeAdv.id)
                } else {
                }
            }
        }
        currUser?.let {
            exchangeAdDetailsViewModel.updateUserWishList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvRelatedAds.adapter = null
        dialog.setView(null)
        _binding = null
    }
}