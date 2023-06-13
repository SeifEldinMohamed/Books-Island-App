package com.seif.booksislandapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentBlockUserSheetBinding

class BlockUserSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBlockUserSheetBinding
    private lateinit var blockViewModel: BlockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        blockViewModel = ViewModelProvider(activity)[BlockViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBlockUserSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
}