package com.seif.booksislandapp.presentation.intro.authentication.forget_password

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentForgetPasswordBinding
import com.seif.booksislandapp.presentation.intro.authentication.forget_password.viewmodel.ForgetPasswordState
import com.seif.booksislandapp.presentation.intro.authentication.forget_password.viewmodel.ForgetPasswordViewModel
import com.seif.booksislandapp.utils.createAlertDialog
import com.seif.booksislandapp.utils.showSnackBar
import com.seif.booksislandapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
@AndroidEntryPoint
class ForgetPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgetPasswordBinding
    private lateinit var dialog: AlertDialog
    private val forgetPasswordViewModel: ForgetPasswordViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createAlertDialog(requireActivity())
        observe()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnSendmesssage.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        forgetPasswordViewModel.resetPassword(binding.etEmail.text.toString())
    }

    private fun observe() {
        lifecycleScope.launch {
            forgetPasswordViewModel.forgetPasswordState.collect {
                when (it) {
                    ForgetPasswordState.Init -> Unit
                    is ForgetPasswordState.IsLoading -> handleLoadingState(it.isLoading)
                    is ForgetPasswordState.ResetSuccessfully -> {
                        toast(it.message)
                        findNavController().navigateUp()
                    }
                    is ForgetPasswordState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is ForgetPasswordState.NoInternetConnection -> {
                        handleNoInternetConnectionState()
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
                        // ...
                        when (hasActiveConnection) {
                            true -> binding.root.showSnackBar("Internet connection is back")
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
    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }
    private fun handleErrorState(message: String) {
        binding.root.showSnackBar(message)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                startLoadingDialog()
            }
            false -> dismissLoadingDialog()
        }
    }
}