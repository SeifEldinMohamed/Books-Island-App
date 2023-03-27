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
import com.seif.booksislandapp.utils.Constants.Companion.AVATAR_MEN_LIST
import com.seif.booksislandapp.utils.Constants.Companion.AVATAR_WOMEN_LIST
import com.seif.booksislandapp.utils.Constants.Companion.IS_LOGGED_IN_KEY
import com.skydoves.balloon.*
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
        binding.ivTooltipPassword.setOnClickListener {
            it.showAlignBottom(createToolTipBalloon(getString(R.string.tooltip_password)))
        }
        binding.ivTooltipUsername.setOnClickListener {
            it.showAlignBottom(createToolTipBalloon(getString(R.string.tooltip_username)))
        }
        binding.ivTooltipEmail.setOnClickListener {
            it.showAlignBottom(createToolTipBalloon(getString(R.string.tooltip_email)))
        }
        binding.tilDistricts.disable()
    }

    private fun createToolTipBalloon(tooltipMessage: String): Balloon {
        return Balloon.Builder(requireContext())
            .setWidthRatio(0.7f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setText(tooltipMessage)
            .setTextColorResource(R.color.white)
            .setTextSize(14f)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setCornerRadius(8f)
            .setBackgroundColorResource(R.color.primary)
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
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
            gender = rb,
            wishListBuy = arrayListOf(),
            wishListDonate = arrayListOf(),
            wishListExchange = arrayListOf(),
            wishListAuction = arrayListOf(),
            myBuyingChats = arrayListOf(),
            mySellingChats = arrayListOf()
        )
    }

    private fun handleAvatarImage(gender: String): String {
        val menAvatarList = AVATAR_MEN_LIST
        val womenAvatarList = AVATAR_WOMEN_LIST
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