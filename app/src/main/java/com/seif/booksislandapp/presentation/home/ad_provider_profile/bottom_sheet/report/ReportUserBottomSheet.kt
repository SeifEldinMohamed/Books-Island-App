package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.report

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musfickjamil.snackify.Snackify
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentReportUserSheetBinding
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ReportUserBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentReportUserSheetBinding? = null
    private val binding get() = _binding!!
    private val reportSheetViewModel: ReportSheetViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReportUserSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        val reporterId = arguments?.getString("reporterId")
        val reportedPersonId = arguments?.getString("reportedPersonId")

        binding.btnSend.setOnClickListener {
            reportSheetViewModel.reportUser(
                Report(
                    id = "",
                    reporterId = reporterId!!,
                    reportedPersonId = reportedPersonId!!,
                    comment = binding.etComment.text.toString().trim(),
                    category = getReportCategory(binding.rgReport.checkedRadioButtonId)
                )
            )
            observe()
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            reportSheetViewModel.reportSheetState.collect {
                when (it) {
                    ReportSheetState.Init -> Unit
                    is ReportSheetState.IsLoading -> handleLoadingState(it.isLoading)
                    is ReportSheetState.ShowError -> handleErrorState(it.message)
                    is ReportSheetState.NoInternetConnection -> handleNoInternetConnectionState(
                        binding.root
                    )

                    is ReportSheetState.ReportUserSuccessfully -> {
                        Timber.d("observe: report Message ${it.message}")
                        reportSheetViewModel.updateReportState(it.message)
                        this@ReportUserBottomSheet.dismiss()
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

    private fun getReportCategory(checkedRadioButtonId: Int): String {
        return when (checkedRadioButtonId) {
            R.id.rb_threat -> ReportCategory.ThisUserIsThreateningMe.toString()
            R.id.rb_insulting -> ReportCategory.ThisUserIsInsultingMe.toString()
            R.id.rb_spam -> ReportCategory.Spam.toString()
            R.id.rb_fraud -> ReportCategory.Fraud.toString()
            else -> ReportCategory.Other.toString()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}