package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.seif.booksislandapp.databinding.FragmentUploadExchangeBinding

class UploadExchangeFragment : Fragment() {
    private lateinit var binding: FragmentUploadExchangeBinding
    private lateinit var taskViewModel: ExchangeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUploadExchangeBinding.inflate(layoutInflater)

        taskViewModel = ViewModelProvider(this)[ExchangeViewModel::class.java]
        binding.btnUploadBook.setOnClickListener {
            ExchangeSheet().show(parentFragmentManager, " ")
        }
        /*taskViewModel.image.observe(viewLifecycleOwner){
            binding.ivUploadImage.show()
        }*/
        taskViewModel.name.observe(viewLifecycleOwner) {
            binding.tvTitle.text = String.format("", it)
        }
        taskViewModel.auther.observe(viewLifecycleOwner) {
            binding.tvAuthor.text = String.format("Auther: %s", it)
        }

        return binding.root
    }
}