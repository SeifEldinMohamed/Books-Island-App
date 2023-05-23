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
    private var viewPager: ViewPager2? = null
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutWithViewPager()
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

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        val tabTitle = arrayListOf(" Selling ", " Donation ", " Exchange ", " Auction ")
        viewPager = binding.viewPager
        viewPager!!.adapter = MyAdsPagerAdapter(this)

        mediator = TabLayoutMediator(binding.tlMyAds, viewPager!!) { tab, position ->
            tab.text = tabTitle[position]
        }
        mediator!!.attach()
        for (i in 0 until tabTitle.size) {
            val textView =
                LayoutInflater.from(requireContext()).inflate(R.layout.my_ads_tab_title, null)
                        as TextView
            binding.tlMyAds.getTabAt(i)?.customView = textView
        }
    }

    private fun navigateToUploadSellAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadSellAdvertisementFragment)
    }

    private fun navigateToUploadDonateAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadDonateFragment)
    }

    private fun navigateToUploadExchangeAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadExchangeFragment)
    }
    private fun navigateToUploadBidAdFragment() {
        findNavController().navigate(R.id.action_myAdsFragment_to_uploadAuctionFragment)
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        viewPager!!.adapter = null
        viewPager = null
        _binding = null
        super.onDestroyView()
    }
}