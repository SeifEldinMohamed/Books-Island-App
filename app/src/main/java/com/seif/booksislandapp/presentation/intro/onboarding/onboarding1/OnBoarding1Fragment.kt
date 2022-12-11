package com.seif.booksislandapp.presentation.intro.onboarding.onboarding1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.seif.booksislandapp.databinding.FragmentOnboarding1Binding

class OnBoarding1Fragment : Fragment() {
    private lateinit var binding: FragmentOnboarding1Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboarding1Binding.inflate(inflater, container, false)
        return binding.root
    }
}