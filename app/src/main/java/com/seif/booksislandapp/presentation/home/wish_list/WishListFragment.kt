package com.seif.booksislandapp.presentation.home.wish_list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentWishListBinding
import com.seif.booksislandapp.presentation.home.wish_list.Adapter.WishListPagerAdater

class WishListFragment : Fragment() {
    private var _binding: FragmentWishListBinding? = null
    private val binding get() = _binding!!
    private val tabTitle = arrayListOf(" Buying ", " Donation ", " Exchange ", " Auction ")
    private var viewPager: ViewPager2? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishListBinding.inflate(inflater, container, false)
        setupTabLayoutWithViewPager()
        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        viewPager = binding.WishViewPager
        viewPager!!.adapter = WishListPagerAdater(this)
        viewPager!!.offscreenPageLimit = 1

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
    override fun onDestroyView() {
        viewPager!!.adapter = null
        viewPager = null
        _binding = null
        super.onDestroyView()
    }
}