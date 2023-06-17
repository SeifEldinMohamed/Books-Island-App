package com.seif.booksislandapp.presentation.home.chat_room

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentChatRoomBinding
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.presentation.home.chat_room.adapter.ChatRoomAdapter
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.FileUtil
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.hideKeyboard
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showInfoSnackBar
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class ChatRoomFragment : Fragment() {
    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!
    private val chatRoomViewModel: ChatRoomViewModel by viewModels()
    private val chatRoomAdapter: ChatRoomAdapter by lazy { ChatRoomAdapter() }
    private var firebaseCurrentUser: FirebaseUser? = null

    // private val args: ChatRoomFragmentArgs by navArgs()
    private lateinit var dialog: AlertDialog
    private var messages: ArrayList<Message> = arrayListOf()
    lateinit var listener: ViewTreeObserver.OnGlobalLayoutListener
    lateinit var imageUris: Uri
    private var receiverUserId: String? = null
    private var currentUser: User? = null

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
        if (firebaseCurrentUser == null) {
            firebaseCurrentUser = chatRoomViewModel.getFirebaseCurrentUser()
        }
        dialog = requireContext().createLoadingAlertDialog(requireActivity())

        receiverUserId = arguments?.getString("ownerId")
        if (receiverUserId == null) {
            arguments?.let {
                receiverUserId = it.getString("userId", "")
            }
        }
        receiverUserId?.let {
            chatRoomViewModel.fetchUserById(it)
            fetchMessagesBetweenTwoUsers(it)
        }

        binding.btnSendMessage.setOnClickListener {
            val message = prepareMessage()
            chatRoomViewModel.requestSendMessage(message)
        }
        binding.ivUploadImage.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        binding.ivBackChatRoom.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.clProfile.setOnClickListener {
            receiverUserId?.let { ownerId ->
                firebaseCurrentUser?.uid?.let { currentUserId ->
                    val action =
                        ChatRoomFragmentDirections.actionChatRoomFragmentToAdProviderProfile(
                            providerId = ownerId,
                            currentUserId = currentUserId
                        )
                    currentUser = null
                    findNavController().navigate(action)
                }
            }
        }

        observe()

        //  handleKeyboard() //  handling the keyboard visibility changes and scrolling the RecyclerView when the keyboard is shown.
        chatRoomViewModel.setInMyChats(Constants.NOT_IN_MYCHATS_OR_CHATROOM, false)
        binding.rvChatRoom.adapter = chatRoomAdapter
    }

    private fun fetchCurrentUserById(currentUserId: String) {
        if (currentUser == null) {
            chatRoomViewModel.fetchCurrentUserById(currentUserId)
        }
    }

    private fun fetchMessagesBetweenTwoUsers(receiverUserId: String) {
        if (messages.isEmpty()) {
            firebaseCurrentUser?.uid?.let { currentId ->
                chatRoomViewModel.requestFetchMessagesBetweenTwoUsers(
                    senderId = currentId,
                    receiverId = receiverUserId
                )
            }
        }
    }

    private fun handleKeyboard() {
        // This listener is triggered whenever the layout changes, including changes in keyboard visibility.
        listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect() // hold the visible display frame of the activity's root view.

            // populates the Rect object with the visible display frame of the activity's root view.
            // This frame represents the area of the screen that is not obscured by system windows (such as the keyboard).
            binding.clChatMessages.getWindowVisibleDisplayFrame(rect)
            val screenHeight =
                binding.clChatMessages.rootView.height // retrieves the height of the root view, which corresponds to the height of the entire screen.
            val keypadHeight =
                screenHeight - rect.bottom // calculates the height of the keyboard by subtracting the bottom coordinate of the visible display frame from the screen height.
            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // Keyboard is shown
                Timber.d("handleKeyboard: keyboard is shown")
                val lastItem = (binding.rvChatRoom.adapter!!.itemCount) - 1
                if (lastItem >= 0) {
                    Timber.d("handleKeyboard: smooth scrooll to $lastItem")
                    binding.rvChatRoom.smoothScrollToPosition(lastItem)
                }
            }
        }

        binding.clChatMessages.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    private fun showReceiverData(receiverUser: User) {
        binding.ivAvatar.load(receiverUser.avatarImage)
        binding.tvUsername.text = receiverUser.username
    }

    private fun prepareMessage(): Message {

        return Message(
            id = "",
            senderId = firebaseCurrentUser!!.uid,
            receiverId = receiverUserId!!,
            text = binding.etMessage.text.toString().trim(),
            imageUrl = null
        )
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatRoomViewModel.chatRoomState.collect {
                    when (it) {
                        ChatRoomState.Init -> Unit
                        is ChatRoomState.IsLoading -> handleLoadingState(it.isLoading)
                        is ChatRoomState.FetchMessagesSuccessfully -> {
                            messages = it.messages.toCollection(ArrayList())
                            if (messages.isNotEmpty()) {
                                if (messages.last().senderId == firebaseCurrentUser!!.uid)
                                    binding.etMessage.setText("")

                                showMessages(messages)
                                // update received messages to isSeen to true
                                chatRoomViewModel.updateReceivedMessagesIsSeen(
                                    firebaseCurrentUser!!.uid,
                                    receiverUserId!!,
                                    messages
                                )
                            }
                        }

                        is ChatRoomState.FetchReceiverUserSuccessfully -> {
                            receiverUserId = it.user.id
                            // fetchMessagesBetweenTwoUsers(receiverUserId = it.user.id)
                            showReceiverData(receiverUser = it.user)

                            firebaseCurrentUser?.uid?.let { currUserId ->
                                fetchCurrentUserById(currUserId)
                            }
                        }

                        is ChatRoomState.FetchCurrentUserSuccessfully -> {
                            currentUser = it.user
                            handleIsReceiverBlocked(currentUser!!.blockedUsersIds)
                        }

                        is ChatRoomState.SendMessageSuccessfully -> {
                            dismissLoadingDialog() // in case of upload image
                        }

                        is ChatRoomState.ShowError -> handleErrorState(it.message)
                        is ChatRoomState.NoInternetConnection -> handleNoInternetConnectionState()
                    }
                }
            }
        }
    }

    private fun handleIsReceiverBlocked(blockedUsersIds: List<String>) {
        if (blockedUsersIds.contains(receiverUserId)) { // blocked
            Timber.d("handleIsReceiverBlocked: blocked")
            binding.chatLayout.hide()
            binding.clConversationIsBlocked.show()
        } else {
            Timber.d("handleIsReceiverBlocked: not blocked")
            binding.chatLayout.show()
            binding.clConversationIsBlocked.hide()
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
                                // fetchMessagesBetweenTwoUsers(receiverUserId)
                                chatRoomViewModel.fetchUserById(receiverUserId!!)
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

    private fun showMessages(messages: ArrayList<Message>) {
        firebaseCurrentUser?.uid?.let { currentId ->
            chatRoomAdapter.addMessages(
                newMessages = messages,
                currentUserId = currentId
            )
            binding.rvChatRoom.scrollToPosition(messages.size - 1)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // PERMISSION GRANTED
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startForProfileImageResult.launch(galleryIntent)
        } else {
            // PERMISSION NOT GRANTED
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            startLoadingDialog()

            val resultCode: Int = activityResult.resultCode
            val data: Intent? = activityResult.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    var compressedFile: File?
                    data?.let {
                        it.data?.let { uri ->
                            // imageUri = uri
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                compressedFile = FileUtil.from(requireContext(), uri)?.let { it1 ->
                                    Compressor.compress(
                                        requireContext(),
                                        it1
                                    )
                                }
                                //  uploadSellAdvertisementViewModel.addImagesUris(imageUris)
                                withContext(Dispatchers.Main) {
                                    imageUris = Uri.fromFile(compressedFile)
                                    // uploadedImagesAdapter.updateList(imageUris)
                                    chatRoomViewModel.requestSendMessage(
                                        Message(
                                            id = "",
                                            senderId = firebaseCurrentUser!!.uid,
                                            receiverId = receiverUserId!!,
                                            text = getString(R.string.sent_you_image),
                                            imageUrl = imageUris
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    dismissLoadingDialog()
                    Timber.d("Task Cancelled")
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
        chatRoomViewModel.setInMyChats(Constants.NOT_IN_MYCHATS_OR_CHATROOM, true)
        currentUser = null
        Timber.d("onResume: on stoooooooooooooooooooop current user = $currentUser")
        //  hideKeyboard()
        super.onStop()
    }

    override fun onResume() {
        binding.etMessage.clearFocus()
        hideKeyboard()
        handleKeyboard() // to scroll to last item when user returns to app and show keyboard
        Timber.d("onResume: on resuemmmmmmmmmmmmmmmmmmmm current user = $currentUser")
        currentUser = null
        fetchCurrentUserById(firebaseCurrentUser!!.uid)
        super.onResume()
    }

    override fun onDestroyView() {
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.white)
        binding.rvChatRoom.adapter = null
        _binding = null
        dialog.setView(null)
        super.onDestroyView()
    }
}

//    private fun handleKeyboard() {
//        // val fragmentRootView = binding.clChatMessages.rootView
//        // val originalPaddingBottom = fragmentRootView.paddingBottom
//
//        // This listener is triggered whenever the layout changes, including changes in keyboard visibility.
//        listener = ViewTreeObserver.OnGlobalLayoutListener {
//            val rect = Rect() // hold the visible display frame of the activity's root view.
//            // populates the Rect object with the visible display frame of the activity's root view.
//            // This frame represents the area of the screen that is not obscured by system windows (such as the keyboard).
//            binding.clChatMessages.getWindowVisibleDisplayFrame(rect)
//            val screenHeight = binding.clChatMessages.rootView.height // retrieves the height of the root view, which corresponds to the height of the entire screen.
//            val keypadHeight = screenHeight - rect.bottom // calculates the height of the keyboard by subtracting the bottom coordinate of the visible display frame from the screen height.
//
//            //  checks if the keyboard height is greater than 15% of the screen height. You can adjust this ratio as needed
//            //  to determine when the keyboard is considered visible.
//            if (keypadHeight > screenHeight * 0.15) { // Adjust the ratio as needed
//                // Keyboard is shown
//                // val lastItemIndex = binding.rvChatRoom.adapter?.itemCount?.minus(1) ?: -1
//                Timber.d("handleKeyboard: keyboard is shown")
//                if (messages.size >= 0) {
//                    binding.rvChatRoom.post { // ensures that this operation is performed after the layout has been updated.
//                        // Scroll to the last item with smooth scrolling
//                        Timber.d("handleKeyboard: scroll to last message")
//                        binding.rvChatRoom.smoothScrollToPosition(messages.size - 1)
//                    }
//                }
//            } else {
//                // Keyboard is hidden
//                binding.rvChatRoom.post {
//                    // Restore the original padding to avoid any layout issues
//                    binding.clChatMessages.setPadding(
//                        binding.clChatMessages.paddingLeft,
//                        binding.clChatMessages.paddingTop,
//                        binding.clChatMessages.paddingRight,
//                        binding.clChatMessages.paddingBottom
//                    )
//                }
//            }
//        }
//    }