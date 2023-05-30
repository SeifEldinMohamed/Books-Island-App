package com.seif.booksislandapp.presentation.home.requests.received_requests

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
import com.seif.booksislandapp.databinding.FragmentReceivedRequestsBinding
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class ReceivedRequestsFragment : Fragment(), OnReceivedRequestItemClick<MyReceivedRequest> {
    private var _binding: FragmentReceivedRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private val receivedRequestAdapter: ReceivedRequestAdapter by lazy { ReceivedRequestAdapter() }
    private val receivedRequestsViewModel: ReceivedRequestsViewModel by viewModels()
    private var receivedRequests: List<MyReceivedRequest>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReceivedRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        receivedRequestAdapter.onReceivedButtonClick = this

        if (receivedRequests == null) {
            getMyReceivedRequests()
        }
        binding.swipeRefresh.setOnRefreshListener {
            getMyReceivedRequests()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.rvReceivedRequests.adapter = receivedRequestAdapter
    }

    private fun getMyReceivedRequests() {
        val currentUserId = receivedRequestsViewModel.getCurrentUserId()
        receivedRequestsViewModel.getMyReceivedRequests(currentUserId)
        observe()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                receivedRequestsViewModel.receivedRequestsState.collect {
                    when (it) {
                        ReceivedRequestsState.Init -> Unit
                        is ReceivedRequestsState.FetchReceivedRequestsSuccessfully -> {
                            receivedRequests = it.receivedRequests
                            receivedRequestAdapter.updateList(it.receivedRequests)
                            handleUi(it.receivedRequests)
                        }
                        is ReceivedRequestsState.AcceptedConfirmationRequestSuccessfully -> {
                            binding.root.showSuccessSnackBar(it.message)
                        }
                        is ReceivedRequestsState.RejectedConfirmationRequestSuccessfully -> {
                            binding.root.showSuccessSnackBar(it.message)
                        }
                        is ReceivedRequestsState.IsLoading -> handleLoadingState(it.isLoading)
                        is ReceivedRequestsState.NoInternetConnection -> handleNoInternetConnectionState()
                        is ReceivedRequestsState.ShowError -> handleErrorState(it.message)
                    }
                }
            }
        }
    }

    private fun handleUi(receivedRequests: List<MyReceivedRequest>) {
        if (receivedRequests.isEmpty()) {
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
                                binding.root.showInfoSnackBar("Internet connection is back")
                                if (receivedRequests == null) {
                                    getMyReceivedRequests()
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
        _binding = null
        super.onDestroyView()
    }

    override fun onAcceptButtonClick(item: MyReceivedRequest, position: Int) {
        Timber.d("onAcceptButtonClick: $position - $item")
        receivedRequestsViewModel.acceptConfirmationRequest(
            item.id,
            item.senderId,
            item.adType,
            "Accepted",
            item.advertisementId
        )
    }

    override fun onRejectButtonClick(item: MyReceivedRequest, position: Int) {
        Timber.d("onRejectButtonClick: $position - $item")
        receivedRequestsViewModel.rejectConfirmationRequest(
            item.id,
            item.advertisementId,
            item.adType,
            "Rejected"
        )
    }
}