package com.seif.booksislandapp.presentation.home.wish_list.Adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.wish_list.fragments.auction.AuctionWishListFragment
import com.seif.booksislandapp.presentation.home.wish_list.fragments.buy.BuyWishListFragment
import com.seif.booksislandapp.presentation.home.wish_list.fragments.donate.DonationWishListFragment
import com.seif.booksislandapp.presentation.home.wish_list.fragments.exchange.ExchangeWishListFragment

class WishListPagerAdater(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BuyWishListFragment()
            1 -> DonationWishListFragment()
            2 -> ExchangeWishListFragment()
            else -> AuctionWishListFragment()
        }
    }
}