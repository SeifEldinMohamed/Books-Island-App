package com.seif.booksislandapp.presentation.home.my_chats.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.my_chats.fragments.SellingChat
import com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats.BuyingChatFragment

class UserChatPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BuyingChatFragment()
            else -> SellingChat()
        }
    }
}