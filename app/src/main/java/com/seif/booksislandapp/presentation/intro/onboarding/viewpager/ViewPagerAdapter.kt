package com.seif.booksislandapp.presentation.intro.onboarding.viewpager

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seif.booksislandapp.presentation.intro.onboarding.onboarding1.OnBoarding1Fragment
import com.seif.booksislandapp.presentation.intro.onboarding.onboarding2.OnBoarding2Fragment
import com.seif.booksislandapp.presentation.intro.onboarding.onboarding3.OnBoarding3Fragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 ->  OnBoarding1Fragment()
            1 ->  OnBoarding2Fragment()
            2 ->  OnBoarding3Fragment()
            else -> { throw Resources.NotFoundException("Position Not Found") }
        }
    }
}