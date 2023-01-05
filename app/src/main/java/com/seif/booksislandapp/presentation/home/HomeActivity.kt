package com.seif.booksislandapp.presentation.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.ActivityHomeBinding
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationComponent()

        binding.fabProfile.setOnClickListener {
            navController.navigate(R.id.profileFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.profileFragment -> {
                    binding.bottomAppBar.hide()
                    binding.fabProfile.hide()
                }
                R.id.buyFragment -> {
                    binding.bottomAppBar.hide()
                    binding.fabProfile.hide()
                }
                else -> {
                    binding.bottomAppBar.show()
                    binding.fabProfile.show()
                }
            }
        }
    }

    private fun setupNavigationComponent() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() ||
            super.onSupportNavigateUp()
    }
}
