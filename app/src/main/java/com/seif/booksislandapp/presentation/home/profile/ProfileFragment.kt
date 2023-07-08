package com.seif.booksislandapp.presentation.home.profile

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentProfileBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.presentation.intro.IntroActivity
import com.seif.booksislandapp.utils.Constants.Companion.USER_ID_KEY
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.disable
import com.seif.booksislandapp.utils.enabled
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import com.seif.booksislandapp.utils.showSuccessSnackBar
import com.seif.booksislandapp.utils.start
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private lateinit var user: User
    private var governorates: List<Governorate>? = null
    private var districts: List<District>? = null
    private var governorateId: String? = null
    private lateinit var avatarImage: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvLogout.setOnClickListener {
            profileViewModel.requestLogout()
        }

        binding.btnUpdateProfile.setOnClickListener {
            val user = prepareUserData()
            profileViewModel.updateUserProfileData(user)
        }

        binding.ivAvatar.setOnClickListener {
            changeAvatarImage()
        }

        binding.acGovernorate.setOnItemClickListener { _, _, i, _ ->
            governorates?.let {
                governorateId = it[i].id
                profileViewModel.getDistricts(it[i].id)
            }
        }
        binding.acDistricts.setOnItemClickListener { _, _, _, _ ->
            enableUpdateProfileButton()
        }

        requestUserProfileData()
        observe()
    }

    private fun changeAvatarImage() {
        binding.ivAvatar.disable()
        avatarImage = handleAvatarImage(user.gender)
        binding.ivAvatar.load(avatarImage) {
            crossfade(200)
        }
        enableUpdateProfileButton()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(500)
                binding.ivAvatar.enabled()
            }
        }
    }

    private fun prepareUserData(): User {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        return User(
            id = user.id,
            avatarImage = avatarImage,
            username = username,
            email = email,
            password = user.password,
            governorate = binding.acGovernorate.text.toString(),
            district = binding.acDistricts.text.toString(),
            gender = user.gender,
            wishListBuy = user.wishListBuy,
            wishListDonate = user.wishListDonate,
            wishListExchange = user.wishListExchange,
            wishListAuction = user.wishListAuction,
            blockedUsersIds = user.blockedUsersIds,
            averageRate = user.averageRate,
            givenRates = user.givenRates,
            receivedRates = user.receivedRates,
            numberOfCompletedSellAds = user.numberOfCompletedSellAds,
            numberOfCompletedDonateAds = user.numberOfCompletedDonateAds,
            numberOfCompletedExchangeAds = user.numberOfCompletedExchangeAds,
            numberOfCompletedAuctionAds = user.numberOfCompletedAuctionAds,
            isSuspended = user.isSuspended
        )
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.profileState.collect {
                    when (it) {
                        ProfileState.Init -> Unit
                        is ProfileState.GetUserByIdSuccessfully -> {
                            user = it.user
                            avatarImage = user.avatarImage
                            showUserProfileData(it.user)
                            profileViewModel.getGovernorates()
                        }

                        is ProfileState.UpdateUserProfileSuccessfully -> {
                            binding.root.showSuccessSnackBar(getString(R.string.profile_updated_successfully))
                            user = it.user
                            binding.tvUsername.text = user.username
                            enableUpdateProfileButton()
                        }

                        is ProfileState.GetGovernoratesSuccessfully -> {
                            governorates = it.governorates
                            setUpGovernoratesDropDown(it.governorates)
                            governorateId?.let { id ->
                                profileViewModel.getDistricts(id)
                            }
                        }

                        is ProfileState.GetDistrictsSuccessfully -> {
                            districts = it.districts
                            binding.acDistricts.setText("")
                            setUpDistrictsDropDown(it.districts)
                            enableUpdateProfileButton()
                            listenForUserInput()
                        }

                        is ProfileState.LogoutSuccessfully -> {
                            binding.root.showSuccessSnackBar(it.message)
                            requireActivity().apply {
                                start<IntroActivity>()
                                finish()
                            }
                        }

                        is ProfileState.IsLoading -> handleLoadingState(it.isLoading)
                        is ProfileState.NoInternetConnection -> handleNoInternetConnectionState()
                        is ProfileState.ShowError -> handleErrorState(it.message)
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
                                if (binding.etUsername.text.toString().isEmpty()) {
                                    requestUserProfileData()
                                }
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

    private fun requestUserProfileData() {
        val userId = profileViewModel.readFromSP(USER_ID_KEY, String::class.java)
        profileViewModel.getUserById(userId)
    }

    private fun handleAvatarImage(gender: String): String {
        val menAvatarList = arrayListOf(
            // don't use variable in constants bec it doesn't initialize each time we call fun ( weired )
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_1.png?alt=media&token=06750e12-87c7-480a-a32e-373be9dc8d1f",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fman_avatar_2.png?alt=media&token=eb2cfef6-686f-4354-9d0e-6daf8b800741",
            "https://firebasestorage.googleapis.com/v0/b/books-island-app.appspot.com/o/app%2Favatars%2Fmen_avatar_3.png?alt=media&token=87de7013-b2a8-4f0c-8bf4-0d7bfed35364",
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
            getString(R.string.male) -> {
                menAvatarList.remove(avatarImage)
                menAvatarList.random().toString()
            }

            getString(R.string.female) -> {
                womenAvatarList.remove(avatarImage)
                womenAvatarList.random().toString()
            }

            else -> Unit.toString()
        }
    }

    private fun enableUpdateProfileButton() {
        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val governorate = binding.acGovernorate.text.toString()
        val district = binding.acDistricts.text.toString()
        Timber.d("enableUpdateProfileButton: $district ---- user cureent district: ${user.district}")
        binding.btnUpdateProfile.isEnabled =
            !(
                username == user.username && email == user.email &&
                    governorate == user.governorate && district == user.district &&
                    avatarImage == user.avatarImage
                )
    }

    private fun listenForUserInput() {
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                enableUpdateProfileButton()
            }

            override fun afterTextChanged(p0: Editable?) {
                // binding.tvUsername.text = p0.toString()
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                enableUpdateProfileButton()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun showUserProfileData(user: User) {
        binding.ivAvatar.load(user.avatarImage) {
            crossfade(200)
        }
        binding.etEmail.setText(user.email)
        binding.etUsername.setText(user.username)
        binding.tvUsername.text = user.username
        binding.tvRate.text = getString(R.string.user_rate_value, user.averageRate)
    }

    private fun setUpDistrictsDropDown(districts: List<District>) {
        val districtsName: List<String> = districts.map { it.name }
        val userDistrictIdIndex = districtsName.indexOfFirst { it == user.district }
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, districtsName)
        if (userDistrictIdIndex != -1) {
            binding.acDistricts.setText(arrayAdapter.getItem(userDistrictIdIndex))
        }
        binding.acDistricts.setAdapter(arrayAdapter)
    }

    private fun setUpGovernoratesDropDown(governorates: List<Governorate>) {
        val governoratesName: List<String> = governorates.map { it.name }
        val userGovernorateIdIndex = governoratesName.indexOfFirst { it == user.governorate }
        governorateId = governorates[userGovernorateIdIndex].id
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, governoratesName)
        binding.acGovernorate.setText(arrayAdapter.getItem(userGovernorateIdIndex))
        binding.acGovernorate.setAdapter(arrayAdapter)
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
        super.onDestroyView()
        dialog.setView(null)
        _binding = null
    }
}