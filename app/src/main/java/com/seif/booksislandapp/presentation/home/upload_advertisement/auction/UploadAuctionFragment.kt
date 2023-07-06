package com.seif.booksislandapp.presentation.home.upload_advertisement.auction

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
import com.seif.booksislandapp.databinding.FragmentUploadAuctionBinding
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
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
class UploadAuctionFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadAuctionBinding? = null
    private val binding get() = _binding!!

    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val uploadAuctionAdvertisementViewModel: UploadAuctionViewModel by viewModels()
    private val itemUserViewModel: ItemUserViewModel by activityViewModels()

    private var categoryName: String = ""
    private var firebaseCurrentUser: FirebaseUser? = null
    private lateinit var requestId: String

    private val args: UploadAuctionFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadAuctionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        firebaseCurrentUser = uploadAuctionAdvertisementViewModel.getFirebaseCurrentUser()

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
            uploadAuctionAdvertisementViewModel.cancelRequest(
                requestId,
                AdType.Auction,
                args.auctionAdvertisement!!.id
            )
        }

        binding.ivBackUpload.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadAuctionFragment_to_bookCategoriesFragment)
        }

        binding.btnUploadImages.setOnClickListener {
            pickPhoto()
        }

        binding.ivDeleteMyAuctionAd.setOnClickListener {
            showConfirmationDialog()
        }

        binding.ivBiddersHistory.setOnClickListener {
            val action =
                UploadAuctionFragmentDirections.actionUploadAuctionFragmentToBiddersHistoryFragment(
                    args.auctionAdvertisement!!.id
                )
            findNavController().navigate(action)
        }

        binding.btnSubmit.setOnClickListener {
            val auctionAdvertisement = prepareAuctionAdvertisement()
            if (args.auctionAdvertisement != null)
                uploadAuctionAdvertisementViewModel.requestUpdateMyAuctionAd(auctionAdvertisement)
            else
                uploadAuctionAdvertisementViewModel.uploadAuctionAdvertisement(auctionAdvertisement)
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
        args.auctionAdvertisement?.confirmationMessageSent?.let {
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
        binding.ivRequestConfirmation.apply {
            binding.tvCancelRequest.hide()
            binding.ivRequestConfirmation.show()
        }
    }

    private fun disableSentConfirmationMessageButton() {
        binding.ivRequestConfirmation.hide()
        binding.tvCancelRequest.show()
    }

    private fun observeSelectedUserToRequestConfirmation() {
        itemUserViewModel.selectedCategoryItem.observe(viewLifecycleOwner) { user ->
            user?.let {
                // send confirmation request
                uploadAuctionAdvertisementViewModel.sendRequest(
                    MySentRequest(
                        id = "",
                        senderId = firebaseCurrentUser!!.uid,
                        receiverId = it.id,
                        advertisementId = args.auctionAdvertisement!!.id,
                        username = it.username,
                        avatarImage = it.avatarImage,
                        bookTitle = args.auctionAdvertisement!!.book.title,
                        condition = args.auctionAdvertisement!!.book.condition.toString(),
                        category = args.auctionAdvertisement!!.book.category,
                        adType = AdType.Auction,
                        edition = args.auctionAdvertisement!!.book.edition,
                        status = "Pending"
                    )
                )
            }
        }
    }

    private fun checkForUpdateOrPost() {
        if (args.auctionAdvertisement != null) { // edit
            if (uploadAuctionAdvertisementViewModel.isFirstTime) {
                uploadAuctionAdvertisementViewModel.isFirstTime = false
                requestId = args.auctionAdvertisement!!.confirmationRequestId
                args.auctionAdvertisement?.let {
                    imageUris = it.book.images.toCollection(ArrayList())
                    categoryName = it.book.category
                    showMyAuctionAdvertisement(it)
                    binding.ivDeleteMyAuctionAd.show()
                    binding.tvCancelRequest.hide()
                    binding.ivBiddersHistory.show()
                }
            }
            binding.btnSubmit.text = getString(R.string.update_post)
        } else {
            binding.btnSubmit.text = getString(R.string.submit_post)
            binding.ivDeleteMyAuctionAd.hide()
            binding.ivRequestConfirmation.hide()
            binding.ivBiddersHistory.hide()
        }
    }

    private fun showMyAuctionAdvertisement(myAuctionAdvertisement: AuctionAdvertisement) {

        uploadedImagesAdapter.updateList(myAuctionAdvertisement.book.images)

        binding.etTitle.setText(myAuctionAdvertisement.book.title)
        binding.etAuthor.setText(myAuctionAdvertisement.book.author)
        binding.etStartPrice.setText(myAuctionAdvertisement.startPrice.toString())
        binding.tilStartPrice.disable()
        binding.etStartPrice.disable()
        binding.etDescription.setText(myAuctionAdvertisement.book.description)
        itemCategoryViewModel.selectItem(myAuctionAdvertisement.book.category)
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete this book Advertisement?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            uploadAuctionAdvertisementViewModel.requestDeleteMyAuctionAd(args.auctionAdvertisement!!.id)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        setupConditionDropdown()
        setupEditionDropdown()
        setupPostDurationDropdown()
    }

    private fun setupPostDurationDropdown() {
        val conditions = resources.getStringArray(R.array.post_duration)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        if (args.auctionAdvertisement != null) { // update scenario
            binding.acPostDuration.setText(args.auctionAdvertisement!!.postDuration)
        }
        binding.acPostDuration.setAdapter(arrayAdapter)
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
                                    if (imageUris.size >= Constants.MAX_UPLOADED_IMAGES_NUMBER)
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
            uploadAuctionAdvertisementViewModel.uploadState.collect {
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
                        binding.root.showSuccessSnackBar(getString(R.string.confirmation_sent_successfully))
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

    private fun prepareAuctionAdvertisement(): AuctionAdvertisement { // need to ber refactor and enhance
        val condition: String? =
            when (binding.acCondition.text.toString()) {
                "New" -> "New"
                "Used With Good Condition" -> "Used With Good Condition"
                "Used With Bad Condition" -> "Used With Bad Condition"
                else -> null
            }
        val id = if (args.auctionAdvertisement == null) "" else args.auctionAdvertisement!!.id

        val date =
            if (args.auctionAdvertisement == null) Date() else args.auctionAdvertisement!!.publishDate
        val status =
            if (args.auctionAdvertisement == null) AdvStatus.Opened else args.auctionAdvertisement!!.status
        val auctionStatus =
            if (args.auctionAdvertisement == null) AuctionStatus.STARTED else args.auctionAdvertisement!!.auctionStatus
        val userLocation =
            if (args.auctionAdvertisement == null) getUserLocation() else args.auctionAdvertisement!!.location
        val bidders =
            if (args.auctionAdvertisement == null) emptyList() else args.auctionAdvertisement!!.bidders
        val closeDate =
            if (args.auctionAdvertisement == null) null else args.auctionAdvertisement!!.closeDate
        val endPrice =
            if (args.auctionAdvertisement == null) null else args.auctionAdvertisement!!.endPrice
        val startPrice =
            if (args.auctionAdvertisement == null) binding.etStartPrice.text.toString()
                .toDouble() else args.auctionAdvertisement!!.startPrice
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
        return AuctionAdvertisement(
            id = id,
            ownerId = firebaseCurrentUser?.uid ?: "",
            book = book,
            status = status,
            publishDate = date,
            startPrice = startPrice,
            location = userLocation,
            endPrice = endPrice,
            closeDate = closeDate,
            postDuration = binding.acPostDuration.text.toString(),
            auctionStatus = auctionStatus,
            bidders = bidders,
            confirmationMessageSent = false
        )
    }

    private fun getUserLocation(): String {
        return "${
        uploadAuctionAdvertisementViewModel.getFromSP(
            Constants.USER_GOVERNORATE_KEY,
            String::class.java
        )
        } - ${
        uploadAuctionAdvertisementViewModel.getFromSP(
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
        if (args.auctionAdvertisement != null) { // update scenario
            Timber.d("setupConditionDropdown: ${args.auctionAdvertisement!!.book.condition}")
            binding.acCondition.setText(args.auctionAdvertisement!!.book.condition)
        }
        binding.acCondition.setAdapter(arrayAdapter)
    }

    private fun setupEditionDropdown() {
        val conditions = resources.getStringArray(R.array.edition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        if (args.auctionAdvertisement != null) { // update scenario
            binding.acEdition.setText(args.auctionAdvertisement!!.book.edition)
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
        itemUserViewModel.selectedUser(null)
        uploadAuctionAdvertisementViewModel.resetUploadStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        itemCategoryViewModel.selectItem(getString(R.string.choose_category))
    }
}