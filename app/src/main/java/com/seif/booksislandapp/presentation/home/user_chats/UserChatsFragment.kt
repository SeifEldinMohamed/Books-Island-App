package com.seif.booksislandapp.presentation.home.user_chats

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentUserChatsBinding
import com.seif.booksislandapp.presentation.home.user_chats.adapter.UserChatPagerAdapter

class UserChatsFragment : Fragment() {
    private lateinit var binding: FragmentUserChatsBinding
    private val tabTitle = arrayListOf(" Buying ", " Selling ")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserChatsBinding.inflate(layoutInflater)
        setupTabLayoutWithViewPager()
        return binding.root
    }
    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        binding.chatViewPager.adapter = UserChatPagerAdapter(this)
        TabLayoutMediator(binding.tlChat, binding.chatViewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        for (i in 0..1) {
            val textView =
                LayoutInflater.from(requireContext()).inflate(R.layout.my_ads_tab_title, null)
                    as TextView
            binding.tlChat.getTabAt(i)?.customView = textView
        }
    }
}