package com.seif.booksislandapp.presentation.home.categories.buy

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentSortSheetBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider

class SortSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSortSheetBinding
    private lateinit var sortViewModel: SortViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        sortViewModel = ViewModelProvider(activity)[SortViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSortSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
}