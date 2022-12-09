package com.seif.booksislandapp.presentation.intro.onboarding.onboarding2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentOnboarding2Binding

class OnBoarding2Fragment : Fragment() {
    private lateinit var binding: FragmentOnboarding2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboarding2Binding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_onboarding2, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.btnNext.setOnClickListener {
            viewPager?.currentItem = 2
        }
        binding.btnPrev.setOnClickListener {
            viewPager?.currentItem = 0
        }
    }
}