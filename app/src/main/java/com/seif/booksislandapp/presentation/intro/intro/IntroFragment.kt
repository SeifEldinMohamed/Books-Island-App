package com.seif.booksislandapp.presentation.intro.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R

class IntroFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_loginFragment)
        }
        val btnRegister = view.findViewById<Button>(R.id.btn_registre)
        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_registerFragment)
        }
        return view
    }
}