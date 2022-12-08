package com.seif.booksislandapp.presentation.intro.onboarding.onboarding3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentOnboarding3Binding

class OnBoarding3Fragment : Fragment() {
    private lateinit var binding: FragmentOnboarding3Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboarding3Binding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_onboarding3, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.btnFinish.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_introFragment)
        }
        binding.btnPrev.setOnClickListener {
            viewPager?.currentItem = 1
        }
    }
}