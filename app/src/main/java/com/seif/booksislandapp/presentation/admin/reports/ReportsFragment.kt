package com.seif.booksislandapp.presentation.admin.reports

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentReportsBinding
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.presentation.admin.OnReportReviewedItemClick
import com.seif.booksislandapp.presentation.admin.reports.adapter.ReportsAdapter
import com.seif.booksislandapp.presentation.admin.reports.viewmodel.AllReportsViewModel
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReportsFragment : Fragment(), OnAdItemClick<Report>, OnReportReviewedItemClick<Report> {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val allReportsViewModel: AllReportsViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private var allReports: List<Report>? = null
    private val reportsAdapter by lazy { ReportsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        reportsAdapter.onAdItemClick = this
        reportsAdapter.onReportReviewedItemClick = this
        ifReportReviewed()
        fetchAllReports()
        binding.rvReportsRequests.adapter = reportsAdapter
    }

    private fun fetchAllReports() {
        if (allReports == null) {
            allReportsViewModel.getAllReports()
            observe()
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            allReportsViewModel.reportsState.collect {
                when (it) {
                    is AllReportsState.Init -> Unit
                    is AllReportsState.IsLoading -> handleLoadingState(it.isLoading)
                    is AllReportsState.GetAllReportsSuccessfully -> {
                        allReports = it.reports
                        reportsAdapter.updateList(it.reports)
                        binding.tvReportsNum.text = it.reports.size.toString()
                    }
                    is AllReportsState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is AllReportsState.NoInternetConnection -> {
                        handleNoInternetConnectionState(binding.root)
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
        binding.root.showErrorSnackBar(message)
    }
    private fun ifReportReviewed() {
        lifecycleScope.launch {
            allReportsViewModel.reportReviewState.collect {
                when (it) {
                    ReviewedState.Init -> Unit
                    is ReviewedState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is ReviewedState.ShowError -> handleErrorState(it.message)
                    is ReviewedState.UpdatedSuccessfully -> Unit
                }
            }
        }
    }

    override fun onAdItemClick(item: Report, position: Int) {
        val action =
            ReportsFragmentDirections.actionReportsFragmentToReportDetailsFragment(item, position)
        findNavController().navigate(action)
    }

    override fun onReportReviewedItemClick(item: Report) {
        allReportsViewModel.setReviewed(item.id)
    }

    override fun onDestroyView() {
        reportsAdapter.onAdItemClick = null
        reportsAdapter.onReportReviewedItemClick = null
        binding.rvReportsRequests.adapter = null
        dialog.setView(null)
        _binding = null
        allReports = null
        super.onDestroyView()
    }
}