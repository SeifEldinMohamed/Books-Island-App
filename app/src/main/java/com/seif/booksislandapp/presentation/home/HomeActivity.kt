package com.seif.booksislandapp.presentation.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.ActivityHomeBinding
import com.seif.booksislandapp.utils.hide
import com.seif.booksislandapp.utils.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // appBarConfiguration = AppBarConfiguration.Builder(navController.graph)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.homeFragment,
                R.id.myAdsFragment,
                R.id.chatsFragment,
                R.id.wishListFragment
            )
        ).setOpenableLayout(binding.drawerLayout)
            .build()
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        // binding.navView.setupWithNavController(navController)

        setupNavigationComponent()

        binding.fabProfile.setOnClickListener {
            navController.navigate(R.id.profileFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    showBottomBar()
                    showToolBar()
                }
                R.id.myAdsFragment -> {
                    showBottomBar()
                    showToolBar()
                }
                R.id.chatsFragment -> {
                    showBottomBar()
                    showToolBar()
                }
                R.id.wishListFragment -> {
                    showBottomBar()
                    showToolBar()
                }
                else -> {
                    hideBottomBar()
                    hideToolBar()
                }
            }
        }
    }

    private fun showToolBar() {
        binding.toolBar.show()
    }
    private fun hideToolBar() {
        binding.toolBar.hide()
    }

    private fun hideBottomBar() {
        binding.bottomAppBar.hide()
        binding.fabProfile.visibility = View.GONE
    }

    private fun showBottomBar() {
        binding.fabProfile.visibility = View.VISIBLE
        binding.bottomAppBar.show()
    }

    private fun setupNavigationComponent() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() ||
            super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
