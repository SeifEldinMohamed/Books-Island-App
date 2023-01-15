package com.seif.booksislandapp.presentation.home.upload_advertisement.sell

import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentUploadSellAdvertisementBinding
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.OnImageItemClick
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import com.github.dhaval2404.imagepicker.ImagePicker
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.MAX_UPLOADED_IMAGES_NUMBER
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UploadSellAdvertisementFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadSellAdvertisementBinding? = null
    private val binding get() = _binding!!

    private var imageUris: MutableList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog
    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()

    private var categoryName: String? = null

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

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadAdvertisementFragment_to_categoryFragment)
        }

        binding.btnUploadImages.setOnClickListener {
            startImagePicker()
        }

        itemCategoryViewModel.selectedCategoryItem.observe(viewLifecycleOwner) { name ->
            name?.let {
                Timber.d("onViewCreated: $it")
                categoryName = it
                binding.btnCategory.text = categoryName
            }
        }

        binding.rvUploadedImages.adapter = uploadedImagesAdapter
    }

    private fun startImagePicker() {
        ImagePicker.with(this)
            .crop()
            .compress(1024) // Final image size will be less than 1 MB
            .galleryOnly() // we use gallery only bec the camera option makes memory leak ( app size = 219 LOL)
            .createIntent { intent ->
                startLoadingDialog()
                startForProfileImageResult.launch(intent)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val resultCode = activityResult.resultCode
            val data = activityResult.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val imageUri = data?.data!!
                    imageUris.add(imageUri)
                    uploadedImagesAdapter.updateList(imageUris)

                    dismissLoadingDialog()
                    binding.rvUploadedImages.show()
                    binding.ivUploadImage.hide()

                    if (imageUris.size == MAX_UPLOADED_IMAGES_NUMBER)
                        disableUploadButton()
                }
                ImagePicker.RESULT_ERROR -> {
                    dismissLoadingDialog()
                    binding.root.showSnackBar(ImagePicker.getError(data))
                }
                else -> {
                    dismissLoadingDialog()
                    Timber.d("Task Cancelled")
                }
            }
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

    override fun onRemoveImageItemClick(item: Uri, position: Int) {
        Timber.d(" size: ${imageUris.size}")
        Timber.d("onRemoveImageItemClick: size: $position")
        imageUris.removeAt(position)
        uploadedImagesAdapter.updateList(imageUris)
        if (imageUris.size == 0)
            binding.ivUploadImage.show()
        else if (imageUris.size < MAX_UPLOADED_IMAGES_NUMBER)
            enableUploadButton()
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
        itemCategoryViewModel.selectItem(getString(R.string.choose_category))
    }
}