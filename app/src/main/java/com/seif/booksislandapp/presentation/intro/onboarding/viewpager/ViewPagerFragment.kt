package com.seif.booksislandapp.presentation.intro.onboarding.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.seif.booksislandapp.databinding.FragmentViewPagerBinding

class ViewPagerFragment : Fragment() {
    private lateinit var binding: FragmentViewPagerBinding
    private lateinit var viewPager: ViewPager2
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_view_pager, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dotsIndicator = binding.dotsIndicator
        viewPager = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(requireContext() as FragmentActivity)
        dotsIndicator.attachTo(viewPager)
    }
}