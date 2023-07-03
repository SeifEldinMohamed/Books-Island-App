package com.seif.booksislandapp.presentation.home.categories.filter

import android.app.AlertDialog
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private var governorates: List<Governorate>? = null
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    private val chooseCategory = "Choose Category"
    private var districts: List<District>? = null
    private val registerViewModel: RegisterViewModel by viewModels()
    private val filterViewModel: FilterViewModel by activityViewModels()
    private var governorateId: String? = null
    private var governorateName: String? = null
    private var districtName: String? = null
    private var condition: String? = null
    private var categoryName: String? = null
    private lateinit var dialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = requireContext().createLoadingAlertDialog(requireActivity())
        setLastFilter(filterViewModel.lastFilter)
        binding.ivBack.setOnClickListener {
            filterViewModel.filter(null)
            findNavController().navigateUp()
        }
        binding.btnApply.setOnClickListener {
            filter()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                itemCategoryViewModel.selectedCategoryItem.collect {
                    if (it != chooseCategory) {
                        categoryName = it
                        binding.btnCategory.text = categoryName
                    }
                }
            }
        }
        binding.btnCategory.setOnClickListener {
            filterViewModel.lastFilter = FilterBy(
                categoryName,
                governorateName,
                districtName,
                condition
            )
            districts?.let {
                filterViewModel.lastDistricts = it
            }
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
        if (filterViewModel.lastDistricts.isEmpty())
            binding.tilDistrict.disable()
    }

    private fun setLastFilter(lastFilter: FilterBy) {
        checkFetchingDataFromServer()
        observe()
        binding.btnCategory.text = chooseCategory

        if (lastFilter.category != null) {
            binding.btnCategory.text = lastFilter.category
            categoryName = lastFilter.category
        }
        if (lastFilter.district != null) {
            districtName = lastFilter.district
            binding.tilDistrict.hint = districtName
        }
        if (lastFilter.governorate != null) {

            binding.tilGovernorate.hint = lastFilter.governorate
            governorateName = lastFilter.governorate
            binding.tilDistrict.enabled()
            districts = filterViewModel.lastDistricts
            setUpDistrictsDropDown(districts!!)
        }

        if (lastFilter.condition != null && lastFilter.condition == "All") {
            binding.cbBadUsed.isChecked = true
            binding.cbGoodUsed.isChecked = true
            binding.cbNew.isChecked = true
            condition = "All"
        }
        if (lastFilter.condition != null && lastFilter.condition == "Used With Good Condition") {
            binding.cbGoodUsed.isChecked = true
            condition = "Used With Good Condition"
        }
        if (lastFilter.condition != null && lastFilter.condition == "New&Used With Good Condition") {
            binding.cbGoodUsed.isChecked = true
            binding.cbNew.isChecked = true
            condition = "New&Used With Good Condition"
        }
        if (lastFilter.condition != null && lastFilter.condition == "New&Used With Bad Condition") {
            binding.cbBadUsed.isChecked = true
            binding.cbNew.isChecked = true
            condition = "New&Used With Bad Condition"
        }
        if (lastFilter.condition != null && lastFilter.condition == "Used With Good Condition&Used With Bad Condition") {
            binding.cbGoodUsed.isChecked = true
            binding.cbBadUsed.isChecked = true
            condition = "Used With Good Condition&Used With Bad Condition"
        }
        if (lastFilter.condition != null && lastFilter.condition == "Used With Bad Condition") {
            binding.cbBadUsed.isChecked = true
            condition = "Used With Bad Condition"
        }
        if (lastFilter.condition != null && lastFilter.condition == "New") {
            binding.cbNew.isChecked = true
            condition = "New"
        }
    }

    private fun filter() {
        setConditionStatus()
        if (categoryName == chooseCategory)
            categoryName = null
        filterViewModel.lastFilter = FilterBy(
            categoryName,
            governorateName,
            districtName,
            condition
        )
        districts?.let {
            filterViewModel.lastDistricts = it
        }
        observeOnFilterState(
            FilterBy(
                categoryName,
                governorateName,
                districtName,
                condition
            )
        )
    }

    private fun setConditionStatus() {
        condition =
            if (binding.cbNew.isChecked && binding.cbGoodUsed.isChecked && binding.cbBadUsed.isChecked) {
                "All"
            } else if (binding.cbNew.isChecked && binding.cbGoodUsed.isChecked) {
                "New&Used With Good Condition"
            } else if (binding.cbGoodUsed.isChecked && binding.cbBadUsed.isChecked) {
                "Used With Good Condition&Used With Bad Condition"
            } else if (binding.cbNew.isChecked && binding.cbBadUsed.isChecked) {
                "New&Used With Bad Condition"
            } else if (binding.cbGoodUsed.isChecked) {
                "Used With Good Condition"
            } else if (binding.cbBadUsed.isChecked) {
                "Used With Bad Condition"
            } else if (binding.cbNew.isChecked) {
                "New"
            } else {
                null
            }
    }

    private fun observeOnFilterState(filterBy: FilterBy) {
        when (val result = filterViewModel.isValidFilter(filterBy)) {
            is Resource.Error -> {
                binding.root.showErrorSnackBar(result.message)
            }
            is Resource.Success -> {
                if (filterBy.condition == "All")
                    filterViewModel.filter(
                        FilterBy(
                            filterBy.category,
                            filterBy.governorate,
                            filterBy.district,
                            null
                        )
                    )
                else
                    filterViewModel.filter(filterBy)

                findNavController().popBackStack()
            }
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
        governorateId = null
        governorateName = null
        districtName = null
        categoryName = null
        districts = null
        governorates = null
        itemCategoryViewModel.reset()
        _binding = null
    }
}