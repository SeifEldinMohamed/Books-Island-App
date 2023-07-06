package com.seif.booksislandapp.presentation.home.my_ads

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentMyAdsBinding
import com.seif.booksislandapp.presentation.home.home.HomeViewModel
import com.seif.booksislandapp.presentation.home.my_ads.adapter.MyAdsPagerAdapter
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.showErrorSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MyAdsFragment : Fragment() {
    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!
    private var viewPager: ViewPager2? = null
    private var mediator: TabLayoutMediator? = null
    private val homeViewModel: HomeViewModel by viewModels()
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

            if (!homeViewModel.readFromSP(Constants.IS_SUSPENDED_KEY, Boolean::class.java)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    when (viewPager!!.currentItem) {
                        0 -> navigateToUploadSellAdFragment()
                        1 -> navigateToUploadDonateAdFragment()
                        2 -> navigateToUploadExchangeAdFragment()
                        3 -> navigateToUploadBidAdFragment()
                    }
                }
            } else {
                binding.root.showErrorSnackBar("Sorry but your account is suspended")
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        val tabTitle = arrayListOf(" Selling ", " Donation ", " Exchange ", " Auction ")
        viewPager = binding.viewPager
        viewPager!!.adapter = MyAdsPagerAdapter(this)

        viewPager!!.offscreenPageLimit =
            1 // to prevent creating preloading fragment when click on tab

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
        viewPager!!.adapter = null
        viewPager = null
        mediator?.detach()
        mediator = null
        _binding = null
        super.onDestroyView()
    }
}