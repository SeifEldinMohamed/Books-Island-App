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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentUploadAuctionBinding
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.UploadState
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.OnImageItemClick
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedImagesAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*

@AndroidEntryPoint
class UploadAuctionFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadAuctionBinding? = null
    private val binding get() = _binding!!

    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val uploadAuctionAdvertisementViewModel: UploadAuctionViewModel by viewModels()

    private var categoryName: String = ""
    private var firebaseCurrentUser: FirebaseUser? = null
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
        setupConditionDropdown()
        setupEditionDropdown()
        setupPostDurationDropdown()
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        firebaseCurrentUser = uploadAuctionAdvertisementViewModel.getFirebaseCurrentUser()
        observe()

        binding.ivBackUpload.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadAuctionFragment_to_bookCategoriesFragment)
        }

        binding.btnUploadImages.setOnClickListener {
            pickPhoto()
        }

        itemCategoryViewModel.selectedCategoryItem.observe(viewLifecycleOwner) { name ->
            name?.let {
                Timber.d("onViewCreated: $it")
                categoryName = it
                binding.btnCategory.text = categoryName
            }
        }

        binding.btnSubmit.setOnClickListener {
            val auctionAdvertisement = prepareAuctionAdvertisement()
            uploadAuctionAdvertisementViewModel.uploadAuctionAdvertisement(auctionAdvertisement)
        }

        if (imageUris.size > 0) {
            binding.rvUploadedImages.show()
            binding.ivUploadImage.hide()
        } else {
            binding.rvUploadedImages.hide()
            binding.ivUploadImage.show()
        }

        binding.rvUploadedImages.adapter = uploadedImagesAdapter
    }

    private fun setupPostDurationDropdown() {
        val conditions = resources.getStringArray(R.array.post_duration)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
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

    private fun prepareAuctionAdvertisement(): AuctionAdvertisement {
        val isUsed: Boolean? =
            when (binding.acCondition.text.toString()) {
                "New" -> false
                "Used" -> true
                else -> null
            }
        val book = Book(
            id = "",
            images = imageUris,
            title = binding.etTitle.text.toString(),
            author = binding.etAuthor.text.toString(),
            category = categoryName,
            isUsed = isUsed,
            description = binding.etDescription.text.toString(),
            edition = binding.acEdition.text.toString()
        )
        return AuctionAdvertisement(
            id = "",
            ownerId = firebaseCurrentUser?.uid ?: "",
            book = book,
            status = AdvStatus.Opened,
            publishDate = Date(),
            startPrice = binding.etStartPrice.text.toString().toDouble(),
            location = getUserLocation(),
            endPrice = null,
            closeDate = null,
            postDuration = binding.acPostDuration.text.toString(),
            auctionStatus = AuctionStatus.STARTED,
            bidders = emptyList()
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

    override fun onRemoveImageItemClick(item: Uri, position: Int) {
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
        binding.acCondition.setAdapter(arrayAdapter)
    }

    private fun setupEditionDropdown() {
        val conditions = resources.getStringArray(R.array.edition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
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
        uploadAuctionAdvertisementViewModel.resetUploadStatus()
    }
}