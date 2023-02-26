package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.musfickjamil.snackify.Snackify
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentExchangeSheetBinding
import com.seif.booksislandapp.domain.model.book.BooksToExchange
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

@AndroidEntryPoint
class ExchangeSheetFragment : BottomSheetDialogFragment(), OnImageItemClick<Uri> {

    private lateinit var binding: FragmentExchangeSheetBinding
    private val exchangeViewModel: ExchangeViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private var imageUri: Uri? = null
    private val uris: ArrayList<Uri> = arrayListOf()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        observe()
        binding.btnAddBook.setOnClickListener {
            val item = BooksToExchange(
                imageUri = imageUri,
                title = binding.etTitle.text.toString(),
                author = binding.etAuther.text.toString()
            )
            saveAction(item)
        }
        binding.btnUploadImages.setOnClickListener {
            pickPhoto()
        }

        binding.rvUploadedImages.adapter = uploadedImagesAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExchangeSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            exchangeViewModel.uploadState.collect {
                when (it) {
                    ExchangeSheetState.Init -> Unit
                    is ExchangeSheetState.IsLoading -> {
                        handleLoadingState(it.isLoading)
                    }
                    is ExchangeSheetState.ShowError -> {
                        Snackify.error(binding.root, it.message, Snackify.LENGTH_SHORT)
                            .setAnchorView(binding.root).show()
                    }
                }
            }
        }
    }

    private fun saveAction(booksToExchangeItem: BooksToExchange) {
        exchangeViewModel.addBook(booksToExchangeItem)
        dismiss()
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
                                    imageUri = Uri.fromFile(compressedFile)

                                    uris.add(imageUri!!)
                                    uploadedImagesAdapter.updateList(uris)

                                    dismissLoadingDialog()
                                    // binding.rvUploadedImages.show()
                                    //  binding.ivUploadImage.hide()
                                    if (uris.size > 0) {
                                        binding.rvUploadedImages.show()
                                        binding.ivUploadImage.hide()
                                        binding.btnUploadImages.hide()
                                    } else {
                                        binding.rvUploadedImages.hide()
                                        binding.ivUploadImage.show()
                                        binding.btnUploadImages.show()
                                    }
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

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    private fun disableUploadButton() {
        binding.btnUploadImages.apply {
            disable()
            setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_medium))
            setStrokeColorResource(R.color.gray_medium)
            setIconTintResource(R.color.gray_medium)
        }
    }

    override fun onRemoveImageItemClick(item: Uri, position: Int, bookOrImage: String) {
        uris.removeAt(position)
        uploadedImagesAdapter.updateList(uris)
        if (uris.size > 0) {
            binding.rvUploadedImages.show()
            binding.ivUploadImage.hide()
            binding.btnUploadImages.hide()
        } else {
            binding.rvUploadedImages.hide()
            binding.ivUploadImage.show()
            binding.btnUploadImages.show()
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        Timber.d("handleLoadingState: loading $isLoading")
        when (isLoading) {
            true -> {
                startLoadingDialog()
            }
            false -> dismissLoadingDialog()
        }
    }
}