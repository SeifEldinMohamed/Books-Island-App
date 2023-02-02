package com.seif.booksislandapp.presentation.intro.authentication.login
import android.app.AlertDialog
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.utils.createAlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentLoginBinding
import com.seif.booksislandapp.presentation.home.HomeActivity
import com.seif.booksislandapp.presentation.intro.authentication.login.viewmodel.LoginState
import com.seif.booksislandapp.presentation.intro.authentication.login.viewmodel.LoginViewModel
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var dialog: AlertDialog
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createAlertDialog(requireActivity())
        observe()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }
        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }
    private fun observe() {
        lifecycleScope.launch {
            loginViewModel.loginState.collect {
                when (it) {
                    LoginState.Init -> Unit
                    is LoginState.IsLoading -> handleLoadingState(it.isLoading)
                    is LoginState.LoginSuccessfully -> {
                        Intent(requireActivity(), HomeActivity::class.java).also { intent ->
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                    is LoginState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is LoginState.NoInternetConnection -> {
                        handleNoInternetConnectionState(binding.root)
                    }
                }
            }
        }
    }
    private fun loginUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        loginViewModel.login(email, password)
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