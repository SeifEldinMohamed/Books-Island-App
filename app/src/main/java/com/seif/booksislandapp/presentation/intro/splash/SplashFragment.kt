package com.seif.booksislandapp.presentation.intro.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentSplashBinding
import com.seif.booksislandapp.presentation.admin.AdminActivity
import com.seif.booksislandapp.presentation.home.HomeActivity
import com.seif.booksislandapp.utils.Constants.Companion.ANIMATION_DURATION
import com.seif.booksislandapp.utils.Constants.Companion.CURRENT_PROGRESS_ANIMATION
import com.seif.booksislandapp.utils.Constants.Companion.HANDLER_DELAY
import com.seif.booksislandapp.utils.Constants.Companion.IS_FIRST_TIME_KEY
import com.seif.booksislandapp.utils.Constants.Companion.IS_LOGGED_IN_KEY
import com.seif.booksislandapp.utils.Constants.Companion.MAX_PROGRESS_BAR
import com.seif.booksislandapp.utils.Constants.Companion.USER_ID_KEY
import com.seif.booksislandapp.utils.start
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val splashViewModel: SplashViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.primary)
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            handleNextNavigation()
        }, HANDLER_DELAY)
        animateProgressBar()
    }

    private fun handleNextNavigation() {
        if (splashViewModel.getFromSP(IS_FIRST_TIME_KEY, Boolean::class.java)) {
            Timber.d("onViewCreated: first time to enter")
            splashViewModel.saveInSP(IS_FIRST_TIME_KEY, false)
            splashViewModel.saveInSP(IS_LOGGED_IN_KEY, false)
            findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
        } else { // not first time
            if (splashViewModel.getFromSP(IS_LOGGED_IN_KEY, Boolean::class.java)) {
                if (splashViewModel.getFromSP(USER_ID_KEY, String::class.java) == "") {
                    requireActivity().apply {
                        start<AdminActivity>()
                        finish()
                    }
                } else {
                    requireActivity().apply {
                        start<HomeActivity>()
                        finish()
                    }
                }
            } else { // not logged in
                Timber.d("onViewCreated: user not logged in")
                findNavController().navigate(R.id.action_splashFragment_to_introFragment)
            }
        }
    }

    private fun animateProgressBar() {
        binding.progressBarSplash.max = MAX_PROGRESS_BAR
        ObjectAnimator.ofInt(binding.progressBarSplash, "progress", CURRENT_PROGRESS_ANIMATION)
            .setDuration(ANIMATION_DURATION)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.window?.statusBarColor = requireActivity().getColor(R.color.white)
    }
}