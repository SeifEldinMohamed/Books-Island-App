package com.seif.booksislandapp.presentation.home.categories.buy

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.databinding.FragmentBuyBinding
import com.seif.booksislandapp.presentation.home.categories.buy.adapter.BuyAdapter
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class BuyFragment : Fragment() {
    lateinit var binding: FragmentBuyBinding
    private val buyViewModel: BuyViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private val buyAdapter by lazy { BuyAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBuyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createAlertDialog(requireActivity())
        observe()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                lifecycleScope.launch() {
                    delay(1000)
                    text?.let {
                        if (it.toString().isEmpty()) {
                            buyViewModel.fetchAllSellAdvertisement()
                        } else {
                            buyViewModel.searchSellAdvertisements(
                                searchQuery = it.toString()
                            )
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.rvBuy.adapter = buyAdapter
    }

    private fun observe() {
        lifecycleScope.launch {
            buyViewModel.buyState.collect {
                when (it) {
                    BuyState.Init -> Unit
                    is BuyState.FetchAllSellAdvertisementSuccessfully -> {
                        buyAdapter.updateList(it.sellAds)
                    }
                    is BuyState.SearchSellAdvertisementSuccessfully -> {
                        buyAdapter.updateList(it.searchedSellAds)
                    }
                    is BuyState.IsLoading -> handleLoadingState(it.isLoading)
                    is BuyState.NoInternetConnection -> handleNoInternetConnectionState(binding.root)
                    is BuyState.ShowError -> handleErrorState(it.message)
                }
            }
        }
    }

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                startLoadingDialog()
            }
            false -> dismissLoadingDialog()
        }
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }
}