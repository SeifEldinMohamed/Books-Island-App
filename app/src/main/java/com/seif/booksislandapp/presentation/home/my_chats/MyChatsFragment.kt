package com.seif.booksislandapp.presentation.home.my_chats

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentMyChatsBinding
import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats.MyChatsState
import com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats.MyChatsViewModel
import com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats.adapter.MyChatsAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber

@AndroidEntryPoint
class MyChatsFragment : Fragment(), OnAdItemClick<MyChat> {
    private var _binding: FragmentMyChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private val myChatsAdapter by lazy { MyChatsAdapter() }
    private val myChatsViewModel: MyChatsViewModel by viewModels()
    private lateinit var userId: String
    private var myChats: List<MyChat>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        myChatsAdapter.onAdItemClick = this
        userId = myChatsViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            myChatsViewModel.getMyBuyingChats(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchMyBuyingChats()

        binding.rvBuyingUsersChat.adapter = myChatsAdapter
    }

    private fun fetchMyBuyingChats() {
        if (myChats == null) {
            myChatsViewModel.getMyBuyingChats(userId)
            observe()
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                myChatsViewModel.buyingChatsState.collect {
                    when (it) {
                        MyChatsState.Init -> Unit
                        is MyChatsState.FetchMyChatsSuccessfully -> {
                            myChats = it.myBuyingChat
                            Timber.d("observe: ${it.myBuyingChat}")
                            myChatsAdapter.updateList(it.myBuyingChat)
                            handleUi(it.myBuyingChat)
                        }
                        is MyChatsState.IsLoading -> handleLoadingState(it.isLoading)
                        is MyChatsState.NoInternetConnection -> handleNoInternetConnectionState()
                        is MyChatsState.ShowError -> handleErrorState(it.errorMessage)
                    }
                }
            }
        }
    }

    private fun handleUi(myBuyingChats: List<MyChat>) {
        if (_binding != null) {
            if (myBuyingChats.isEmpty()) {
                binding.rvBuyingUsersChat.hide()
                binding.noBooksAnimationMyBuyingChats.show()
            } else {
                binding.rvBuyingUsersChat.show()
                binding.noBooksAnimationMyBuyingChats.hide()
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
                                fetchMyBuyingChats()
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

    override fun onAdItemClick(item: MyChat, position: Int) {
        val action =
            MyChatsFragmentDirections.actionMyChatsFragmentToChatRoomFragment(item.userIChatWith.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // we don't reset those variables to null bec we need to keep listen for changes so if the user send message and then return back to myChats can see the last message updates in realtime
        binding.rvBuyingUsersChat.adapter = null
        dialog.setView(null)
        // if (findNavController().currentDestination!!.id != R.id.chatRoomFragment) {
        _binding = null
        // }
    }
}