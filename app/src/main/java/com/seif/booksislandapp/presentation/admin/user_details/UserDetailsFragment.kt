package com.seif.booksislandapp.presentation.admin.user_details

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
import coil.load
import com.seif.booksislandapp.databinding.FragmentUserDetailsBinding
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.presentation.admin.reports.AllReportsState
import com.seif.booksislandapp.presentation.admin.user_details.adapter.UserDetailsAdapter
import com.seif.booksislandapp.presentation.admin.user_details.viewmodel.UserDetailsViewModel
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class UserDetailsFragment : Fragment(), OnAdItemClick<Report> {
    private var _binding: FragmentUserDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: UserDetailsFragmentArgs by navArgs()
    private lateinit var dialog: AlertDialog
    private val userDetailsViewModel: UserDetailsViewModel by viewModels()
    private var allReports: List<Report>? = null
    private val userDetailsAdapter by lazy { UserDetailsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        userDetailsAdapter.onAdItemClick = this
        setUserDate()
        fetchAllReports()
        binding.rvReports.adapter = userDetailsAdapter
    }
    private fun setUserDate() {
        binding.ivOwnerAvatar.load(args.user.avatarImage)
        binding.tvOwnerName.text = args.user.username
        binding.tvUserEmail.text = args.user.email
        // binding.tvUserRate.text= args.user.rate
        val location = args.user.district + "," + args.user.governorate
        binding.tvUserLocation.text = location
    }

    private fun fetchAllReports() {
        if (allReports == null) {
            userDetailsViewModel.getAllReports(args.user.id)
            observe()
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            userDetailsViewModel.reportsState.collect {
                when (it) {
                    is AllReportsState.Init -> Unit
                    is AllReportsState.IsLoading -> handleLoadingState(it.isLoading)
                    is AllReportsState.GetAllReportsSuccessfully -> {
                        allReports = it.reports
                        userDetailsAdapter.updateList(it.reports)
                        binding.tvTotalReportsNum.text = it.reports.size.toString()
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

    override fun onAdItemClick(item: Report, position: Int) {
        val action = UserDetailsFragmentDirections.actionUserDetailsFragmentToReportDetailsFragment(item, position)
        findNavController().navigate(action)
    }
}