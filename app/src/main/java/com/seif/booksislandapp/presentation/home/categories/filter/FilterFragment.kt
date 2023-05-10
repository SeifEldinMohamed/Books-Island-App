package com.seif.booksislandapp.presentation.home.categories.filter

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentFilterBinding
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel.RegisterState
import com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel.RegisterViewModel
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
@AndroidEntryPoint
class FilterFragment : Fragment() {

    private lateinit var binding: FragmentFilterBinding
    private var governorates: List<Governorate>? = null
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()

    private var districts: List<District>? = null
    private val registerViewModel: RegisterViewModel by viewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private var governorateId: String? = null
    private var governorateName: String? = null
    private var districtName: String? = null
    private var categoryName: String? = null
    private lateinit var dialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        observe()
        binding.ivBack.setOnClickListener {
            filterViewModel.filter(null)
            findNavController().navigateUp()
        }
        binding.btnApply.setOnClickListener {
            filter()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            itemCategoryViewModel.selectedCategoryItem.collect {
                categoryName = it
                binding.btnCategory.text = categoryName
            }
        }

        binding.btnCategory.setOnClickListener {
            findNavController().navigate(R.id.action_filterFragment_to_bookCategoriesFragment)
        }
        binding.acGovernorates.setOnItemClickListener { _, _, i, _ ->
            governorates?.let {
                governorateId = it[i].id

                governorateName = it[i].name
                registerViewModel.getDistricts(it[i].id)
            }
        }
        binding.acDistrect.setOnItemClickListener { _, _, i, _ ->
            districts?.let {
                districtName = it[i].name
            }
        }
        binding.tilDistrict.disable()
    }

    private fun filter() {
        val radioButton: RadioButton = binding.root.findViewById(binding.rgCondition.checkedRadioButtonId)
        if (categoryName == "Choose Category")
            categoryName = null

        observeOnFilterState(
            FilterBy(
                categoryName,
                governorateName,
                districtName,
                radioButton.text.toString()
            )
        )
        findNavController().popBackStack()
    }
    private fun observeOnFilterState(filterBy: FilterBy) {
        when (filterViewModel.isValidFilter(filterBy)) {
            is Resource.Error -> filterViewModel.filter(null)
            is Resource.Success -> filterViewModel.filter(filterBy)
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            registerViewModel.registerState.collect {
                when (it) {
                    RegisterState.Init -> Unit
                    is RegisterState.IsLoading -> handleLoadingState(it.isLoading)
                    is RegisterState.RegisteredSuccessfully -> Unit
                    is RegisterState.GetGovernoratesSuccessfully -> {
                        governorates = it.governorates
                        setUpGovernoratesDropDown(it.governorates)
                    }
                    is RegisterState.GetDistrictsSuccessfully -> {
                        districts = it.districts
                        binding.acDistrect.setText("")
                        binding.tilDistrict.enabled()
                        setUpDistrictsDropDown(it.districts)
                    }
                    is RegisterState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is RegisterState.NoInternetConnection -> {
                        handleNoInternetConnectionState()
                    }
                }
            }
        }
    }

    private fun handleNoInternetConnectionState() {
        NoInternetDialogPendulum.Builder(
            requireActivity(),
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        when (hasActiveConnection) {
                            true -> {
                                checkFetchingDataFromServer()
                                binding.root.showInfoSnackBar("Internet connection is back")
                            }
                            false -> Unit
                        }
                    }
                }

                cancelable = true // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional
                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()
    }

    private fun setUpDistrictsDropDown(districts: List<District>) {
        val districtsName: List<String> = districts.map { it.name }
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, districtsName)
        binding.acDistrect.setAdapter(arrayAdapter)
    }

    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    private fun checkFetchingDataFromServer() {
        if (governorates == null)
            registerViewModel.getGovernorates()
        else if (districts == null) {
            governorateId?.let {
                registerViewModel.getDistricts(it)
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        when (isLoading) {
            true -> {
                startLoadingDialog()
            }
            false -> dismissLoadingDialog()
        }
    }

    private fun setUpGovernoratesDropDown(governorates: List<Governorate>) {
        val governoratesName: List<String> = governorates.map { it.name }
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, R.id.tv_text, governoratesName)
        binding.acGovernorates.setAdapter(arrayAdapter)
    }

    private fun startLoadingDialog() {
        dialog.create()
        dialog.show()
    }

    private fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemCategoryViewModel.reset()
    }
}