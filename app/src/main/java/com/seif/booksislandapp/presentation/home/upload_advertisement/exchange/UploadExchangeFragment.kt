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
import com.seif.booksislandapp.databinding.FragmentUploadExchangeBinding
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.ItemUserViewModel
import com.seif.booksislandapp.presentation.home.upload_advertisement.UploadState
import com.seif.booksislandapp.presentation.home.upload_advertisement.UsersBottomSheetFragment
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.OnImageItemClick
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedBooksForExchangeAdapter
import com.seif.booksislandapp.presentation.home.upload_advertisement.adapter.UploadedImagesAdapter
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.MAX_UPLOADED_EXCHANGE_FOR_IMAGES_NUMBER
import com.seif.booksislandapp.utils.FileUtil
import com.seif.booksislandapp.utils.createLoadingAlertDialog
import com.seif.booksislandapp.utils.disable
import com.seif.booksislandapp.utils.enabled
import com.seif.booksislandapp.utils.handleNoInternetConnectionState
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.invisible
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
class UploadExchangeFragment : Fragment(), OnImageItemClick<Uri> {
    private var _binding: FragmentUploadExchangeBinding? = null
    private val binding get() = _binding!!

    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var dialog: AlertDialog

    private val uploadedImagesAdapter by lazy { UploadedImagesAdapter() }
    private val uploadedExchangeAdapter by lazy { UploadedBooksForExchangeAdapter() }

    private val taskViewModel: ExchangeViewModel by activityViewModels()
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val uploadExchangeAdvertisementViewModel: UploadExchangeViewModel by viewModels()
    private val itemUserViewModel: ItemUserViewModel by activityViewModels()

    private var categoryName: String = ""
    private var firebaseCurrentUser: FirebaseUser? = null
    private var exchangeFor: ArrayList<BooksToExchange> = ArrayList()
    private lateinit var requestId: String

    private val args: UploadExchangeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentUploadExchangeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        uploadedImagesAdapter.onImageItemClick = this
        uploadedExchangeAdapter.onImageItemClick = this
        firebaseCurrentUser = uploadExchangeAdvertisementViewModel.getFirebaseCurrentUser()

        observe()
        observeBooksToExchange()
        observeSelectedCategoryItem()
        observeSelectedUserToRequestConfirmation()
        checkForUpdateOrPost()
        checkIsConfirmationMessageSent()

        binding.ivRequestConfirmation.setOnClickListener {
            // open bottom sheet to get users that he chat with
            val bottomSheet = UsersBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "")
        }

        binding.tvCancelRequest.setOnClickListener {
            uploadExchangeAdvertisementViewModel.cancelRequest(
                requestId,
                AdType.Buying,
                args.exchangeAdvertisement!!.id
            )
        }

        binding.ivDeleteMyExchangeAd.setOnClickListener {
            showConfirmationDialog()
        }

        binding.ivBackUpload.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_uploadExchangeFragment_to_bookCategoriesFragment)
        }

        binding.btnUploadImages.setOnClickListener {
            pickPhoto()
        }

        binding.btnUploadBook.setOnClickListener {
            ExchangeSheetFragment().show(parentFragmentManager, " ")
        }

        binding.btnUpload.setOnClickListener {
            val exchangeAdvertisement = prepareExchangeAdvertisement()
            if (args.exchangeAdvertisement != null)
                uploadExchangeAdvertisementViewModel.requestUpdateMyExchangeAd(exchangeAdvertisement)
            else
                uploadExchangeAdvertisementViewModel.uploadExchangeAdvertisement(
                    exchangeAdvertisement
                )
        }

        handleUploadImagesViews()
        handleUploadBooksViews()

        binding.rvUploadedImages.adapter = uploadedImagesAdapter
        binding.rvUploadedBook.adapter = uploadedExchangeAdapter
    }

    private fun checkIsConfirmationMessageSent() {
        args.exchangeAdvertisement?.confirmationMessageSent?.let {
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
        binding.tvCancelRequest.hide()
        binding.ivRequestConfirmation.show()
    }

    private fun disableSentConfirmationMessageButton() {
        binding.ivRequestConfirmation.hide()
        binding.tvCancelRequest.show()
    }

    private fun observeSelectedUserToRequestConfirmation() {
        itemUserViewModel.selectedCategoryItem.observe(viewLifecycleOwner) { user ->
            user?.let {
                // send confirmation request
                uploadExchangeAdvertisementViewModel.sendRequest(
                    MySentRequest(
                        id = "",
                        senderId = firebaseCurrentUser!!.uid,
                        receiverId = it.id,
                        advertisementId = args.exchangeAdvertisement!!.id,
                        username = it.username,
                        avatarImage = it.avatarImage,
                        bookTitle = args.exchangeAdvertisement!!.book.title,
                        condition = args.exchangeAdvertisement!!.book.condition.toString(),
                        category = args.exchangeAdvertisement!!.book.category,
                        adType = AdType.Exchange,
                        edition = args.exchangeAdvertisement!!.book.edition,
                        status = "Pending"
                    )
                )
            }
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to delete this book Advertisement?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            uploadExchangeAdvertisementViewModel.requestDeleteMyExchangeAd(args.exchangeAdvertisement!!.id)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkForUpdateOrPost() {
        if (args.exchangeAdvertisement != null) { // edit
            if (uploadExchangeAdvertisementViewModel.isFirstTime) {
                uploadExchangeAdvertisementViewModel.isFirstTime = false
                requestId = args.exchangeAdvertisement!!.confirmationRequestId
                args.exchangeAdvertisement?.let {
                    imageUris = it.book.images.toCollection(ArrayList())
                    exchangeFor = it.booksToExchange.toCollection(ArrayList())
                    categoryName = it.book.category
                    showMyDonateAdvertisement(it)
                    binding.ivDeleteMyExchangeAd.show()
                }
            }
            binding.btnUpload.text = getString(R.string.update_post)
        } else {
            binding.btnUpload.text = getString(R.string.submit_post)
            binding.ivDeleteMyExchangeAd.hide()
            binding.ivRequestConfirmation.hide()
            binding.tvCancelRequest.hide()
        }
    }

    private fun showMyDonateAdvertisement(myExchangeAdvertisement: ExchangeAdvertisement) {

        uploadedImagesAdapter.updateList(myExchangeAdvertisement.book.images)
        uploadedExchangeAdapter.updateList(myExchangeAdvertisement.booksToExchange)
        binding.etTitle.setText(myExchangeAdvertisement.book.title)
        binding.etAuthor.setText(myExchangeAdvertisement.book.author)
        binding.etDescription.setText(myExchangeAdvertisement.book.description)
        itemCategoryViewModel.selectItem(myExchangeAdvertisement.book.category)
    }

    private fun observeSelectedCategoryItem() {
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

    private fun observeBooksToExchange() {
        taskViewModel.liveData.observe(viewLifecycleOwner) {
            it?.let {
                exchangeFor.add(it)
                uploadedExchangeAdapter.updateList(exchangeFor)

                if (exchangeFor.size in 1 until MAX_UPLOADED_EXCHANGE_FOR_IMAGES_NUMBER) {
                    binding.rvUploadedBook.show()
                    binding.ivUploadBook.invisible()
                } else if (exchangeFor.size == MAX_UPLOADED_EXCHANGE_FOR_IMAGES_NUMBER) {
                    binding.btnUploadBook.disable()
                } else { // never enter this condition
                    binding.rvUploadedBook.invisible()
                    binding.ivUploadBook.show()
                }
            }
        }
    }

    private fun handleUploadBooksViews() {
        if (exchangeFor.isEmpty()) {
            binding.rvUploadedBook.hide()
            binding.ivUploadBook.show()
        } else {
            binding.rvUploadedBook.show()
            binding.ivUploadBook.hide()
        }
    }

    private fun handleUploadImagesViews() {
        if (imageUris.isEmpty()) {
            binding.rvUploadedImages.hide()
            binding.ivUploadImage.show()
        } else {
            binding.rvUploadedImages.show()
            binding.ivUploadImage.hide()
        }
    }

    override fun onResume() {
        super.onResume()
        setupConditionDropdown()
        setupEditionDropdown()
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
            uploadExchangeAdvertisementViewModel.uploadState.collect {
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
                        // Timber.d("observe: send successfully ->> ${it.message}")
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

    private fun prepareExchangeAdvertisement(): ExchangeAdvertisement {
        val condition: String? =
            when (binding.acCondition.text.toString()) {
                "New" -> "New"
                "Used" -> "Used"
                else -> null
            }
        val id = if (args.exchangeAdvertisement == null) "" else args.exchangeAdvertisement!!.id
        val date =
            if (args.exchangeAdvertisement == null) Date() else args.exchangeAdvertisement!!.publishDate
        val status =
            if (args.exchangeAdvertisement == null) AdvStatus.Opened else args.exchangeAdvertisement!!.status
        val userLocation =
            if (args.exchangeAdvertisement == null) getUserLocation() else args.exchangeAdvertisement!!.location
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
        return ExchangeAdvertisement(
            id = id,
            ownerId = firebaseCurrentUser?.uid ?: "",
            book = book,
            status = status,
            publishDate = date,
            location = userLocation,
            booksToExchange = exchangeFor,
            confirmationMessageSent = false

        )
    }

    private fun getUserLocation(): String {
        return "${
        uploadExchangeAdvertisementViewModel.getFromSP(
            Constants.USER_GOVERNORATE_KEY,
            String::class.java
        )
        } - ${
        uploadExchangeAdvertisementViewModel.getFromSP(
            Constants.USER_DISTRICT_KEY,
            String::class.java
        )
        }"
    }

    override fun onRemoveImageItemClick(item: Uri, position: Int, bookOrImage: String) {
        Timber.d("onRemoveImageItemClick: clicked $bookOrImage")
        if (bookOrImage == "Image") {
            imageUris.removeAt(position)
            uploadedImagesAdapter.updateList(imageUris)
            if (imageUris.size == 0) {
                binding.ivUploadImage.show()
                binding.rvUploadedImages.hide()
            } else if (imageUris.size < Constants.MAX_UPLOADED_IMAGES_NUMBER)
                enableUploadButton()
        } else if (bookOrImage == "Book") {
            exchangeFor.removeAt(position)
            uploadedExchangeAdapter.updateList(exchangeFor)
            if (exchangeFor.size == 0) {
                binding.ivUploadBook.show()
                binding.rvUploadedBook.invisible()
            } else if (exchangeFor.size < Constants.MAX_UPLOADED_IMAGES_NUMBER)
                binding.btnUploadBook.enabled()
        }
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
        if (args.exchangeAdvertisement != null) { // update scenario
            binding.acCondition.setText(args.exchangeAdvertisement!!.book.condition)
        }
        binding.acCondition.setAdapter(arrayAdapter)
    }

    private fun setupEditionDropdown() {
        val conditions = resources.getStringArray(R.array.edition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        if (args.exchangeAdvertisement != null) { // update scenario
            binding.acEdition.setText(args.exchangeAdvertisement!!.book.edition)
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
        taskViewModel.resetBooksToExchangeLiveData()
        binding.rvUploadedBook.adapter = null
        binding.rvUploadedImages.adapter = null
        // return states to initial values
        itemUserViewModel.selectedUser(null)
        uploadExchangeAdvertisementViewModel.resetUploadStatus()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        itemCategoryViewModel.selectItem(getString(R.string.choose_category))
    }
}