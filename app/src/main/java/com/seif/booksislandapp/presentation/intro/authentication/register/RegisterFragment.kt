package com.seif.booksislandapp.presentation.intro.authentication.register

import android.annotation.SuppressLint
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
import com.seif.booksislandapp.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

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
                        Intent(requireActivity(), HomeActivity::class.java).also { intent ->
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                    is RegisterState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is RegisterState.NoInternetConnection -> {
                        handleNoInternetConnectionState()
                    }
                }
            }
        }
    }

    private fun handleErrorState(message: String) {
        binding.root.showSnackBar(message)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                createAlertDialog()
                startLoadingDialog()
            }
            false -> dismissLoadingDialog()
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

    @SuppressLint("InflateParams")
    private fun createAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(layoutInflater.inflate(R.layout.custom_loading_dialog, null))
        builder.setCancelable(true)
        dialog = builder.create()
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }
}