package com.seif.booksislandapp.presentation.home.requests

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.home.requests.received_requests.ReceivedRequestsFragment
import com.seif.booksislandapp.presentation.home.requests.sent_requests.SentRequestsFragment

class RequestsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SentRequestsFragment()
            else -> ReceivedRequestsFragment()
        }
    }
}