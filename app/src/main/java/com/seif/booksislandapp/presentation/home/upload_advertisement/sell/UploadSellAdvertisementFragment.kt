package com.seif.booksislandapp.presentation.home.upload_advertisement.sell

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentUploadSellAdvertisementBinding
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.OnImageItemClick
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import com.google.firebase.auth.FirebaseUser
import com.musfickjamil.snackify.Snackify
import com.seif.booksislandapp.domain.model.AdvStatus
import com.seif.booksislandapp.domain.model.Book
import com.seif.booksislandapp.domain.model.BookCondition
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.MAX_UPLOADED_IMAGES_NUMBER
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class UploadSellAdvertisementFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadSellAdvertisementBinding? = null
    private val binding get() = _binding!!

    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val uploadSellAdvertisementViewModel: UploadSellAdvertisementViewModel by viewModels()

    private var categoryName: String = ""
    private var firebaseCurrentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUploadSellAdvertisementBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupConditionDropdown()
        setupEditionDropdown()
        dialog = requireContext().createAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        firebaseCurrentUser = uploadSellAdvertisementViewModel.getFirebaseCurrentUser()
        observe()

        binding.ivBackUpload.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadAdvertisementFragment_to_categoryFragment)
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
            val sellAdvertisement = prepareSellAdvertisement()
            uploadSellAdvertisementViewModel.uploadSellAdvertisement(sellAdvertisement)
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
                            lifecycleScope.launch(Dispatchers.IO) {
                                compressedFile = FileUtil.from(requireContext(), uri)?.let { it1 ->
                                    Compressor.compress(
                                        requireContext(),
                                        it1
                                    )
                                }
                                imageUris.add(Uri.fromFile(compressedFile))
                                Timber.d("upload: $imageUris")
                                //  uploadSellAdvertisementViewModel.addImagesUris(imageUris)
                                withContext(Dispatchers.Main) {
                                    uploadedImagesAdapter.updateList(imageUris)
                                }
                            }
                        }
                    }

                    dismissLoadingDialog()
                    binding.rvUploadedImages.show()
                    binding.ivUploadImage.hide()

                    if (imageUris.size == MAX_UPLOADED_IMAGES_NUMBER)
                        disableUploadButton()
                }
                else -> {
                    dismissLoadingDialog()
                    Timber.d("Task Cancelled")
                }
            }
        }

    private fun observe() {
        lifecycleScope.launch {
            uploadSellAdvertisementViewModel.uploadState.collect {
                when (it) {
                    UploadState.Init -> Unit
                    is UploadState.IsLoading -> handleLoadingState(it.isLoading)
                    is UploadState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is UploadState.ShowError -> handleErrorState(it.message)
                    is UploadState.UploadedSuccessfully -> {
                        Snackify.success(
                            binding.root,
                            "Uploaded Successfully",
                            Snackify.LENGTH_SHORT
                        ).show()
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
        // binding.root.showSnackBar(message)
        Snackify.error(binding.root, message, Snackify.LENGTH_SHORT).show()
    }

    private fun prepareSellAdvertisement(): SellAdvertisement {
        val bookCondition: BookCondition? =
            when (binding.acCondition.text.toString()) {
                "New" -> BookCondition.New
                "Used" -> BookCondition.Used
                else -> null
            }
        val book = Book(
            id = "",
            images = imageUris,
            title = binding.etTitle.text.toString(),
            author = binding.etAuthor.text.toString(),
            category = categoryName,
            condition = bookCondition,
            description = binding.etDescription.text.toString()
        )
        return SellAdvertisement(
            id = "",
            ownerId = firebaseCurrentUser?.uid ?: "",
            book = book,
            status = AdvStatus.Opened,
            publishTime = Date(),
            price = binding.etPrice.text.toString(),
            location = "Cairo - Egypt"
        )
    }

    override fun onRemoveImageItemClick(item: Uri, position: Int) {
        imageUris.removeAt(position)
        uploadedImagesAdapter.updateList(imageUris)
        if (imageUris.size == 0) {
            binding.ivUploadImage.show()
            binding.rvUploadedImages.hide()
        } else if (imageUris.size < MAX_UPLOADED_IMAGES_NUMBER)
            enableUploadButton()
    }

    private fun disableUploadButton() {
        binding.btnUploadImages.disable()
        binding.btnUploadImages.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.gray_medium
            )
        )
        binding.btnUploadImages.setStrokeColorResource(R.color.gray_medium)
        binding.btnUploadImages.setIconTintResource(R.color.gray_medium)
    }

    private fun enableUploadButton() {
        binding.btnUploadImages.enabled()
        binding.btnUploadImages.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary
            )
        )
        binding.btnUploadImages.setStrokeColorResource(R.color.primary)
        binding.btnUploadImages.setIconTintResource(R.color.primary)
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
        uploadSellAdvertisementViewModel.resetUploadStatus()
    }
}