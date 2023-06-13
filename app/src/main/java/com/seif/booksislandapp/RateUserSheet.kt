package com.seif.booksislandapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentRateUserSheetBinding

class RateUserSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentRateUserSheetBinding
    private lateinit var rateViewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        rateViewModel = ViewModelProvider(activity)[ViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRateUserSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
}