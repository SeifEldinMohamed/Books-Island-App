package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.rate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentRateUserSheetBinding
import com.seif.booksislandapp.presentation.home.ad_provider_profile.AdProviderProfileViewModel

class RateUserBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentRateUserSheetBinding
    private lateinit var rateAdProviderProfileViewModel: AdProviderProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        rateAdProviderProfileViewModel =
            ViewModelProvider(activity)[AdProviderProfileViewModel::class.java]
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