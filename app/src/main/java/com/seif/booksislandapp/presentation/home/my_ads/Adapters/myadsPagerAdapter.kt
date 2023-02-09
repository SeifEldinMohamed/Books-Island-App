package com.seif.booksislandapp.presentation.home.my_ads.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.my_ads.fragments.MABidFragment
import com.seif.booksislandapp.presentation.home.my_ads.fragments.maBuyFragment
import com.seif.booksislandapp.presentation.home.my_ads.fragments.maDonateFragment
import com.seif.booksislandapp.presentation.home.my_ads.fragments.maExchangeFragment

class myadsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> maBuyFragment()
            1 -> maExchangeFragment()
            2 -> maDonateFragment()
            else -> MABidFragment()
        }
    }
}