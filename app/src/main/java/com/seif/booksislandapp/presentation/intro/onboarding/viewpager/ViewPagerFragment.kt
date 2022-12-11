package com.seif.booksislandapp.presentation.intro.onboarding.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentViewPagerBinding
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show

class ViewPagerFragment : Fragment() {
    private lateinit var binding: FragmentViewPagerBinding
    private lateinit var viewPager: ViewPager2
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewPager()
        binding.btnNext.setOnClickListener {
            handleNextButtonStates()
        }
        binding.btnPrev.setOnClickListener {
            handlePreviousButtonStates()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handleOnPageSelectedStates(position)
            }
        })
    }

    private fun initializeViewPager() {
        viewPager = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(requireContext() as FragmentActivity)
        binding.dotsIndicator.attachTo(viewPager)
    }

    private fun handleNextButtonStates() {
        when (viewPager.currentItem) {
            0 -> viewPager.currentItem = 1
            1 -> viewPager.currentItem = 2
            2 -> findNavController().navigate(R.id.action_viewPagerFragment_to_introFragment)
        }
    }

    private fun handlePreviousButtonStates() {
        when (viewPager.currentItem) {
            1 -> viewPager.currentItem = 0
            2 -> viewPager.currentItem = 1
        }
    }

    private fun handleOnPageSelectedStates(position: Int) {
        when (position) {
            0 -> {
                binding.btnNext.text = getString(R.string.next)
                binding.btnNext.show()
                binding.btnPrev.hide()
            }
            1 -> {
                binding.btnNext.text = getString(R.string.next)
                binding.btnNext.show()
                binding.btnPrev.show()
            }
            2 -> {
                binding.btnNext.text = getString(R.string.finish)
                binding.btnPrev.show()
                binding.btnNext.show()
            }
        }
    }
}