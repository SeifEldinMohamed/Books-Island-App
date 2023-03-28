package com.seif.booksislandapp.presentation.home.ad_details.sell

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
import com.seif.booksislandapp.databinding.FragmentSellAdDetailsBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.ad_details.sell.adapter.RelatedSellAdsAdapter
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class SellAdDetailsFragment : Fragment(), OnAdItemClick<SellAdvertisement> {
    private var _binding: FragmentSellAdDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: SellAdDetailsFragmentArgs by navArgs()
    private val sellAdDetailsViewModel: SellAdDetailsViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val relatedSellAdsAdapter: RelatedSellAdsAdapter by lazy { RelatedSellAdsAdapter() }
    private var owner: User? = null
    private var currUser: User? = null
    private var isFavorite: Boolean? = false
    private var relatedAds: List<SellAdvertisement> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSellAdDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        relatedSellAdsAdapter.onRelatedAdItemClick = this
        fetchOwnerData()
        showAdDetails()
        observe()
        fetchRelatedSellAds()
        ownerAdLimitations()
        binding.ivHeart.setOnClickListener {
            handleIsFavorite()
        }
        binding.ivBackSellDetails.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rvRelatedAds.adapter = relatedSellAdsAdapter
    }

    private fun ownerAdLimitations() {
        if (args.buyAdvertisement.ownerId == sellAdDetailsViewModel.readFromSP(
                USER_ID_KEY,
                String::class.java
            )
        ) {
            Timber.d("ownerAdLimitations: disable ")
            binding.ivChat.disable()
            binding.ivChat.setColorFilter(binding.root.context.getColor(R.color.gray_light))
        }
    }

    private fun fetchRelatedSellAds() {
        if (relatedAds.isEmpty())
            sellAdDetailsViewModel.fetchRelatedAds(
                args.buyAdvertisement.id,
                args.buyAdvertisement.book.category
            )
    }

    private fun fetchOwnerData() {
        if (owner == null || currUser == null)
            sellAdDetailsViewModel.getUserById(
                args.buyAdvertisement.ownerId,
                sellAdDetailsViewModel.readFromSP(
                    USER_ID_KEY,
                    String::class.java
                )
            )
    }
    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            sellAdDetailsViewModel.sellDetailsState.collect {
                when (it) {
                    SellDetailsState.Init -> Unit
                    is SellDetailsState.IsLoading -> handleLoadingState(it.isLoading)
                    is SellDetailsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is SellDetailsState.ShowError -> handleErrorState(it.message)
                    is SellDetailsState.GetUserByIdSuccessfully -> {
                        owner = it.user
                        binding.ivOwnerAvatar.load(it.user.avatarImage)
                        binding.tvOwnerName.text = it.user.username
                    }

                    is SellDetailsState.FetchRelatedSellAdvertisementSuccessfully -> {
                        relatedAds = it.relatedAds
                        relatedSellAdsAdapter.updateList(it.relatedAds)
                        if (it.relatedAds.isEmpty())
                            binding.tvNoRelatedAds.show()
                        else
                            binding.tvNoRelatedAds.hide()
                    }
                    is SellDetailsState.AddedToFavorite -> {
                    }
                    is SellDetailsState.GetCurrentUserByIdSuccessfully -> {
                        currUser = it.user
                        withContext(Dispatchers.Main) {
                            if (currUser!!.wishListBuy.contains(args.buyAdvertisement.id)) {
                                isFavorite = true
                                binding.ivHeart.setImageResource(R.drawable.baseline_favorite_24)
                            }
                        }
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
        val bookCondition: String = when (buyAdvertisement.book.condition) {
            "Used" -> "Used"
            "New" -> "New"
            else -> ""
        }
        binding.tvTitle.text = buyAdvertisement.book.title
        binding.tvPrice.text = getString(R.string.egypt_pound, buyAdvertisement.price)
        binding.ivBook.load(buyAdvertisement.book.images.first())
        binding.tvLocation.text = buyAdvertisement.location
        binding.tvPublishDate.text = buyAdvertisement.publishDate.formatDateInDetails()
        binding.tvBookDescription.text = buyAdvertisement.book.description
        binding.tvAuthorName.text = buyAdvertisement.book.author
        binding.tvConditionStatus.text = bookCondition
        binding.tvCategoryStatus.text = buyAdvertisement.book.category
        binding.tvEditionValue.text = buyAdvertisement.book.edition
    }

    override fun onAdItemClick(item: SellAdvertisement, position: Int) {
        val action = SellAdDetailsFragmentDirections.actionSellAdDetailsFragmentSelf(item)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        owner = null
        currUser = null
        relatedAds = emptyList()
        dialog.setView(null)
        isFavorite = null
        _binding = null
    }
    private fun handleIsFavorite() {

        isFavorite?.let { isFav ->
            isFavorite = !isFav
            currUser?.wishListBuy?.let { wishList ->
                if ((!isFav) && !wishList.contains(args.buyAdvertisement.id)) {
                    binding.ivHeart.setImageResource(R.drawable.baseline_favorite_24)
                    wishList.add(args.buyAdvertisement.id)
                } else if (!isFav && wishList.contains(args.buyAdvertisement.id)) {
                    binding.ivHeart.setImageResource(R.drawable.baseline_favorite_border_24)
                    wishList.remove(args.buyAdvertisement.id)
                } else { }
            }
        }
        sellAdDetailsViewModel.updateUserWishList(currUser!!)
    }
}