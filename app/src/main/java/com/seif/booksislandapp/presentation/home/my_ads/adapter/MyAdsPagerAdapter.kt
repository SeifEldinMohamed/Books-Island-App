package com.seif.booksislandapp.presentation.home.my_ads.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.my_ads.auction.MyAdsAuctionsFragment
import com.seif.booksislandapp.presentation.home.my_ads.donate.MyAdsDonateFragment
import com.seif.booksislandapp.presentation.home.my_ads.exchange.MyAdsExchangeFragment
import com.seif.booksislandapp.presentation.home.my_ads.sell.MySellAdsFragment

class MyAdsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MySellAdsFragment()
            1 -> MyAdsDonateFragment()
            2 -> MyAdsExchangeFragment()
            else -> MyAdsAuctionsFragment()
        }
    }
}