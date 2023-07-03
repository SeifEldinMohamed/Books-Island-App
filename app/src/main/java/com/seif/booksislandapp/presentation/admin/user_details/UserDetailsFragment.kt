package com.seif.booksislandapp.presentation.admin.user_details

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.seif.booksislandapp.R
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
import timber.log.Timber

@AndroidEntryPoint
class UserDetailsFragment : Fragment(), OnAdItemClick<Report> {
    private var _binding: FragmentUserDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: UserDetailsFragmentArgs by navArgs()
    private lateinit var dialog: AlertDialog
    private val userDetailsViewModel: UserDetailsViewModel by viewModels()
    private var allReports: List<Report>? = null
    private val userDetailsAdapter by lazy { UserDetailsAdapter() }
    private var isSuspended: Boolean? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        isSuspended = args.user.isSuspended
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        Timber.d(isSuspended.toString())
        userDetailsAdapter.onAdItemClick = this
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnSuspend.setOnClickListener {
            isSuspended?.let {
                isSuspended = !it
            }
            isSuspended?.let {
                if (it) {
                    binding.btnSuspend.text = getString(R.string.un_suspend)
                } else {
                    binding.btnSuspend.text = getString(R.string.suspend)
                }
                userDetailsViewModel.handleSuspendState(it, args.user.id)
            }
        }
        setUserDate()
        observeOnSuspendState()
        fetchAllReports()
        binding.rvReports.adapter = userDetailsAdapter
    }

    private fun setUserDate() {
        binding.ivOwnerAvatar.load(args.user.avatarImage) {
            crossfade(200)
        }
        binding.tvOwnerName.text = args.user.username
        binding.tvUserEmail.text = args.user.email
        binding.tvUserRate.text = args.user.averageRate
        binding.btnSuspend.text = if (args.user.isSuspended) {
            getString(R.string.un_suspend)
        } else {
            getString(R.string.suspend)
        }
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
                        Timber.d(it.message)
                    }
                    is AllReportsState.NoInternetConnection -> {
                        handleNoInternetConnectionState(binding.root)
                    }
                }
            }
        }
    }

    private fun observeOnSuspendState() {
        lifecycleScope.launch {
            userDetailsViewModel.suspendState.collect {
                when (it) {
                    SuspendState.Init -> Unit
                    is SuspendState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is SuspendState.ShowError -> handleErrorState(it.message)
                    is SuspendState.UpdatedSuccessfully -> {
                        when (it.suspendState) {
                            true -> binding.btnSuspend.text = getString(R.string.un_suspend)
                            false -> binding.btnSuspend.text = getString(R.string.suspend)
                        }
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
        val action = UserDetailsFragmentDirections.actionUserDetailsFragmentToReportDetailsFragment(
            item,
            position
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        userDetailsAdapter.onAdItemClick = null
        binding.rvReports.adapter = null
        dialog.setView(null)
        _binding = null
        isSuspended = null
        allReports = null
        super.onDestroyView()
    }
}