package com.seif.booksislandapp.presentation.home.wish_list.fragments.buy

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
import com.seif.booksislandapp.databinding.WishlistSellBinding
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.buy.adapter.BuyAdapter
import com.seif.booksislandapp.presentation.home.wish_list.WishListFragmentDirections
import com.seif.booksislandapp.utils.Constants
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
class BuyWishListFragment : Fragment(), OnAdItemClick<SellAdvertisement> {
    private var _binding: WishlistSellBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val buyAdapter by lazy { BuyAdapter() }
    private val buyWishListViewModel: BuyWishListViewModel by viewModels()
    private lateinit var userId: String
    // private var myBuyWishAds: List<SellAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WishlistSellBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        buyAdapter.onAdItemClick = this
        userId = buyWishListViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            buyWishListViewModel.fetchAllBuyWishListAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchBuyWishList()

        binding.rvBuyList.adapter = buyAdapter
        binding.rvBuyList.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun fetchBuyWishList() {
        // if (myBuyWishAds == null) {
        buyWishListViewModel.fetchAllBuyWishListAdvertisement(userId)
        observe()
        //  }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                buyWishListViewModel.buyWishListState.collect {
                    when (it) {
                        BuyWishListState.Init -> Unit
                        is BuyWishListState.FetchAllWishBuyItemsSuccessfully -> {
                            //      myBuyWishAds = it.sellAds
                            buyAdapter.updateList(it.sellAds)
                            handleUi(it.sellAds)
                        }
                        is BuyWishListState.IsLoading -> handleLoadingState(it.isLoading)
                        is BuyWishListState.NoInternetConnection -> handleNoInternetConnectionState()
                        is BuyWishListState.ShowError -> handleErrorState(it.message)
                    }
                }
            }
        }
    }

    private fun handleUi(sellAds: ArrayList<SellAdvertisement>) {
        if (sellAds.isEmpty()) {
            binding.rvBuyList.hide()
            binding.noBooksAnimationSellMy.show()
            binding.tvNoAdsYet.show()
            binding.ivNoAdsYet.show()
        } else {
            binding.rvBuyList.show()
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
                                fetchBuyWishList()
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
        buyAdapter.onAdItemClick = null
        dialog.dismiss()
        dialog.setView(null)
        binding.rvBuyList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: SellAdvertisement, position: Int) {
        val action = WishListFragmentDirections.actionWishListFragmentToSellAdDetailsFragment(item)
        findNavController().navigate(action)
    }
}