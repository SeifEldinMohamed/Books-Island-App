package com.seif.booksislandapp.presentation.home.my_ads

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentMyAdsBinding
import com.seif.booksislandapp.presentation.home.my_ads.adapter.MyAdsPagerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAdsFragment : Fragment() {
    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!
    private val tabTitle = arrayListOf(" Buying ", " Donation ", " Exchanges ", " Auctions ")
    private var viewPager: ViewPager2? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        setupTabLayoutWithViewPager()
        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        viewPager = binding.viewPager
        viewPager!!.adapter = MyAdsPagerAdapter(this)

        TabLayoutMediator(binding.tlMyAds, viewPager!!) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        for (i in 0..4) {
            val textView =
                LayoutInflater.from(requireContext()).inflate(R.layout.my_ads_tab_title, null)
                    as TextView
            binding.tlMyAds.getTabAt(i)?.customView = textView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddAdv.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(500)
                when (viewPager!!.currentItem) {
                    0 -> navigateToUploadSellAdFragment()
                    1 -> navigateToUploadDonateAdFragment()
                    2 -> navigateToUploadExchangeAdFragment()
                    3 -> navigateToUploadBidAdFragment()
                }
            }
        }
    }

    private fun navigateToUploadSellAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadAdvertisementFragment2)
    }
    private fun navigateToUploadDonateAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadDonateFragment)
    }
    private fun navigateToUploadExchangeAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadExchangeFragment)
    }
    private fun navigateToUploadBidAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadBidFragment)
    }

    override fun onDestroyView() {
        _binding = null
        viewPager!!.adapter = null
        viewPager = null
        super.onDestroyView()
    }
}