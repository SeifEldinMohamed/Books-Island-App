package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentExchangeSheetBinding

class ExchangeSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentExchangeSheetBinding
    private lateinit var exchangeViewModel: ExchangeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        exchangeViewModel = ViewModelProvider(activity)[ExchangeViewModel::class.java]
        binding.btnAddBook.setOnClickListener {
            saveAction()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExchangeSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun saveAction() {
        exchangeViewModel.auther.value = binding.tvAuthor.text.toString()
        exchangeViewModel.name.value = binding.tvTitle.text.toString()
        // exchangeViewModel.image.value = binding.ivUploadImage.drawable(exchangeViewModel)
        binding.tvAuthor.text = ""
        binding.tvTitle.text = " "
        // binding.ivUploadImage.setImageResource(0)
        dismiss()
    }
}