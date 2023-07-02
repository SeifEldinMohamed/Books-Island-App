package com.seif.booksislandapp.presentation.home.my_ads.exchange

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
import com.seif.booksislandapp.databinding.FragmentMyadsExchangeBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.exchange.adapter.ExchangeAdapter
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

@AndroidEntryPoint
class MyAdsExchangeFragment : Fragment(), OnAdItemClick<ExchangeAdvertisement> {
    private var _binding: FragmentMyadsExchangeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val exchangeAdapter by lazy { ExchangeAdapter() }
    private val myExchangeAdsViewModel: MyExchangeAdsViewModel by viewModels()
    private lateinit var userId: String
    private var myExchangeAds: List<ExchangeAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyadsExchangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        exchangeAdapter.onAdItemClick = this
        userId = myExchangeAdsViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            myExchangeAdsViewModel.fetchAllExchangeAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchMyExchangeAds()

        binding.rvExchangeMyAds.adapter = exchangeAdapter
        binding.rvExchangeMyAds.itemAnimator = ScaleInTopAnimator().apply {
            addDuration = 300
        }
    }

    private fun fetchMyExchangeAds() {
        if (myExchangeAds == null) {
            myExchangeAdsViewModel.fetchAllExchangeAdvertisement(userId)
            observe()
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                myExchangeAdsViewModel.myAuctionAdsState.collect {
                    when (it) {
                        MyExchangeAdsState.Init -> Unit
                        is MyExchangeAdsState.FetchAllMyExchangeAdsSuccessfully -> {
                            myExchangeAds = it.exchangeAds
                            exchangeAdapter.updateList(it.exchangeAds)
                            handleUi(it.exchangeAds)
                        }
                        is MyExchangeAdsState.IsLoading -> handleLoadingState(it.isLoading)
                        is MyExchangeAdsState.NoInternetConnection -> handleNoInternetConnectionState()
                        is MyExchangeAdsState.ShowError -> handleErrorState(it.errorMessage)
                    }
                }
            }
        }
    }

    override fun onResume() {
        myExchangeAds?.let {
            handleUi(it.toCollection(ArrayList()))
        }
        super.onResume()
    }

    private fun handleUi(exchangeAds: ArrayList<ExchangeAdvertisement>) {
        if (_binding != null) {
            if (exchangeAds.isEmpty()) {
                binding.rvExchangeMyAds.hide()
                binding.noBooksAnimationExchangeMy.show()
                binding.tvNoAdsYet.show()
                binding.ivNoAdsYet.show()
            } else {
                binding.rvExchangeMyAds.show()
                binding.noBooksAnimationExchangeMy.hide()
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
                                fetchMyExchangeAds()
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
        exchangeAdapter.onAdItemClick = null
        dialog.setView(null)
        binding.rvExchangeMyAds.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: ExchangeAdvertisement, position: Int) {
        val action =
            MyAdsFragmentDirections.actionMyAdsFragmentToUploadExchangeFragment(null, item)
        findNavController().navigate(action)
    }
}