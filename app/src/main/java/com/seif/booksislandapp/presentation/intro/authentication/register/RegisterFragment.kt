package com.seif.booksislandapp.presentation.intro.authentication.register

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentRegisterBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.home.HomeActivity
import com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel.RegisterState
import com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel.RegisterViewModel
import com.seif.booksislandapp.utils.Constants.Companion.IS_LOGGED_IN_KEY
import com.seif.booksislandapp.utils.createAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var dialog: AlertDialog
    private val registerViewModel: RegisterViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        // return inflater.inflate(R.layout.fragment_register, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createAlertDialog(requireActivity())
        observe()

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            registerViewModel.registerState.collect {
                when (it) {
                    RegisterState.Init -> Unit
                    is RegisterState.IsLoading -> handleLoadingState(it.isLoading)
                    is RegisterState.RegisteredSuccessfully -> {
                        registerViewModel.saveInSP(IS_LOGGED_IN_KEY, true)
                        Intent(requireActivity(), HomeActivity::class.java).also { intent ->
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                    is RegisterState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is RegisterState.NoInternetConnection -> {
                        handleNoInternetConnectionState(binding.root)
                    }
                }
            }
        }
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

    private fun registerUser() {
        val user: User = createUser()
        registerViewModel.register(user)
    }

    private fun createUser(): User {
        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val selectedId: Int = binding.rgGender.checkedRadioButtonId
        val radioButton: RadioButton = requireView().findViewById(selectedId)
        val rb = radioButton.text.toString()

        return User(
            id = "",
            avatarImage = handleAvatarImage(rb),
            username = username,
            email = email,
            password = password,
            governorate = "Cairo",
            district = "Maadi",
            gender = rb
        )
    }

    private fun handleAvatarImage(gender: String): String {
        val menAvatarList = arrayListOf(
            R.drawable.man_avatar_1,
            R.drawable.man_avatar_2,
            R.drawable.man_avatar_3,
            R.drawable.man_avatar_4,
            R.drawable.man_avatar_5,
            R.drawable.man_avatar_6,
        )
        val womenAvatarList = arrayListOf(
            R.drawable.woman_avatar_1,
            R.drawable.woman_avatar_2,
            R.drawable.woman_avatar_3,
            R.drawable.woman_avatar_4,
            R.drawable.woman_avatar_5,
            R.drawable.woman_avatar_6,
        )
        return when (gender) {
            getString(R.string.male) -> menAvatarList.random().toString()
            getString(R.string.female) -> womenAvatarList.random().toString()
            else -> Unit.toString()
        }
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }
}