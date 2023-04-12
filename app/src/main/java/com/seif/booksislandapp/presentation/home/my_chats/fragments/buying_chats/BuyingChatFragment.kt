package com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentBuyingChatBinding
import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.presentation.home.categories.OnAdItemClick
import com.seif.booksislandapp.presentation.home.my_chats.MyChatsFragmentDirections
import com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats.adapter.MyChatsAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum

@AndroidEntryPoint
class BuyingChatFragment : Fragment(), OnAdItemClick<MyChat> {
    private var _binding: FragmentBuyingChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private val myChatsAdapter by lazy { MyChatsAdapter() }
    private val buyingChatsViewModel: BuyingChatsViewModel by viewModels()
    private lateinit var userId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyingChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        myChatsAdapter.onAdItemClick = this
        userId = buyingChatsViewModel.getFromSP(Constants.USER_ID_KEY, String::class.java)

        binding.swipeRefresh.setOnRefreshListener {
            buyingChatsViewModel.getMyBuyingChats(userId)
            observe()
            binding.swipeRefresh.isRefreshing = false
        }
        fetchMyBuyingChats()

        binding.rvBuyingUsersChat.adapter = myChatsAdapter
    }

    private fun fetchMyBuyingChats() {
        buyingChatsViewModel.getMyBuyingChats(userId)
        observe()
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            buyingChatsViewModel.buyingChatsState.collect {
                when (it) {
                    MyBuyingChatsState.Init -> Unit
                    is MyBuyingChatsState.FetchMyBuyingChatsSuccessfully -> {
                        myChatsAdapter.updateList(it.myBuyingChat)
                        handleUi(it.myBuyingChat)
                    }
                    is MyBuyingChatsState.IsLoading -> handleLoadingState(it.isLoading)
                    is MyBuyingChatsState.NoInternetConnection -> handleNoInternetConnectionState()
                    is MyBuyingChatsState.ShowError -> handleErrorState(it.errorMessage)
                }
            }
        }
    }

    private fun handleUi(myBuyingChats: List<MyChat>) {
        if (myBuyingChats.isEmpty()) {
            binding.rvBuyingUsersChat.hide()
            binding.noBooksAnimationMyBuyingChats.show()
        } else {
            binding.rvBuyingUsersChat.show()
            binding.noBooksAnimationMyBuyingChats.hide()
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
            MyChatsFragmentDirections.actionMyChatsFragmentToChatRoomFragment(item.userIChatWith)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvBuyingUsersChat.adapter = null
        dialog.setView(null)
        _binding = null
    }
}