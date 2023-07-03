package com.seif.booksislandapp.presentation.home.upload_advertisement.donate

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
import com.seif.booksislandapp.databinding.FragmentUploadDonateBinding
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.ItemUserViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.UploadState
import com.seif.booksislandapp.presentation.home.upload_advertisement.UsersBottomSheetFragment
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.OnImageItemClick
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedImagesAdapter
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.FileUtil
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.disable
import com.seif.booksislandapp.utils.enabled
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import com.seif.booksislandapp.utils.showErrorSnackBar
import com.seif.booksislandapp.utils.showSuccessSnackBar
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Date

@AndroidEntryPoint
class UploadDonateFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadDonateBinding? = null
    private val binding get() = _binding!!

    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val uploadDonateAdvertisementViewModel: UploadDonateViewModel by viewModels()
    private val itemUserViewModel: ItemUserViewModel by activityViewModels()

    private var categoryName: String = ""
    private var firebaseCurrentUser: FirebaseUser? = null
    private lateinit var requestId: String

    private val args: UploadDonateFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUploadDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        firebaseCurrentUser = uploadDonateAdvertisementViewModel.getFirebaseCurrentUser()

        observe()
        observeCategorySelected()
        observeSelectedUserToRequestConfirmation()
        checkForUpdateOrPost()
        checkIsConfirmationMessageSent()

        binding.ivBackUpload.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadDonateFragment_to_bookCategoriesFragment)
        }

        binding.btnUploadImages.setOnClickListener {
            pickPhoto()
        }

        binding.ivDeleteMyAd.setOnClickListener {
            showConfirmationDialog()
        }

        binding.ivRequestConfirmation.setOnClickListener {
            // open bottom sheet to get users that he chat with
            val bottomSheet = UsersBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "")
        }

        binding.tvCancelRequest2.setOnClickListener {
            uploadDonateAdvertisementViewModel.cancelRequest(
                requestId,
                AdType.Buying,
                args.myDonateAdvertisement!!.id
            )
        }

        binding.btnSubmit.setOnClickListener {
            val donateAdvertisement = prepareDonateAdvertisement()
            if (args.myDonateAdvertisement != null)
                uploadDonateAdvertisementViewModel.requestUpdateMyDonateAd(donateAdvertisement)
            else
                uploadDonateAdvertisementViewModel.uploadDonateAdvertisement(donateAdvertisement)
        }

        handleUi()

        binding.rvUploadedImages.adapter = uploadedImagesAdapter
    }

    private fun observeCategorySelected() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                itemCategoryViewModel.selectedCategoryItem.collect {
                    Timber.d("collector: $it")
                    categoryName = it
                    binding.btnCategory.text = categoryName
                }
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

    private fun checkIsConfirmationMessageSent() {
        args.myDonateAdvertisement?.confirmationMessageSent?.let {
            Timber.d("onViewCreated:............. $it")
            if (it) {
                Timber.d("checkIsConfirmationMessageSent: $requestId")
                if (requestId.isEmpty()) {
                    binding.tvCancelRequest2.hide()
                    binding.ivRequestConfirmation.hide()
                } else {
                    binding.tvCancelRequest2.show()
                    binding.ivRequestConfirmation.hide()
                }
            } else {
                binding.tvCancelRequest2.hide()
                binding.ivRequestConfirmation.show()
            }
        }
    }

    private fun enableSentConfirmationMessageButton() {
        binding.tvCancelRequest2.hide()
        binding.ivRequestConfirmation.show()
    }

    private fun disableSentConfirmationMessageButton() {
        binding.ivRequestConfirmation.hide()
        binding.tvCancelRequest2.show()
    }

    private fun observeSelectedUserToRequestConfirmation() {
        itemUserViewModel.selectedCategoryItem.observe(viewLifecycleOwner) { user ->
            user?.let {
                // send confirmation request
                uploadDonateAdvertisementViewModel.sendRequest(
                    MySentRequest(
                        id = "",
                        senderId = firebaseCurrentUser!!.uid,
                        receiverId = it.id,
                        advertisementId = args.myDonateAdvertisement!!.id,
                        username = it.username,
                        avatarImage = it.avatarImage,
                        bookTitle = args.myDonateAdvertisement!!.book.title,
                        condition = args.myDonateAdvertisement!!.book.condition.toString(),
                        category = args.myDonateAdvertisement!!.book.category,
                        adType = AdType.Donation,
                        edition = args.myDonateAdvertisement!!.book.edition,
                        status = "Pending"
                    )
                )
            }
        }
    }

    private fun checkForUpdateOrPost() {
        if (args.myDonateAdvertisement != null) { // edit
            if (uploadDonateAdvertisementViewModel.isFirstTime) {
                uploadDonateAdvertisementViewModel.isFirstTime = false
                requestId = args.myDonateAdvertisement!!.confirmationRequestId
                args.myDonateAdvertisement?.let {
                    imageUris = it.book.images.toCollection(ArrayList())
                    categoryName = it.book.category
                    showMyDonateAdvertisement(it)
                    binding.ivDeleteMyAd.show()
                }
            }
            binding.btnSubmit.text = getString(R.string.update_post)
        } else {
            binding.btnSubmit.text = getString(R.string.submit_post)
            binding.ivDeleteMyAd.hide()
            binding.ivRequestConfirmation.hide()
            binding.tvCancelRequest2.hide()
        }
    }

    private fun showMyDonateAdvertisement(myDonateAdvertisement: DonateAdvertisement) {

        uploadedImagesAdapter.updateList(myDonateAdvertisement.book.images)

        binding.etTitle.setText(myDonateAdvertisement.book.title)
        binding.etAuthor.setText(myDonateAdvertisement.book.author)
        // binding.etPrice.setText(myDonateAdvertisement.price)
        binding.etDescription.setText(myDonateAdvertisement.book.description)
        itemCategoryViewModel.selectItem(myDonateAdvertisement.book.category)
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
            uploadDonateAdvertisementViewModel.requestDeleteMyDonateAd(args.myDonateAdvertisement!!.id)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
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
                                withContext(Dispatchers.Main) {
                                    imageUris.add(Uri.fromFile(compressedFile))
                                    uploadedImagesAdapter.updateList(imageUris)

                                    dismissLoadingDialog()
                                    binding.rvUploadedImages.show()
                                    binding.ivUploadImage.hide()

                                    if (imageUris.size == Constants.MAX_UPLOADED_IMAGES_NUMBER)
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
            uploadDonateAdvertisementViewModel.uploadState.collect {
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
                        // Timber.d("observe: send successfully ->> ${it.message}")
                        binding.root.showSuccessSnackBar(getString(R.string.confirmation_sent_successfully))
                        requestId = it.requestId
                        disableSentConfirmationMessageButton()
                    }
                    is UploadState.CancelSentRequestsSuccessfully -> {
                        // Timber.d("observe: send successfully ->> ${it.message}")
                        binding.root.showSuccessSnackBar(it.message)
                        // edit isConfirmationMessageSent to true
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

    private fun prepareDonateAdvertisement(): DonateAdvertisement {
        val condition: String? =
            when (binding.acCondition.text.toString()) {
                "New" -> "New"
                "Used With Good Condition" -> "Used With Good Condition"
                "Used With Bad Condition" -> "Used With Bad Condition"
                else -> null
            }
        val id = if (args.myDonateAdvertisement == null) "" else args.myDonateAdvertisement!!.id
        val date =
            if (args.myDonateAdvertisement == null) Date() else args.myDonateAdvertisement!!.publishDate
        val status =
            if (args.myDonateAdvertisement == null) AdvStatus.Opened else args.myDonateAdvertisement!!.status
        val userLocation =
            if (args.myDonateAdvertisement == null) getUserLocation() else args.myDonateAdvertisement!!.location
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
        return DonateAdvertisement(
            id = id,
            ownerId = firebaseCurrentUser?.uid ?: "",
            book = book,
            status = status,
            publishDate = date,
            location = userLocation,
            confirmationMessageSent = false
        )
    }

    private fun getUserLocation(): String {
        return "${
        uploadDonateAdvertisementViewModel.getFromSP(
            Constants.USER_GOVERNORATE_KEY,
            String::class.java
        )
        } - ${
        uploadDonateAdvertisementViewModel.getFromSP(
            Constants.USER_DISTRICT_KEY,
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
        } else if (imageUris.size < Constants.MAX_UPLOADED_IMAGES_NUMBER)
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
        if (args.myDonateAdvertisement != null) { // update scenario
            Timber.d("setupConditionDropdown: ${args.myDonateAdvertisement!!.book.condition}")
            binding.acCondition.setText(args.myDonateAdvertisement!!.book.condition)
        }
        binding.acCondition.setAdapter(arrayAdapter)
    }

    private fun setupEditionDropdown() {
        val conditions = resources.getStringArray(R.array.edition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        if (args.myDonateAdvertisement != null) { // update scenario
            binding.acEdition.setText(args.myDonateAdvertisement!!.book.edition)
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
        dialog.setView(null)
        binding.rvUploadedImages.adapter = null
        // return states to initial values
        itemUserViewModel.selectedUser(null)
        uploadDonateAdvertisementViewModel.resetUploadStatus()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        itemCategoryViewModel.selectItem(getString(R.string.choose_category))
    }
}