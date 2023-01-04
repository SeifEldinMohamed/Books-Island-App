package com.seif.booksislandapp.presentation.intro.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {
    lateinit var binding: FragmentSplashBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.primary)
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
        }, 2000)
        binding.progressBarSplash.max = 1000
        val currentProgress = 1500
        ObjectAnimator.ofInt(binding.progressBarSplash, "progress", currentProgress)
            .setDuration(1000)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.white)
    }
}