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
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
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
                        handleNoInternetConnectionState(binding.root)
                    }
                }
            }
        }
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
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
}