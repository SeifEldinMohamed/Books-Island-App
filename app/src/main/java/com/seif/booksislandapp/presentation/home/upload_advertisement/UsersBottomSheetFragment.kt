package com.seif.booksislandapp.presentation.home.upload_advertisement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentUsersBottomSheetBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class UsersBottomSheetFragment : BottomSheetDialogFragment(), OnAdItemClick<User> {
    private var _binding: FragmentUsersBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val userAdapter: UserAdapter by lazy { UserAdapter() }
    private val itemUserViewModel: ItemUserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUsersBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userAdapter.onAdItemClick = this

        // fetch the users who chat with current user
        val currentUserId = itemUserViewModel.getCurrentUser().uid
        itemUserViewModel.fetchUsersIChatWith(currentUserId)
        observe()

        binding.rvUsers.adapter = userAdapter
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            itemUserViewModel.usersIChatWithList.collect {
                when (it) {
                    UsersBottomSheetState.Init -> Unit
                    is UsersBottomSheetState.IsLoading -> handleLoadingState(it.isLoading)
                    is UsersBottomSheetState.NoInternetConnection -> handleNoInternetConnectionState()
                    is UsersBottomSheetState.ShowError -> handleErrorState(it.message)
                    is UsersBottomSheetState.FetchUsersIChatWithSuccessfully -> {
                        Timber.d("observe: users i chat with ${it.usersIChatWith}")
                        userAdapter.updateList(it.usersIChatWith)
                    }
                }
            }
        }
    }

    private fun handleNoInternetConnectionState() {
        NoInternetDialogPendulum.Builder(
            requireActivity(),
            viewLifecycleOwner.lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        when (hasActiveConnection) {
                            true -> {
                                binding.root.showInfoSnackBar("Internet connection is back")
                                val currentUserId = itemUserViewModel.getCurrentUser().uid
                                itemUserViewModel.fetchUsersIChatWith(currentUserId)
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
            true -> binding.pbBottomSheet.show()
            false -> binding.pbBottomSheet.hide()
        }
    }

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onAdItemClick(item: User, position: Int) {
        Timber.d("onAdItemClick: clicked $item")
        itemUserViewModel.selectedUser(selectedUser = item)
        dismiss()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}