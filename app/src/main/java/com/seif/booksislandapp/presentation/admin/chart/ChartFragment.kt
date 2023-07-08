package com.seif.booksislandapp.presentation.admin.chart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.seif.booksislandapp.databinding.FragmentChartBinding

class ChartFragment : Fragment() {
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webView.webViewClient = WebViewClient()

        binding.webView.loadUrl("https://colab.research.google.com/drive/1bFDbWxKLQ6hfKLgDwc2UdV_qxIcCk0PB?usp=sharing")

        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.javaScriptEnabled = true
    }
}