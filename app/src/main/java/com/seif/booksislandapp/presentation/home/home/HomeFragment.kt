package com.seif.booksislandapp.presentation.home.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentHomeBinding
import com.seif.booksislandapp.utils.Constants.Companion.USERNAME_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
            findNavController().navigate(R.id.action_homeFragment_to_auctionFragment)
        }
        binding.tvHelloUsername.text =
            getString(
                R.string.welcome_username,
                homeViewModel.readFromSP(key = USERNAME_KEY, String::class.java)
            )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}