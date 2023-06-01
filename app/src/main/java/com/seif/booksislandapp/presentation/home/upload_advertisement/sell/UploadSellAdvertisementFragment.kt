package com.seif.booksislandapp.presentation.home.upload_advertisement.sell

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentUploadSellAdvertisementBinding
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.ItemUserViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.UploadState
import com.seif.booksislandapp.presentation.home.upload_advertisement.UsersBottomSheetFragment
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.OnImageItemClick
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedImagesAdapter
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.MAX_UPLOADED_IMAGES_NUMBER
import com.seif.booksislandapp.utils.Constants.Companion.USER_DISTRICT_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USER_GOVERNORATE_KEY
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*

@AndroidEntryPoint
class UploadSellAdvertisementFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadSellAdvertisementBinding? = null
    private val binding get() = _binding!!

    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val uploadSellAdvertisementViewModel: UploadSellAdvertisementViewModel by viewModels()
    private val itemUserViewModel: ItemUserViewModel by activityViewModels()

    private var categoryName: String = ""
    private var firebaseCurrentUser: FirebaseUser? = null
    private lateinit var requestId: String

    private val args: UploadSellAdvertisementFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUploadSellAdvertisementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        firebaseCurrentUser = uploadSellAdvertisementViewModel.getFirebaseCurrentUser()

        observe()
        observeCategorySelected()
        observeSelectedUserToRequestConfirmation()
        checkForUpdateOrPost()
        checkIsConfirmationMessageSent()

        binding.ivRequestConfirmation.setOnClickListener {
            // open bottom sheet to get users that he chat with
            val bottomSheet = UsersBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "")
        }
        binding.tvCancelRequest.setOnClickListener {
            uploadSellAdvertisementViewModel.cancelRequest(
                requestId,
                AdType.Buying,
                args.mySellAdvertisement!!.id
            )
        }

        binding.ivDeleteMyAd.setOnClickListener {
            showConfirmationDialog()
        }

        binding.ivBackUpload.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadAdvertisementFragment_to_categoryFragment)
        }

        binding.btnUploadImages.setOnClickListener {
            pickPhoto()
        }

        binding.btnSubmit.setOnClickListener {
            val sellAdvertisement = prepareSellAdvertisement()
            if (args.mySellAdvertisement != null)
                uploadSellAdvertisementViewModel.requestUpdateMySellAd(sellAdvertisement)
            else
                uploadSellAdvertisementViewModel.uploadSellAdvertisement(sellAdvertisement)
        }

        handleUi()

        binding.rvUploadedImages.adapter = uploadedImagesAdapter
    }

    private fun checkIsConfirmationMessageSent() {
        args.mySellAdvertisement?.confirmationMessageSent?.let {
            Timber.d("onViewCreated:............. $it")
            if (it) {
                Timber.d("checkIsConfirmationMessageSent: $requestId")
                if (requestId.isEmpty()) {
                    binding.tvCancelRequest.hide()
                    binding.ivRequestConfirmation.hide()
                } else {
                    binding.tvCancelRequest.show()
                    binding.ivRequestConfirmation.hide()
                }
            } else {
                binding.tvCancelRequest.hide()
                binding.ivRequestConfirmation.show()
            }
        }
    }

    private fun enableSentConfirmationMessageButton() {
//        binding.ivRequestConfirmation.apply {
//            enabled()
//            isClickable = true
//            isFocusable = true
//        }
        binding.tvCancelRequest.hide()
        binding.ivRequestConfirmation.show()
    }

    private fun disableSentConfirmationMessageButton() {
//        binding.ivRequestConfirmation.apply {
//            disable()
//            isClickable = false
//            isFocusable = false
//        }
//        binding.ivRequestConfirmation.setColorFilter(
//            ContextCompat.getColor(
//                requireContext(),
//                R.color.gray_medium
//            )
//        )
        binding.ivRequestConfirmation.hide()
        binding.tvCancelRequest.show()
    }

    private fun observeSelectedUserToRequestConfirmation() {
        itemUserViewModel.selectedCategoryItem.observe(viewLifecycleOwner) { user ->
            user?.let {
                // send confirmation request
                uploadSellAdvertisementViewModel.sendRequest(
                    MySentRequest(
                        id = "",
                        senderId = firebaseCurrentUser!!.uid,
                        receiverId = it.id,
                        advertisementId = args.mySellAdvertisement!!.id,
                        username = it.username,
                        avatarImage = it.avatarImage,
                        bookTitle = args.mySellAdvertisement!!.book.title,
                        condition = args.mySellAdvertisement!!.book.condition.toString(),
                        category = args.mySellAdvertisement!!.book.category,
                        adType = AdType.Buying,
                        edition = args.mySellAdvertisement!!.book.edition,
                        status = "Pending"
                    )
                )
            }
        }
    }

    private fun handleUi() {
        if (imageUris.size > 0) {
            binding.rvUploadedImages.show()
            binding.ivUploadImage.hide()
        } else {
            binding.rvUploadedImages.hide()
            binding.ivUploadImage.show()
        }
    }

    private fun observeCategorySelected() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                itemCategoryViewModel.selectedCategoryItem.collect {
                    categoryName = it
                    binding.btnCategory.text = categoryName
                }
            }
        }
    }

    private fun checkForUpdateOrPost() {
        if (args.mySellAdvertisement != null) { // edit
            if (uploadSellAdvertisementViewModel.isFirstTime) {
                uploadSellAdvertisementViewModel.isFirstTime = false
                requestId = args.mySellAdvertisement!!.confirmationRequestId
                args.mySellAdvertisement?.let {
                    imageUris = it.book.images.toCollection(ArrayList())
                    categoryName = it.book.category
                    showMySellAdvertisement(it)
                    binding.btnSubmit.text = getString(R.string.update_post)
                    binding.ivDeleteMyAd.show()
//                    binding.ivRequestConfirmation.show()
//                    binding.tvCancelRequest.hide()
                }
            }
        } else {
            binding.btnSubmit.text = getString(R.string.submit_post)
            binding.ivDeleteMyAd.hide()
            binding.ivRequestConfirmation.hide()
            binding.tvCancelRequest.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        setupConditionDropdown()
        setupEditionDropdown()
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete this book Advertisement?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            uploadSellAdvertisementViewModel.requestDeleteMySellAd(args.mySellAdvertisement!!.id)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showMySellAdvertisement(mySellAdvertisement: SellAdvertisement) {

        uploadedImagesAdapter.updateList(mySellAdvertisement.book.images)
        binding.etTitle.setText(mySellAdvertisement.book.title)
        binding.etAuthor.setText(mySellAdvertisement.book.author)
        binding.etPrice.setText(mySellAdvertisement.price)
        binding.etDescription.setText(mySellAdvertisement.book.description)
        itemCategoryViewModel.selectItem(mySellAdvertisement.book.category)
    }

    private fun pickPhoto() {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                                    imageUris.add(Uri.fromFile(compressedFile))
                                    uploadedImagesAdapter.updateList(imageUris)

                                    dismissLoadingDialog()
                                    binding.rvUploadedImages.show()
                                    binding.ivUploadImage.hide()

                                    if (imageUris.size == MAX_UPLOADED_IMAGES_NUMBER)
                                        disableUploadButton()
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

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            uploadSellAdvertisementViewModel.uploadState.collect {
                when (it) {
                    UploadState.Init -> Unit
                    is UploadState.IsLoading -> handleLoadingState(it.isLoading)
                    is UploadState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is UploadState.ShowError -> handleErrorState(it.message)
                    is UploadState.UploadedSuccessfully -> {
                        binding.root.showSuccessSnackBar("Uploaded Successfully")
                        findNavController().navigateUp()
                    }
                    is UploadState.UpdatedSuccessfully -> {
                        binding.root.showSuccessSnackBar(it.message)
                        findNavController().navigateUp()
                    }
                    is UploadState.DeletedSuccessfully -> {
                        binding.root.showSuccessSnackBar(it.message)
                        findNavController().navigateUp()
                    }
                    is UploadState.SendRequestSuccessfully -> {
                        binding.root.showSuccessSnackBar(getString(R.string.confirmation_sent_suuccessfully))
                        requestId = it.requestId
                        disableSentConfirmationMessageButton()
                    }
                    is UploadState.CancelSentRequestsSuccessfully -> {
                        binding.root.showSuccessSnackBar(it.message)
                        enableSentConfirmationMessageButton()
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

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    private fun prepareSellAdvertisement(): SellAdvertisement {
        val condition: String? =
            when (binding.acCondition.text.toString()) {
                "New" -> "New"
                "Used" -> "Used"
                else -> null
            }
        val id = if (args.mySellAdvertisement == null) "" else args.mySellAdvertisement!!.id
        val date =
            if (args.mySellAdvertisement == null) Date() else args.mySellAdvertisement!!.publishDate
        val status =
            if (args.mySellAdvertisement == null) AdvStatus.Opened else args.mySellAdvertisement!!.status
        val userLocation =
            if (args.mySellAdvertisement == null) getUserLocation() else args.mySellAdvertisement!!.location
        val book = Book(
            id = "",
            images = imageUris,
            title = binding.etTitle.text.toString(),
            author = binding.etAuthor.text.toString(),
            category = categoryName,
            condition = condition,
            description = binding.etDescription.text.toString(),
            edition = binding.acEdition.text.toString()
        )
        return SellAdvertisement(
            id = id,
            ownerId = firebaseCurrentUser?.uid ?: "", // if it's null get it from shared prefernce
            book = book,
            status = status,
            publishDate = date,
            price = binding.etPrice.text.toString(),
            location = userLocation,
            confirmationMessageSent = false
        )
    }

    private fun getUserLocation(): String {
        return "${
        uploadSellAdvertisementViewModel.getFromSP(
            USER_GOVERNORATE_KEY,
            String::class.java
        )
        } - ${
        uploadSellAdvertisementViewModel.getFromSP(
            USER_DISTRICT_KEY,
            String::class.java
        )
        }"
    }

    override fun onRemoveImageItemClick(item: Uri, position: Int, bookOrImage: String) {
        imageUris.removeAt(position)
        uploadedImagesAdapter.updateList(imageUris)
        if (imageUris.size == 0) {
            binding.ivUploadImage.show()
            binding.rvUploadedImages.hide()
        } else if (imageUris.size < MAX_UPLOADED_IMAGES_NUMBER)
            enableUploadButton()
    }

    private fun disableUploadButton() {
        binding.btnUploadImages.apply {
            disable()
            setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_medium))
            setStrokeColorResource(R.color.gray_medium)
            setIconTintResource(R.color.gray_medium)
        }
    }

    private fun enableUploadButton() {
        binding.btnUploadImages.apply {
            enabled()
            setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            setStrokeColorResource(R.color.primary)
            setIconTintResource(R.color.primary)
        }
    }

    private fun setupConditionDropdown() {
        val conditions = resources.getStringArray(R.array.condition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        if (args.mySellAdvertisement != null) { // update scenario
            Timber.d("setupConditionDropdown: ${args.mySellAdvertisement!!.book.condition}")
            binding.acCondition.setText(args.mySellAdvertisement!!.book.condition)
        }
        binding.acCondition.setAdapter(arrayAdapter)
    }

    private fun setupEditionDropdown() {
        val conditions = resources.getStringArray(R.array.edition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        if (args.mySellAdvertisement != null) { // update scenario
            binding.acEdition.setText(args.mySellAdvertisement!!.book.edition)
        }
        binding.acEdition.setAdapter(arrayAdapter)
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog.setView(null)
        // return states to initial values
        itemCategoryViewModel.selectItem(getString(R.string.choose_category))
        itemUserViewModel.selectedUser(null)
        uploadSellAdvertisementViewModel.resetUploadStatus()
    }
}