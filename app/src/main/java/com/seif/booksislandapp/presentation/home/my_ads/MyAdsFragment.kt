package com.seif.booksislandapp.presentation.home.my_ads

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentMyAdsBinding

class MyAdsFragment : Fragment() {
    lateinit var binding: FragmentMyAdsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddAdv.setOnClickListener {
            findNavController().navigate(R.id.action_myAdsFragment_to_uploadAdvertisementFragment2)
        }
    }
}