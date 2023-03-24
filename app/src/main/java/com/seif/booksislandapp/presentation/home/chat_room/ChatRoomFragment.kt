package com.seif.booksislandapp.presentation.home.chat_room

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentChatRoomBinding
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.presentation.home.chat_room.adapter.ChatRoomAdapter
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ChatRoomFragment : Fragment() {
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!
    private val chatRoomViewModel: ChatRoomViewModel by viewModels()
    private val chatRoomAdapter: ChatRoomAdapter by lazy { ChatRoomAdapter() }
    private var firebaseCurrentUser: FirebaseUser? = null
    private val args: ChatRoomFragmentArgs by navArgs()
    private lateinit var dialog: AlertDialog
    private var messages: ArrayList<Message> = arrayListOf()
    lateinit var listener: ViewTreeObserver.OnGlobalLayoutListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.chat_gray_background)
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseCurrentUser = chatRoomViewModel.getFirebaseCurrentUser()
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        val receiverId = args.owner.id
        firebaseCurrentUser?.uid?.let { currentId ->
            chatRoomViewModel.requestFetchMessagesBetweenTwoUsers(
                senderId = currentId,
                receiverId = receiverId
            )
        }
        binding.btnSendMessage.setOnClickListener {
            val message = prepareMessage()
            chatRoomViewModel.requestSendMessage(message)
        }
        binding.ivBackChatRoom.setOnClickListener {
            findNavController().navigateUp()
        }
        showReceiverData()
        observe()
        handleKeyboard()

        binding.rvChatRoom.adapter = chatRoomAdapter
    }

    private fun handleKeyboard() {
        listener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            binding.clChatMessages.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.clChatMessages.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // Keyboard is shown
                val lastItem = (chatRoomAdapter.itemCount) - 1
                if (lastItem >= 0) {
                    binding.rvChatRoom.scrollToPosition(lastItem)
                }
            }
        }

        binding.clChatMessages.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    private fun showReceiverData() {
        binding.ivAvatar.load(args.owner.avatarImage)
        binding.tvUsername.text = args.owner.username
    }

    private fun prepareMessage(): Message {

        return Message(
            id = "",
            senderId = firebaseCurrentUser!!.uid,
            receiverId = args.owner.id,
            text = binding.etMessage.text.toString().trim(),
            imageUrl = null,
            date = Date()
        )
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            chatRoomViewModel.chatRoomState.collect {
                when (it) {
                    ChatRoomState.Init -> Unit
                    is ChatRoomState.IsLoading -> handleLoadingState(it.isLoading)
                    is ChatRoomState.FetchMessagesSuccessfullySuccessfully -> {
                        messages = it.messages.toCollection(ArrayList())
                        showMessages(messages)
                    }
                    is ChatRoomState.SendMessageSuccessfully -> {
                        binding.etMessage.text?.clear()
                        // messages.add(it.message)
                        showMessages(messages)
                    }
                    is ChatRoomState.ShowError -> handleErrorState(it.message)
                    is ChatRoomState.NoInternetConnection -> TODO()
                }
            }
        }
    }

    private fun showMessages(messages: ArrayList<Message>) {
        firebaseCurrentUser?.uid?.let { currentId ->
            chatRoomAdapter.addMessages(
                newMessages = messages,
                currentUserId = currentId
            )
            binding.rvChatRoom.scrollToPosition(messages.size - 1)
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

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    override fun onStop() {
        binding.clChatMessages.viewTreeObserver.removeOnGlobalLayoutListener(listener) // to prevent null pointer exception and memory leakks
        super.onStop()
    }

    override fun onDestroyView() {
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.white)
        binding.rvChatRoom.adapter = null
        _binding = null
        dialog.setView(null)
        super.onDestroyView()
    }
}