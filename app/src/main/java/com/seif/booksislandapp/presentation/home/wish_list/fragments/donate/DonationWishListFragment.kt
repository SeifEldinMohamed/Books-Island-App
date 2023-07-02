package com.seif.booksislandapp.presentation.home.wish_list.fragments.donate

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
import com.seif.booksislandapp.databinding.WishlistDonateBinding
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.donation.adapter.DonateAdapter
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
class DonationWishListFragment : Fragment(), OnAdItemClick<DonateAdvertisement> {
    private var _binding: WishlistDonateBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val donateAdapter by lazy { DonateAdapter() }
    private val donateWishListViewModel: DonateWishListViewModel by viewModels()
    private lateinit var userId: String
    //   private var myDonationWishAds: List<DonateAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WishlistDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        donateAdapter.onAdItemClick = this
        userId = donateWishListViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            donateWishListViewModel.fetchAllDonateWishListAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchDonateWishList()

        binding.rvDonateWishList.adapter = donateAdapter
        binding.rvDonateWishList.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun fetchDonateWishList() {
        //   if (myDonationWishAds == null) {
        donateWishListViewModel.fetchAllDonateWishListAdvertisement(userId)
        observe()
        //  }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                donateWishListViewModel.donateWishListState.collect {
                    when (it) {
                        DonateWishListState.Init -> Unit
                        is DonateWishListState.FetchAllWishDonateItemsSuccessfully -> {
                            // myDonationWishAds = it.donateAds
                            donateAdapter.updateList(it.donateAds)
                            handleUi(it.donateAds)
                        }
                        is DonateWishListState.IsLoading -> handleLoadingState(it.isLoading)
                        is DonateWishListState.NoInternetConnection -> handleNoInternetConnectionState()
                        is DonateWishListState.ShowError -> handleErrorState(it.message)
                    }
                }
            }
        }
    }

    private fun handleUi(donateAds: ArrayList<DonateAdvertisement>) {
        if (donateAds.isEmpty()) {
            binding.rvDonateWishList.hide()
            binding.noBooksAnimationSellMy.show()
            binding.tvNoAdsYet.show()
            binding.ivNoAdsYet.show()
        } else {
            binding.rvDonateWishList.show()
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
                                fetchDonateWishList()
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
        donateAdapter.onAdItemClick = null
        dialog.dismiss()
        dialog.setView(null)
        binding.rvDonateWishList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: DonateAdvertisement, position: Int) {
        val action =
            WishListFragmentDirections.actionWishListFragmentToDonateAdDetailsFragment(item)
        findNavController().navigate(action)
    }
}