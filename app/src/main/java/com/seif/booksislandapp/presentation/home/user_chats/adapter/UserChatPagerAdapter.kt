package com.seif.booksislandapp.presentation.home.user_chats.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.user_chats.fragments.BuyingChat
import com.seif.booksislandapp.presentation.home.user_chats.fragments.SellingChat

class UserChatPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SellingChat()
            else -> BuyingChat()
        }
    }
}