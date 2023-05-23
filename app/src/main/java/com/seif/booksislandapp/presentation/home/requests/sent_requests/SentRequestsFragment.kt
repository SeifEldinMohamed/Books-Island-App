package com.seif.booksislandapp.presentation.home.requests.sent_requests

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.seif.booksislandapp.databinding.FragmentSentRequestsBinding
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class SentRequestsFragment : Fragment(), OnAdItemClick<MySentRequest> {
    private var _binding: FragmentSentRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private val sentRequestAdapter: SentRequestAdapter by lazy { SentRequestAdapter() }
    private val sentRequestsViewModel: SentRequestsViewModel by viewModels()
    private var sentRequests: List<MySentRequest>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        sentRequestAdapter.onCancelButtonItemClick = this

        if (sentRequests == null) {
            getMySentRequests()
        }
        binding.swipeRefresh.setOnRefreshListener {
            getMySentRequests()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvSetRequests.adapter = sentRequestAdapter
    }

    private fun getMySentRequests() {
        val currentUserId = sentRequestsViewModel.getCurrentUserId()
        sentRequestsViewModel.getMySentRequests(currentUserId)
        observe()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            sentRequestsViewModel.sentRequestsState.collect {
                when (it) {
                    SentRequestsState.Init -> Unit
                    is SentRequestsState.FetchSentRequestsSuccessfully -> {
                        sentRequests = it.sentRequests
                        sentRequestAdapter.updateList(it.sentRequests)
                        handleUi(it.sentRequests)
                    }
                    is SentRequestsState.CancelSentRequestsSuccessfully -> {
                        binding.root.showSuccessSnackBar(it.message)
                    }
                    is SentRequestsState.IsLoading -> handleLoadingState(it.isLoading)
                    is SentRequestsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is SentRequestsState.ShowError -> handleErrorState(it.message)
                }
            }
        }
    }

    private fun handleUi(sentRequests: List<MySentRequest>) {
        if (sentRequests.isEmpty()) {
            binding.tvNoRequestsYet.show()
            binding.ivNoRequestsYet.show()
            binding.noBooksAnimation.show()
        } else {
            binding.tvNoRequestsYet.hide()
            binding.ivNoRequestsYet.hide()
            binding.noBooksAnimation.hide()
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
                                if (sentRequests == null) {
                                    binding.root.showInfoSnackBar("Internet connection is back")
                                    getMySentRequests()
                                }
                            }
                            false -> Unit
                        }
                    }
                }

                cancelable = true // Optional
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
        Timber.d("handleLoadingState: loading $isLoading")
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
        dialog.setView(null)
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: MySentRequest, position: Int) {
        Timber.d("onAdItemClick: $position) $item ")
        sentRequestsViewModel.cancelRequest(item.id, item.adType, item.advertisementId)
    }
}