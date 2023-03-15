package com.seif.booksislandapp.presentation.home.wish_list

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentWishListBinding
import com.seif.booksislandapp.presentation.home.wish_list.Adapter.WishListPagerAdater

class WishListFragment : Fragment() {

    private lateinit var binding: FragmentWishListBinding
    private val tabTitle = arrayListOf(" Buying ", " Donation ", " Exchanges ", " Auctions ")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishListBinding.inflate(layoutInflater)
        setupTabLayoutWithViewPager()
        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        binding.WishViewPager.adapter = WishListPagerAdater(this)
        TabLayoutMediator(binding.tlWishlist, binding.WishViewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        for (i in 0..4) {
            val textView =
                LayoutInflater.from(requireContext()).inflate(R.layout.my_ads_tab_title, null)
                    as TextView
            binding.tlWishlist.getTabAt(i)?.customView = textView
        }
    }
}