package com.seif.booksislandapp.presentation.home.my_ads

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentMyAdsBinding
import com.seif.booksislandapp.presentation.home.my_ads.Adapters.myadsPagerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAdsFragment : Fragment() {
    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!
    private val tabTitle = arrayListOf("  Buy  ", "Exchange", "Donate", "Biding")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        setupTabLayoutWithViewPager()
        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        binding.viewPager.adapter = myadsPagerAdapter(this)
        TabLayoutMediator(binding.tlMyAds, binding.viewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        for (i in 0..4) {
            val textView = LayoutInflater.from(requireContext()).inflate(R.layout.myads_tab_title, null)
                as TextView
            binding.tlMyAds.getTabAt(i)?.customView = textView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddAdv.setOnClickListener {
            lifecycleScope.launch {
                delay(500)
                findNavController().navigate(R.id.action_myAdsFragment_to_uploadAdvertisementFragment2)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}