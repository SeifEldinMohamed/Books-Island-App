package com.seif.booksislandapp.presentation.home.upload_advertisement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentUploadSellAdvertisementBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadSellAdvertisementFragment : Fragment() {
    lateinit var binding: FragmentUploadSellAdvertisementBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUploadSellAdvertisementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val conditions = resources.getStringArray(R.array.condition)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, conditions)
        binding.acCondition.setAdapter(arrayAdapter)
    }
}