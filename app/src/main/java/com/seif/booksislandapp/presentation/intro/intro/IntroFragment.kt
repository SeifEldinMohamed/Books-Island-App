package com.seif.booksislandapp.presentation.intro.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentIntroBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroFragment : Fragment() {
    private var _binding: FragmentIntroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIntroBinding.inflate(inflater, container, false)
        // inflater.inflate(R.layout.fragment_intro, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_loginFragment)
        }
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
