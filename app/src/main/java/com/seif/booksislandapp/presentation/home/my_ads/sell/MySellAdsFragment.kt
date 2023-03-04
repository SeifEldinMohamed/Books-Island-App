package com.seif.booksislandapp.presentation.home.my_ads.sell

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.seif.booksislandapp.databinding.FragmentMySellAdsBinding
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.buy.adapter.BuyAdapter
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class MySellAdsFragment : Fragment(), OnAdItemClick<SellAdvertisement> {
    private var _binding: FragmentMySellAdsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private val buyAdapter by lazy { BuyAdapter() }
    private val mySellAdsViewModel: MySellAdsViewModel by viewModels()
    private lateinit var userId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMySellAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        buyAdapter.onAdItemClick = this
        userId = mySellAdsViewModel.getFromSP(USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            mySellAdsViewModel.fetchAllSellAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchMySellAds()

        binding.rvSellMyAds.adapter = buyAdapter
    }

    private fun fetchMySellAds() {
        mySellAdsViewModel.fetchAllSellAdvertisement(userId)
        observe()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mySellAdsViewModel.mySellAdsState.collectLatest {
                when (it) {
                    MySellAdsState.Init -> Unit
                    is MySellAdsState.FetchAllMySellAdsSuccessfully -> {
                        buyAdapter.updateList(it.sellAds)
                        handleUi(it.sellAds)
                    }
                    is MySellAdsState.IsLoading -> handleLoadingState(it.isLoading)
                    is MySellAdsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is MySellAdsState.ShowError -> handleErrorState(it.errorMessage)
                }
            }
        }
    }

    private fun handleUi(sellAds: ArrayList<SellAdvertisement>) {
        if (sellAds.isEmpty()) {
            binding.rvSellMyAds.hide()
            binding.noBooksAnimationSellMy.show()
        } else {
            binding.rvSellMyAds.show()
            binding.noBooksAnimationSellMy.hide()
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
                                fetchMySellAds()
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
        super.onDestroyView()
        buyAdapter.onAdItemClick = null
        _binding = null
    }

    override fun onAdItemClick(item: SellAdvertisement, position: Int) {
    }
}