package com.seif.booksislandapp.presentation.home.wish_list.fragments.exchange

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
import com.seif.booksislandapp.databinding.WishlistExchangeBinding
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.categories.exchange.adapter.ExchangeAdapter
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
class ExchangeWishListFragment : Fragment(), OnAdItemClick<ExchangeAdvertisement> {
    private var _binding: WishlistExchangeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dialog: AlertDialog
    private val exchangeAdapter by lazy { ExchangeAdapter() }
    private val exchangeWishListViewModel: ExchangeWishListViewModel by viewModels()
    private lateinit var userId: String
    //   private var myExchangeWishAds: List<ExchangeAdvertisement>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WishlistExchangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        exchangeAdapter.onAdItemClick = this
        userId = exchangeWishListViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            exchangeWishListViewModel.fetchAllExchangeWishListAdvertisement(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchExchangeWishList()

        binding.rvExchangeWishList.adapter = exchangeAdapter
    }

    private fun fetchExchangeWishList() {
        //  if (myExchangeWishAds == null) {
        exchangeWishListViewModel.fetchAllExchangeWishListAdvertisement(userId)
        observe()
        //  }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                exchangeWishListViewModel.exchangeWishListState.collect {
                    when (it) {
                        ExchangeWishListState.Init -> Unit
                        is ExchangeWishListState.FetchAllWishExchangeItemsSuccessfully -> {
                            //   myExchangeWishAds = it.exchangeAds
                            exchangeAdapter.updateList(it.exchangeAds)
                            handleUi(it.exchangeAds)
                        }
                        is ExchangeWishListState.IsLoading -> handleLoadingState(it.isLoading)
                        is ExchangeWishListState.NoInternetConnection -> handleNoInternetConnectionState()
                        is ExchangeWishListState.ShowError -> handleErrorState(it.message)
                    }
                }
            }
        }
    }

    private fun handleUi(exchangeAds: ArrayList<ExchangeAdvertisement>) {
        if (exchangeAds.isEmpty()) {
            binding.rvExchangeWishList.hide()
            binding.noBooksAnimationSellMy.show()
            binding.tvNoAdsYet.show()
            binding.ivNoAdsYet.show()
        } else {
            binding.rvExchangeWishList.show()
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
                                fetchExchangeWishList()
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
        dialog.dismiss()
        dialog.setView(null)
        binding.rvExchangeWishList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: ExchangeAdvertisement, position: Int) {
        val action =
            WishListFragmentDirections.actionWishListFragmentToExchangeAdDetailsFragment(item)
        findNavController().navigate(action)
    }
}