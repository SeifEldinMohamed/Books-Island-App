package com.seif.booksislandapp.presentation.home.my_ads.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.my_ads.fragments.MyAuctionsFragment
import com.seif.booksislandapp.presentation.home.my_ads.fragments.MyBuyFragment
import com.seif.booksislandapp.presentation.home.my_ads.fragments.MyDonateFragment
import com.seif.booksislandapp.presentation.home.my_ads.fragments.MyExchangeFragment

class MyAdsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyBuyFragment()
            1 -> MyDonateFragment()
            2 -> MyExchangeFragment()
            else -> MyAuctionsFragment()
        }
    }
}