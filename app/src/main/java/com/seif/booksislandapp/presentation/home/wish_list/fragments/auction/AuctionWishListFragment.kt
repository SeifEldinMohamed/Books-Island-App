package com.seif.booksislandapp.presentation.home.wish_list.fragments.auction

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
import com.seif.booksislandapp.databinding.WishlistAuctionBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.auction.adapter.AuctionAdapter
import com.seif.booksislandapp.presentation.home.wish_list.WishListFragmentDirections
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class AuctionWishListFragment : Fragment(), OnAdItemClick<AuctionAdvertisement> {
    private var _binding: WishlistAuctionBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val auctionAdapter by lazy { AuctionAdapter() }
    private val auctionWishListViewModel: AuctionWishListViewModel by viewModels()
    private lateinit var userId: String
    //  private var myAuctionWishAds: List<AuctionAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WishlistAuctionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        auctionAdapter.onAdItemClick = this
        userId = auctionWishListViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            auctionWishListViewModel.fetchAllAuctionWishListAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchAuctionWishList()

        binding.rvAuctionWishList.adapter = auctionAdapter
    }
    private fun fetchAuctionWishList() {
        //  if (myAuctionWishAds == null) {
        auctionWishListViewModel.fetchAllAuctionWishListAdvertisement(userId)
        observe()
        // }
    }
    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                auctionWishListViewModel.auctionWishListState.collect {
                    when (it) {
                        AuctionWishListState.Init -> Unit
                        is AuctionWishListState.FetchAllWishAuctionItemsSuccessfully -> {
                            // myAuctionWishAds = it.auctionAds
                            auctionAdapter.updateList(it.auctionAds)
                            handleUi(it.auctionAds)
                        }
                        is AuctionWishListState.IsLoading -> handleLoadingState(it.isLoading)
                        is AuctionWishListState.NoInternetConnection -> handleNoInternetConnectionState()
                        is AuctionWishListState.ShowError -> handleErrorState(it.message)
                    }
                }
            }
        }
    }
    private fun handleUi(auctionAds: ArrayList<AuctionAdvertisement>) {
        if (auctionAds.isEmpty()) {
            binding.rvAuctionWishList.hide()
            binding.noBooksAnimationSellMy.show()
            binding.tvNoAdsYet.show()
            binding.ivNoAdsYet.show()
        } else {
            binding.rvAuctionWishList.show()
            binding.noBooksAnimationSellMy.hide()
            binding.tvNoAdsYet.hide()
            binding.ivNoAdsYet.hide()
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
                                fetchAuctionWishList()
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

    override fun onDestroyView() {
        auctionAdapter.onAdItemClick = null
        dialog.dismiss()
        dialog.setView(null)
        binding.rvAuctionWishList.adapter = null
        _binding = null
        super.onDestroyView()
    }
    override fun onAdItemClick(item: AuctionAdvertisement, position: Int) {
        val action = WishListFragmentDirections.actionWishListFragmentToAuctionAdDetailsFragment(item)
        findNavController().navigate(action)
    }
}