package com.seif.booksislandapp.presentation.home.my_chats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentMyChatsBinding
import com.seif.booksislandapp.presentation.home.my_chats.adapter.UserChatPagerAdapter

class MyChatsFragment : Fragment() {
    private var _binding: FragmentMyChatsBinding? = null
    private val binding get() = _binding!!
    private val tabTitle = arrayListOf(" Buying ", " Selling ")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutWithViewPager()
    }

    @SuppressLint("InflateParams")
    private fun setupTabLayoutWithViewPager() {
        binding.chatViewPager.adapter = UserChatPagerAdapter(this)
        TabLayoutMediator(binding.tlChat, binding.chatViewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        for (i in 0..1) {
            val textView =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.my_ads_tab_title, null) as TextView
            binding.tlChat.getTabAt(i)?.customView = textView
        }
    }

    override fun onDestroyView() {
        binding.chatViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}