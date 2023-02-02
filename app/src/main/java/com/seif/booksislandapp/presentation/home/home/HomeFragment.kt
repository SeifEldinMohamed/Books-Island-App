package com.seif.booksislandapp.presentation.home.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvDonate.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_donationFragment)
        }
        binding.cvBuy.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_buyFragment)
        }
        binding.cvExchange.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_exchangeFragment)
        }
        binding.cvBid.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_bidFragment)
        }
    }
}