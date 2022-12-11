package com.seif.booksislandapp.presentation.intro.onboarding.onboarding2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.seif.booksislandapp.databinding.FragmentOnboarding2Binding

class OnBoarding2Fragment : Fragment() {
    private lateinit var binding: FragmentOnboarding2Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboarding2Binding.inflate(layoutInflater, container, false)
        return binding.root
    }
}