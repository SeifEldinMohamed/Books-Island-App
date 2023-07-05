package com.seif.booksislandapp.presentation.admin.report_details

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.seif.booksislandapp.databinding.FragmentReportDetailsBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.admin.report_details.viewmodel.ReportDetailsViewModel
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class ReportDetailsFragment : Fragment() {
    private var _binding: FragmentReportDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: ReportDetailsFragmentArgs by navArgs()
    private lateinit var dialog: AlertDialog
    private var user: User? = null
    private val reportDetailsViewModel: ReportDetailsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReportDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnCancelReport.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnAccused.setOnClickListener {
            user?.let {
                val action =
                    ReportDetailsFragmentDirections.actionReportDetailsFragmentToUserDetailsFragment(
                        it
                    )
                findNavController().navigate(action)
            }
        }
        setUserDate()
        reportDetailsViewModel.getUserById(args.report.reportedPersonId)
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            reportDetailsViewModel.userState.collect {
                when (it) {
                    is GetUserByIdState.Init -> Unit
                    is GetUserByIdState.IsLoading -> handleLoadingState(it.isLoading)
                    is GetUserByIdState.GetUserByIdSuccessfully -> {
                        user = it.user
                    }
                    is GetUserByIdState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is GetUserByIdState.NoInternetConnection -> {
                        handleNoInternetConnectionState(binding.root)
                    }
                }
            }
        }
    }

    private fun setUserDate() {
        binding.tvCategoryType.text = args.report.category
        binding.tvReported.text = args.report.reporterName
        binding.tvReportedToName.text = args.report.reportedPersonName
        binding.tvCommentDetails.text = args.report.comment
        val reportNum = args.position + 1
        binding.tvReportNumber.text = reportNum.toString()
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

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        dialog.setView(null)
        _binding = null
        user = null
        super.onDestroyView()
    }
}