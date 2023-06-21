package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.rate

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musfickjamil.snackify.Snackify
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentRateUserSheetBinding
import com.seif.booksislandapp.domain.model.Rate
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.disable
import com.seif.booksislandapp.utils.enabled
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class RateUserBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentRateUserSheetBinding? = null
    private val binding get() = _binding!!
    private val rateSheetViewModel: RateSheetViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRateUserSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        val reporterId = arguments?.getString("reporterId")
        val reportedPersonId = arguments?.getString("reportedPersonId")
        val givenRate = arguments?.getString("givenRate")

        Timber.d("onViewCreated: given rate = $givenRate")
        givenRate?.let { rate ->
            if (rate != "null") {
                binding.ratingbar.rating = rate.toFloat()
                disableSendButton()
                binding.btnSend.text = getString(R.string.edit)

                binding.ratingbar.setOnRatingBarChangeListener { _, rating, _ ->
                    if (rating == rate.toFloat()) {
                        disableSendButton()
                    } else {
                        enableSendButton()
                    }
                }
            }
        }

        binding.btnSend.setOnClickListener {
            rateSheetViewModel.rateAdProvider(
                reporterId!!,
                Rate(
                    reportedPersonId = reportedPersonId!!,
                    rate = binding.ratingbar.rating.toDouble()
                )
            )
            observe()
        }
        binding.btnCancel.setOnClickListener {
            this@RateUserBottomSheet.dismiss()
        }
    }

    private fun disableSendButton() {
        binding.btnSend.disable()
        binding.btnSend.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray_medium
            )
        )
        binding.btnSend.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.light_background
            )
        )
    }

    private fun enableSendButton() {
        binding.btnSend.enabled()
        binding.btnSend.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary
            )
        )
        binding.btnSend.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            rateSheetViewModel.rateSheetState.collect {
                when (it) {
                    RateSheetState.Init -> Unit
                    is RateSheetState.IsLoading -> handleLoadingState(it.isLoading)
                    is RateSheetState.ShowError -> handleErrorState(it.message)
                    is RateSheetState.NoInternetConnection -> handleNoInternetConnectionState(
                        binding.root
                    )

                    is RateSheetState.RateUserSuccessfully -> {
                        Timber.d("observe: given Rate ${it.rates.first}")
                        Timber.d("observe: averageRate ${it.rates.second}")
                        rateSheetViewModel.updateRateState(it.rates)
                        this@RateUserBottomSheet.dismiss()
                    }
                }
            }
        }
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
        Snackify.error(binding.root, message, Snackify.LENGTH_SHORT).setAnchorView(binding.root)
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}