package com.seif.booksislandapp.presentation.home.categories.buy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentFilterSheetBinding

class FilterSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterSheetBinding
    private lateinit var filterViewModel: FilterViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        filterViewModel = ViewModelProvider(activity)[FilterViewModel::class.java]
        binding.btnApply.setOnClickListener {
            saveAction()
        }
    }

    private fun saveAction() {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
}