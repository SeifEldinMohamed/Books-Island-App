package com.seif.booksislandapp.presentation.admin.all_users

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentAllUsersBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.admin.all_users.adapter.AllUsersAdapter
import com.seif.booksislandapp.presentation.admin.all_users.viewmodel.AllUsersViewModel
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllUsersFragment : Fragment(), OnAdItemClick<User> {
    private var _binding: FragmentAllUsersBinding? = null
    private val binding get() = _binding!!
    private val allUsersViewModel: AllUsersViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val allUsersAdapter by lazy { AllUsersAdapter() }
    private var allUsers: List<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAllUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        allUsersAdapter.onAdItemClick = this
        fetchAllUsers()
        binding.rvUsers.adapter = allUsersAdapter
    }
    private fun fetchAllUsers() {
        if (allUsers == null) {

            allUsersViewModel.getAllUsers()
            observe()
        }
    }
    private fun observe() {
        lifecycleScope.launch {
            allUsersViewModel.usersState.collect {
                when (it) {
                    GetAllUsersState.Init -> Unit
                    is GetAllUsersState.IsLoading -> handleLoadingState(it.isLoading)
                    is GetAllUsersState.GetAllUsersSuccessfully -> {
                        allUsers = it.users
                        allUsersAdapter.updateList(it.users)
                    }
                    is GetAllUsersState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is GetAllUsersState.NoInternetConnection -> {
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

    override fun onAdItemClick(item: User, position: Int) {
        val action = AllUsersFragmentDirections.actionAllUsersFragmentToUserDetailsFragment(item)
        findNavController().navigate(action)
    }
}