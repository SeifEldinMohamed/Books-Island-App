package com.seif.booksislandapp.presentation.home.my_ads.auction

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
import com.seif.booksislandapp.databinding.FragmentMyAdsAuctionsBinding
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.auction.adapter.AuctionAdapter
import com.seif.booksislandapp.presentation.home.home.HomeViewModel
import com.seif.booksislandapp.presentation.home.my_ads.MyAdsFragmentDirections
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
import timber.log.Timber

@AndroidEntryPoint
class MyAdsAuctionsFragment : Fragment(), OnAdItemClick<AuctionAdvertisement> {
    private var _binding: FragmentMyAdsAuctionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val auctionAdapter by lazy { AuctionAdapter() }
    private val myAuctionAdsViewModel: MyAuctionAdsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var userId: String
    private var myAuctionAds: List<AuctionAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyAdsAuctionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        auctionAdapter.onAdItemClick = this
        userId = myAuctionAdsViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            myAuctionAdsViewModel.fetchAllAuctionAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchMyAuctionAds()

        binding.rvAuctionMyAds.adapter = auctionAdapter
        binding.rvAuctionMyAds.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun fetchMyAuctionAds() {
        if (myAuctionAds == null) {
            Timber.d("fetchMyAuctionAds: called")
            myAuctionAdsViewModel.fetchAllAuctionAdvertisement(userId)
            observe()
        }
    }

    private fun observe() {
        Timber.d("observe: observe")
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                myAuctionAdsViewModel.myAuctionAdsState.collect {
                    Timber.d("collect: $it")
                    when (it) {
                        MyAuctionAdsState.Init -> Unit
                        is MyAuctionAdsState.FetchAllMyAuctionAdsSuccessfully -> {
                            myAuctionAds = it.auctionAds
                            auctionAdapter.updateList(it.auctionAds)
                            handleUi(it.auctionAds)
                        }

                        is MyAuctionAdsState.IsLoading -> handleLoadingState(it.isLoading)
                        is MyAuctionAdsState.NoInternetConnection -> handleNoInternetConnectionState()
                        is MyAuctionAdsState.ShowError -> handleErrorState(it.errorMessage)
                    }
                }
            }
        }
    }

    override fun onResume() {
        myAuctionAds?.let {
            handleUi(it.toCollection(ArrayList()))
        }
        super.onResume()
    }

    private fun handleUi(auctionAds: ArrayList<AuctionAdvertisement>) {
        if (_binding != null) {
            if (auctionAds.isEmpty()) {
                binding.rvAuctionMyAds.hide()
                binding.noBooksAnimationAuctionMy.show()
                binding.tvNoAdsYet.show()
                binding.ivNoAdsYet.show()
            } else {
                binding.rvAuctionMyAds.show()
                binding.noBooksAnimationAuctionMy.hide()
                binding.tvNoAdsYet.hide()
                binding.ivNoAdsYet.hide()
            }
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
                                fetchMyAuctionAds()
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
        dialog.setView(null)
        binding.rvAuctionMyAds.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: AuctionAdvertisement, position: Int) {

        if (!homeViewModel.readFromSP(Constants.IS_SUSPENDED_KEY, Boolean::class.java)) {
            val action =
                MyAdsFragmentDirections.actionMyAdsFragmentToUploadAuctionFragment(item)
            findNavController().navigate(action)
        } else {
            handleErrorState("Sorry but your account is suspended")
        }
    }
}