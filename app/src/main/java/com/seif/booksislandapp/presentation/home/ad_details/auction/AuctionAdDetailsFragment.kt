package com.seif.booksislandapp.presentation.home.ad_details.auction

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
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
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.presentation.home.ad_details.auction.adapter.RelatedAuctionAdsAdapter
import com.seif.booksislandapp.presentation.home.ad_details.auction.sheet.AuctionSheetFragment
import com.seif.booksislandapp.presentation.home.ad_details.auction.sheet.AuctionSheetViewModel
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.presentation.home.categories.filter.FilterViewModel
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.disable
import com.seif.booksislandapp.utils.formatDateInDetails
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class AuctionAdDetailsFragment : Fragment(), OnAdItemClick<AuctionAdvertisement> {
    private var _binding: FragmentAuctionAdDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: AuctionAdDetailsFragmentArgs by navArgs()
    private val auctionAdDetailsViewModel: AuctionAdDetailsViewModel by viewModels()
    private val auctionSheetViewModel: AuctionSheetViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog
    private val relatedAuctionAdsAdapter: RelatedAuctionAdsAdapter by lazy { RelatedAuctionAdsAdapter() }
    private var owner: User? = null
    private var currUser: User? = null
    private var isFavorite: Boolean? = false
    private var relatedAds: List<AuctionAdvertisement>? = null
    private val filterViewModel: FilterViewModel by activityViewModels()
    private var lastFilter = FilterBy()
    private var districts: List<District>? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuctionAdDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        relatedAuctionAdsAdapter.onRelatedAdItemClick = this

        fetchOwnerData()
        showAdDetails()
        observe()
        fetchRelatedAuctionAds()
        ownerAdLimitations()
        isFavouriteAd()
        handleOnBackPressed()

        binding.ivChat.setOnClickListener {
            navigateToChatRoomFragment()
        }
        binding.ivHeart.setOnClickListener {
            handleIsFavorite()
        }
        binding.ivBackAuctionDetails.setOnClickListener {
            filterViewModel.filter(null)
            filterViewModel.lastFilter = lastFilter
            districts?.let {
                filterViewModel.lastDistricts = it
            }
            findNavController().navigateUp()
        }
        binding.btnParticipate.setOnClickListener {
            auctionSheetViewModel.sendAdvertisement(auctionAdvertisement = args.auctionAdvertisement)
            AuctionSheetFragment().show(parentFragmentManager, "AuctionSheet")
            observeUpdatedAuctionAd()
        }
        binding.clProfile.setOnClickListener {
            owner?.id?.let { ownerId ->
                currUser?.id?.let { currentUserId ->
                    val action =
                        AuctionAdDetailsFragmentDirections.actionAuctionAdDetailsFragmentToAdProviderProfile(
                            providerId = ownerId,
                            currentUserId = currentUserId
                        )
                    findNavController().navigate(action)
                }
            }
        }
        binding.rvRelatedAds.adapter = relatedAuctionAdsAdapter
    }

    private fun handleOnBackPressed() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button press event
                filterViewModel.filter(null)
                filterViewModel.lastFilter = lastFilter
                districts?.let {
                    filterViewModel.lastDistricts = it
                }
                findNavController().navigateUp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun navigateToChatRoomFragment() {
        owner?.let { owner ->
            val action =
                AuctionAdDetailsFragmentDirections.actionAuctionAdDetailsFragmentToChatRoomFragment(
                    ownerId = owner.id
                )
            findNavController().navigate(action)
        }
    }

    private fun isFavouriteAd() {
        currUser?.let {
            if (it.wishListAuction.contains(args.auctionAdvertisement.id)) {
                isFavorite = true
                binding.ivHeart.setImageResource(R.drawable.baseline_favorite_24)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeUpdatedAuctionAd() {
        auctionSheetViewModel.updatedAuctionAdvertisement.observe(viewLifecycleOwner) { updatedAuctionAdvertisement ->
            binding.tvCurrentPriceValue.text = getString(
                R.string.egypt_pound,
                (
                    updatedAuctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice.toInt() }?.suggestedPrice
                        ?: args.auctionAdvertisement.startPrice?.toInt()
                    ).toString()
            )

            val lastBidderId =
                updatedAuctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice.toInt() }?.bidderId
            binding.tvLastBidder.text =
                getString(R.string.last_bidder_value) + " " + if (lastBidderId == null) {
                getString(R.string.no_one)
            } else {
                if (currUser!!.id == lastBidderId) {
                    getString(R.string.you)
                } else {
                    getString(R.string.other)
                }
            }
        }
    }

    private fun ownerAdLimitations() {
        if (args.auctionAdvertisement.ownerId == auctionAdDetailsViewModel.readFromSP(
                Constants.USER_ID_KEY,
                String::class.java
            )
        ) {
            binding.ivChat.disable()
            binding.btnParticipate.disable()
            binding.ivChat.setColorFilter(binding.root.context.getColor(R.color.gray_light))
            binding.btnParticipate.setBackgroundColor(binding.root.context.getColor(R.color.gray_light))
        }
    }

    private fun fetchRelatedAuctionAds() {
        if (relatedAds == null) {
            auctionAdDetailsViewModel.fetchRelatedAds(
                args.auctionAdvertisement.id,
                args.auctionAdvertisement.book.category
            )
        } else {
            relatedAds?.let {
                handleShowRelatedAds(it)
            }
        }
    }

    private fun handleShowRelatedAds(relatedAds: List<AuctionAdvertisement>) {
        if (relatedAds.isEmpty())
            binding.tvNoRelatedAds.show()
        else {
            binding.tvNoRelatedAds.hide()
            relatedAuctionAdsAdapter.updateList(relatedAds)
        }
    }

    private fun fetchOwnerData() {
        if (owner == null) {
            auctionAdDetailsViewModel.getUserById(
                args.auctionAdvertisement.ownerId,
                auctionAdDetailsViewModel.readFromSP(
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
        binding.ivOwnerAvatar.load(owner.avatarImage) {
            crossfade(200)
        }
        binding.tvOwnerName.text = owner.username
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
                        showOwnerData(it.user)
                    }

                    is AuctionDetailsState.FetchRelatedAuctionAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedAuctionAdsAdapter.updateList(it.relatedAds)
                        if (it.relatedAds.isEmpty())
                            binding.tvNoRelatedAds.show()
                        else
                            binding.tvNoRelatedAds.hide()
                    }

                    is AuctionDetailsState.AddedToFavorite -> {
                    }

                    is AuctionDetailsState.GetCurrentUserByIdSuccessfully -> {
                        currUser = it.user
                        showLastBidderFromArgs()
                        isFavouriteAd()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLastBidderFromArgs() {
        // if last bidder is current user then show "You" else show "Other"
        val lastBidderId =
            args.auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice.toInt() }?.bidderId
        binding.tvLastBidder.text =
            getString(R.string.last_bidder_value) + " " + if (lastBidderId == null) {
            getString(R.string.no_one)
        } else {
            if (currUser!!.id == lastBidderId) {
                getString(R.string.you)
            } else {
                getString(R.string.other)
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
        val bookCondition: String = when (auctionAdvertisement.book.condition) {
            "Used With Good Condition" -> "Used With Good Condition"
            "Used With Bad Condition" -> "Used With Bad Condition"
            "New" -> "New"
            else -> ""
        }
        binding.tvTitle.text = auctionAdvertisement.book.title
        binding.tvCurrentPriceValue.text = getString(
            R.string.egypt_pound,
            (
                args.auctionAdvertisement.bidders.maxByOrNull { it.suggestedPrice.toInt() }?.suggestedPrice
                    ?: args.auctionAdvertisement.startPrice?.toInt()
                ).toString()
        )
        binding.tvStartPriceValue.text =
            getString(R.string.egypt_pound, auctionAdvertisement.startPrice?.toInt().toString())
        binding.ivBook.load(auctionAdvertisement.book.images.first()) {
            crossfade(200)
        }
        binding.tvLocation.text = auctionAdvertisement.location
        binding.tvPublishDate.text = auctionAdvertisement.publishDate.formatDateInDetails()
        binding.tvBookDescription.text = auctionAdvertisement.book.description
        binding.tvAuthorName.text = auctionAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvCategoryStatus.text = auctionAdvertisement.book.category
        binding.tvEditionValue.text = auctionAdvertisement.book.edition
    }

    override fun onAdItemClick(item: AuctionAdvertisement, position: Int) {
        val action = AuctionAdDetailsFragmentDirections.actionAuctionAdDetailsFragmentSelf(item)
        findNavController().navigate(action)
    }

    private fun handleIsFavorite() {

        isFavorite?.let { isFav ->
            isFavorite = !isFav
            currUser?.wishListAuction?.let { wishList ->
                if ((!isFav) && !wishList.contains(args.auctionAdvertisement.id)) {
                    binding.ivHeart.setImageResource(R.drawable.baseline_favorite_24)
                    wishList.add(args.auctionAdvertisement.id)
                } else if (!isFav && wishList.contains(args.auctionAdvertisement.id)) {
                    binding.ivHeart.setImageResource(R.drawable.baseline_favorite_border_24)
                    wishList.remove(args.auctionAdvertisement.id)
                } else {
                }
            }
        }
        currUser?.let {
            auctionAdDetailsViewModel.updateUserWishList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.isEnabled = false // Disable the callback
        onBackPressedCallback.remove() // Unregister the callback
        binding.rvRelatedAds.adapter = null
        dialog.setView(null)
        _binding = null
    }
}