package com.seif.booksislandapp.presentation.home.categories.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.booksislandapp.databinding.FragmentBuyBottomShetBinding

class BuyBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBuyBottomShetBinding
    private val sortViewModel: SortViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLastCheck()
        binding.idRadioGroup.setOnCheckedChangeListener { p0, selectedId ->
            val radioButton = view.findViewById(selectedId) as RadioButton
            sortViewModel.sort(radioButton.text.toString())
            sortViewModel.setLastSort(radioButton.text.toString())
            this.dismiss()
        }
    }

    private fun getLastCheck() {
        when (sortViewModel.getLastSort()) {
            "Added Recently" -> binding.idBtnAddedRadio.isChecked = true
            "Lowest Price" -> binding.idBtnLowestRadio.isChecked = true
            "Highest Price" -> binding.idBtnHighestRadio.isChecked = true
            else -> {}
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBuyBottomShetBinding.inflate(inflater, container, false)
        return binding.root
    }
}