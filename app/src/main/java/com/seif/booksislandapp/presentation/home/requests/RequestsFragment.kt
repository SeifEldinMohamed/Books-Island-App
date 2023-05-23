package com.seif.booksislandapp.presentation.home.requests

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentRequestsBinding

class RequestsFragment : Fragment() {
    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!
    private var viewPager: ViewPager2? = null
    private var mediator: TabLayoutMediator? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTabLayoutWithViewpager()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("InflateParams")
    private fun setUpTabLayoutWithViewpager() {
        val tabTitle = arrayListOf(" Sent ", " Received ")

        viewPager = binding.requestsViewPager
        viewPager!!.adapter = RequestsPagerAdapter(this)

        mediator = TabLayoutMediator(binding.tlRequests, viewPager!!) { tab, position ->
            tab.text = tabTitle[position]
        }
        mediator!!.attach()
        for (i in 0 until tabTitle.size) {
            val textView =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.requests_tab_title, null) as TextView
            binding.tlRequests.getTabAt(i)?.customView = textView
        }
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null
        viewPager!!.adapter = null
        viewPager = null
        _binding = null
        super.onDestroyView()
    }
}