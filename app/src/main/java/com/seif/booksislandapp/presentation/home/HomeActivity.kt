package com.seif.booksislandapp.presentation.home

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.ActivityHomeBinding
import com.seif.booksislandapp.presentation.admin.report_details.GetUserByIdState
import com.seif.booksislandapp.presentation.home.categories.recommendation.RecommendationViewModel
import com.seif.booksislandapp.presentation.home.home.HomeViewModel
import com.seif.booksislandapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    private val homeViewModel: HomeViewModel by viewModels()
    private val recommendationViewModel: RecommendationViewModel by viewModels()
    private val TAG = "HomeActivity"
    //  private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // appBarConfiguration = AppBarConfiguration.Builder(navController.graph)
        homeViewModel.currentUser(homeViewModel.readFromSP(Constants.USER_ID_KEY, String::class.java))
        observe()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.homeFragment,
                R.id.myAdsFragment,
                R.id.myChatsFragment,
                R.id.wishListFragment
            )
        ).setOpenableLayout(binding.drawerLayout)
            .build()

        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setNavigationItemSelectedListener(this)
        setupNavigationComponent()

        binding.fabProfile.setOnClickListener {
            if (!recommendationViewModel.getFromSP(
                    Constants.IS_SUSPENDED_KEY,
                    Boolean::class.java
                )
            ) {
                navController.navigate(R.id.profileFragment)
            } else {
                binding.root.showErrorSnackBar("Sorry but your account is suspended")
            }
        }
        binding.ivRequests.setOnClickListener {
            if (!recommendationViewModel.getFromSP(
                    Constants.IS_SUSPENDED_KEY,
                    Boolean::class.java
                )
            ) {
                navController.navigate(R.id.requestsFragment)
            } else {
                binding.root.showErrorSnackBar("Sorry but your account is suspended")
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.coordinatorLayout.show()
                    binding.ivRequests.show()
                    showToolBar()
                }
                R.id.myAdsFragment -> {
                    binding.coordinatorLayout.show()
                    binding.ivRequests.show()
                    showToolBar()
                }
                R.id.myChatsFragment -> {
                    binding.coordinatorLayout.show()
                    binding.ivRequests.show()
                    showToolBar()
                }
                R.id.wishListFragment -> {
                    binding.coordinatorLayout.show()
                    binding.ivRequests.show()
                    showToolBar()
                }

                else -> {
                    binding.coordinatorLayout.hide()
                    binding.ivRequests.hide()
                    hideToolBar()
                }
            }
        }

        // handleOnBackPressed()
    }
    private fun observe() {
        lifecycleScope.launch {
            homeViewModel.userState.collect {
                when (it) {
                    is GetUserByIdState.Init -> Unit
                    is GetUserByIdState.IsLoading -> Unit
                    is GetUserByIdState.GetUserByIdSuccessfully -> {
                        homeViewModel.saveInSP(Constants.IS_SUSPENDED_KEY, it.user.isSuspended)
                    }
                    is GetUserByIdState.ShowError -> {
                        handleErrorState(it.message)
                    }
                    is GetUserByIdState.NoInternetConnection -> Unit
                }
            }
        }
    }
    private fun handleErrorState(message: String) {
        binding.root.showErrorSnackBar(message)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_instructions -> {}
            R.id.menu_complaints -> complaints()
            R.id.menu_share -> shareApp()
            R.id.menu_rate -> rateApp()
            R.id.menu_review -> reviewApp()
            R.id.menu_our_apps -> ourApps()
            R.id.menu_about -> aboutDeveloper()
        }
        binding.drawerLayout.close()
        return true
    }

    private fun complaints() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    getString(
                        R.string.mail_to_from_books_island,
                        Constants.BOOKS_ISLAND_EMAIL,
                        getString(R.string.subject_email),
                        getString(R.string.Complaint)
                    )
                )
            )
        )
    }

//    private fun handleOnBackPressed() {
//        onBackPressedCallback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                // Handle the back button press event
//                if (binding.drawerLayout.isOpen) {
//                    binding.drawerLayout.close()
//                } else {
//                    if (navController.currentDestination?.id != R.id.homeFragment) {
//                        navController.popBackStack(R.id.homeFragment, false)
//                    } else if (isTaskRoot && supportFragmentManager.backStackEntryCount == 0) {
//                        finishAfterTransition()
//                    }
//                }
//            }
//        }
//        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
//    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.close()
        } else {
            super.onBackPressed()
        }
    }

    private fun shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(
                R.string.share_app,
                Constants.GOOGLE_PLAY_URL + packageName
            )

        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_your_app_with)))
    }

    private fun rateApp() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    Constants.RATE_ON_GOOGLE_PLAY_URL + packageName
                )
            )
        )
    }

    private fun reviewApp() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    getString(
                        R.string.mail_to_from_books_island,
                        Constants.BOOKS_ISLAND_EMAIL,
                        getString(R.string.subject_email),
                        getString(R.string.Review)
                    )
                )
            )
        )
    }

    private fun ourApps() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    Constants.OUR_APPS_ON_GOOGLE_PLAY_URL
                )
            )
        )
    }

    private fun aboutDeveloper() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.about_dialog)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok_about)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showToolBar() {
        binding.toolBar.show()
    }

    private fun hideToolBar() {
        binding.toolBar.hide()
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
        // onBackPressedCallback.isEnabled = false // Disable the callback
        //  onBackPressedCallback.remove() // Unregister the callback
        _binding = null

        super.onDestroy()
    }
}
