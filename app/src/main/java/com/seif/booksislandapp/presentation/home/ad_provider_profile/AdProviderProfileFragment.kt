package com.seif.booksislandapp.presentation.home.ad_provider_profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentAdProviderProfileBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.rate.RateSheetViewModel
import com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.rate.RateUserBottomSheet
import com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.report.ReportSheetViewModel
import com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.report.ReportUserBottomSheet
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import com.seif.booksislandapp.utils.showSuccessSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class AdProviderProfileFragment : Fragment() {
    private var _binding: FragmentAdProviderProfileBinding? = null
    private val binding get() = _binding!!

    private val adProviderProfileViewModel: AdProviderProfileViewModel by viewModels()
    private val reportSheetViewModel: ReportSheetViewModel by activityViewModels()
    private val rateSheetViewModel: RateSheetViewModel by activityViewModels()
    private val args = navArgs<AdProviderProfileFragmentArgs>()
    private var adProviderUser: User? = null
    private var currentUser: User? = null
    private var currentUserId: String? = null
    private var givenRate: String? = null
    private lateinit var dialog: AlertDialog

    private lateinit var adProviderUserId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,

        ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdProviderProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        currentUserId = args.value.currentUserId
        adProviderUserId = args.value.providerId

        if (currentUserId == adProviderUserId) { // hide menu if current user is ad provider
            binding.toolbar.menu.clear()
        } else {
            currentUserId?.let {
                fetchCurrentUserById(it)
            }
        }

        if (adProviderUser == null) {
            adProviderProfileViewModel.getAdProviderUserById(adProviderUserId)
        }

        observe()
        onMenuItemClick()

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun handleBlockMenuItemText() {
        if (currentUser!!.blockedUsersIds.contains(adProviderUserId)) { // user blocked adProvider
            Timber.d("onViewCreated: show unblock")
            binding.toolbar.menu.findItem(R.id.menu_block).title = getString(R.string.unblock_user)
        } else {
            Timber.d("onViewCreated: show block")
            binding.toolbar.menu.findItem(R.id.menu_block).title = getString(R.string.block_user)
        }
    }

    private fun fetchCurrentUserById(currentUserId: String) {
        if (currentUser == null) {
            adProviderProfileViewModel.fetchCurrentUser(currentUserId)
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            adProviderProfileViewModel.adProviderProfileState.collect {
                when (it) {
                    AdProviderProfileState.Init -> Unit
                    is AdProviderProfileState.IsLoading -> handleLoadingState(it.isLoading)
                    is AdProviderProfileState.NoInternetConnection -> handleNoInternetConnectionState()
                    is AdProviderProfileState.ShowError -> handleErrorState(it.message)
                    is AdProviderProfileState.FetchAdProviderUserSuccessfully -> {
                        adProviderUser = it.user
                        showAdProviderData(it.user)
                    }

                    is AdProviderProfileState.FetchCurrentUserSuccessfully -> {
                        currentUser = it.user
                        givenRate = it.user.givenRates.find { givenRate ->
                            givenRate.reportedPersonId == adProviderUserId
                        }?.rate.toString()
                        handleBlockMenuItemText()
                    }

                    is AdProviderProfileState.BlockUserSuccessfully -> {
                        binding.root.showSuccessSnackBar(it.message)
                        if (it.message == getString(R.string.blocked_successfully)) {
                            binding.toolbar.menu.findItem(R.id.menu_block).title =
                                getString(R.string.unblock_user)
                        } else {
                            binding.toolbar.menu.findItem(R.id.menu_block).title =
                                getString(R.string.block_user)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showAdProviderData(user: User) {
        binding.tvUsername.text = user.username
        binding.tvLocation.text = "${user.district}, ${user.governorate}"
        binding.ivAvatar.load(user.avatarImage)
        binding.tvRate.text = "${user.averageRate} / 5"
        binding.ratingbar.rating = user.averageRate.toFloat()
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
                                // fetchAdProviderUser()
                            }

                            false -> Unit
                        }
                    }
                }

                cancelable = false // Optional
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

    private fun handleLoadingState(isLoading: Boolean) {
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

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    private fun onMenuItemClick() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_report -> {
                    val reportUserBottomSheet = ReportUserBottomSheet()
                    val bundle = Bundle()

                    bundle.putString("reporterId", currentUserId)
                    bundle.putString("reportedPersonId", adProviderUserId)
                    reportUserBottomSheet.arguments = bundle
                    reportUserBottomSheet.show(parentFragmentManager, " ")
                    observeReportSent()
                }

                R.id.menu_rate -> {
                    val rateUserBottomSheet = RateUserBottomSheet()
                    val bundle = Bundle()

                    bundle.putString("reporterId", currentUserId)
                    bundle.putString("reportedPersonId", adProviderUserId)
                    Timber.d("onViewCreated: In Ad Provider given rate = $givenRate")
                    bundle.putString("givenRate", givenRate)
                    rateUserBottomSheet.arguments = bundle
                    rateUserBottomSheet.show(parentFragmentManager, " ")
                    observeRateSent()
                }

                R.id.menu_block -> {
                    // block this seller then if success show snack bar
                    if (item.title == getString(R.string.block_user)) {
                        adProviderProfileViewModel.blockUser(
                            currentUserId!!,
                            adProviderUserId,
                            true
                        )
                    } else { // unblock
                        adProviderProfileViewModel.blockUser(
                            currentUserId!!,
                            adProviderUserId,
                            false
                        )
                    }
                }

                else -> {
                }
            }
            true
        }
    }

    private fun observeReportSent() {
        reportSheetViewModel.reportSent.observe(viewLifecycleOwner) { message ->
            message?.let {
                binding.root.showSuccessSnackBar(message)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeRateSent() {
        rateSheetViewModel.rateSent.observe(viewLifecycleOwner) { rates ->
            rates?.let {
                val returnedGivenRate = rates.first
                val averageRate = rates.second
                binding.root.showSuccessSnackBar(getString(R.string.rate_user_successfully))
                binding.tvRate.text = getString(R.string.rate_value, averageRate)
                binding.ratingbar.rating = averageRate.toFloat()
                givenRate = returnedGivenRate
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
