package com.seif.booksislandapp.presentation.home.wish_list.Adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.wish_list.fragments.auction.AuctionWishList
import com.seif.booksislandapp.presentation.home.wish_list.fragments.donate.DonateWishList
import com.seif.booksislandapp.presentation.home.wish_list.fragments.exchange.ExchangeWishList
import com.seif.booksislandapp.presentation.home.wish_list.fragments.buy.SellWishList

class WishListPagerAdater(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SellWishList()
            1 -> DonateWishList()
            2 -> ExchangeWishList()
            else -> AuctionWishList()
        }
    }
}