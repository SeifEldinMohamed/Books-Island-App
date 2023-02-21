package com.seif.booksislandapp.presentation.intro.authentication.register

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentRegisterBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.presentation.home.HomeActivity
import com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel.RegisterState
import com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel.RegisterViewModel
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.IS_LOGGED_IN_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var dialog: AlertDialog
    private val registerViewModel: RegisterViewModel by viewModels()
    private var governorates: List<Governorate>? = null
    private var districts: List<District>? = null
    private var governorateId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        observe()

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        binding.acGovernorates.setOnItemClickListener { _, _, i, _ ->
            governorates?.let {
                governorateId = it[i].id
                registerViewModel.getDistricts(it[i].id)
            }
        }
        binding.tilDistricts.disable()
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
                    is RegisterState.GetGovernoratesSuccessfully -> {
                        governorates = it.governorates
                        setUpGovernoratesDropDown(it.governorates)
                    }
                    is RegisterState.GetDistrictsSuccessfully -> {
                        districts = it.districts
                        binding.acDistricts.setText("")
                        binding.tilDistricts.enabled()
                        setUpDistrictsDropDown(it.districts)
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
                                checkFetchingDataFromServer()
                                binding.root.showInfoSnackBar("Internet connection is back")
                            }
                            false -> Unit
                        }
                    }
                }

                cancelable = true // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage = "Check your Internet connection and try again." // Optional
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

    private fun checkFetchingDataFromServer() {
        if (governorates == null)
            registerViewModel.getGovernorates()
        else if (districts == null) {
            governorateId?.let {
                registerViewModel.getDistricts(it)
            }
        }
    }

    private fun setUpDistrictsDropDown(districts: List<District>) {
        val districtsName: List<String> = districts.map { it.name }
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, districtsName)
        binding.acDistricts.setAdapter(arrayAdapter)
    }

    private fun setUpGovernoratesDropDown(governorates: List<Governorate>) {
        val governoratesName: List<String> = governorates.map { it.name }
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, governoratesName)
        binding.acGovernorates.setAdapter(arrayAdapter)
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
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val selectedId: Int = binding.rgGender.checkedRadioButtonId
        val radioButton: RadioButton = requireView().findViewById(selectedId)
        val rb = radioButton.text.toString()

        return User(
            id = "",
            avatarImage = handleAvatarImage(rb),
            username = username,
            email = email,
            password = password,
            governorate = binding.acGovernorates.text.toString(),
            district = binding.acDistricts.text.toString(),
            gender = rb
        )
    }

    private fun handleAvatarImage(gender: String): String {
        val menAvatarList = arrayListOf(
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_1.png?alt=media&token=06750e12-87c7-480a-a32e-373be9dc8d1f",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_2.png?alt=media&token=eb2cfef6-686f-4354-9d0e-6daf8b800741",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_3.png?alt=media&token=86e32058-3172-407f-83c5-c92e7255d078https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_3.png?alt=media&token=86e32058-3172-407f-83c5-c92e7255d078v",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_4.png?alt=media&token=9cbe0cea-162f-4203-996f-d82957d693e3",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_5.png?alt=media&token=0c13a257-a9df-4ff4-aeff-b8ba0747d379",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_6.png?alt=media&token=700b4ba8-0799-4295-bbf3-6619e1f26802",
        )
        val womenAvatarList = arrayListOf(
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_1.png?alt=media&token=3fe755a4-9e34-4806-ad15-885d2a3e0971",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_2.png?alt=media&token=8b314aac-a237-45d8-97a5-e64f910a1297",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_3.png?alt=media&token=329d6fa9-b636-4ad2-8bde-b6914737a329",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_4.png?alt=media&token=b8eb1c6d-4ad6-41d2-8309-a1a12cc4f9fe",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_5.png?alt=media&token=7ab3ec81-a0bb-40ba-8e92-eb1d9fa6d14a",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fwoman_avatar_6.png?alt=media&token=33205a23-1c55-4a53-83e2-b05663625ca5",
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