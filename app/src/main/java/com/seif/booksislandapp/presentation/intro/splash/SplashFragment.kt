package com.seif.booksislandapp.presentation.intro.splash

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentSplashBinding
import com.seif.booksislandapp.presentation.home.HomeActivity
import com.seif.booksislandapp.utils.Constants.Companion.IS_FIRST_TIME_KEY
import com.seif.booksislandapp.utils.Constants.Companion.IS_LOGGED_IN_KEY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SplashFragment : Fragment() {
    lateinit var binding: FragmentSplashBinding
    private val splashViewModel: SplashViewModel by viewModels()
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
          handleNextNavigation()
        }, 2000)
        binding.progressBarSplash.max = 1000
        val currentProgress = 1500
        ObjectAnimator.ofInt(binding.progressBarSplash, "progress", currentProgress)
            .setDuration(1000)
            .start()
    }

    private fun handleNextNavigation() {
        if (splashViewModel.getFromSP(IS_FIRST_TIME_KEY, Boolean::class.java)) {
            Timber.d("onViewCreated: first time to enter")
            splashViewModel.saveInSP(IS_FIRST_TIME_KEY, false)
            splashViewModel.saveInSP(IS_LOGGED_IN_KEY, false)
            findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
        } else { // not first time
            if (splashViewModel.getFromSP(IS_LOGGED_IN_KEY, Boolean::class.java)) {
                Timber.d("onViewCreated: user already logged in")
                Intent(requireActivity(), HomeActivity::class.java).also { intent ->
                    startActivity(intent)
                    requireActivity().finish()
                }
            } else { // not logged in
                Timber.d("onViewCreated: user not logged in")
                findNavController().navigate(R.id.action_splashFragment_to_introFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.white)
    }
}