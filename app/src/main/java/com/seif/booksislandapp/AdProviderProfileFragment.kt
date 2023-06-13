package com.seif.booksislandapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentAdProviderProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class AdProviderProfileFragment : Fragment(), MenuProvider {
    private lateinit var _binding: FragmentAdProviderProfileBinding
    private lateinit var reportViewModel: ViewModel
    private lateinit var rateViewModel: ViewModel
    private lateinit var blockViewModel: ViewModel
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdProviderProfileBinding.inflate(layoutInflater)
        reportViewModel = ViewModelProvider(this)[ViewModel::class.java]
        rateViewModel = ViewModelProvider(this)[ViewModel::class.java]
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_report -> {
                    ReportUserSheet().show(parentFragmentManager, " ")
                }
                R.id.menu_rate -> {
                    RateUserSheet().show(parentFragmentManager, "")
                }
                R.id.menu_block -> {
                }
                else -> {
                }
            }
            true
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.user_action, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}
