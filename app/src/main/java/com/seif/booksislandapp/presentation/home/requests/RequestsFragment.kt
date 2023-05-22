package com.seif.booksislandapp.presentation.home.requests

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {
    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatViewPager.adapter = RequestsPagerAdapter(this)
        val tabTitle = arrayListOf(" Sent ", " Received ")
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
        _binding = null
        super.onDestroyView()
    }
}