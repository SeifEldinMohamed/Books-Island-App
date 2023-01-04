package com.seif.booksislandapp.presentation.intro.splash

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seif.booksislandapp.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashFragmentTest {

    @Test
    fun testNav() {
        // Getting the NavController for test
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        // Launches the Fragment in isolation
        launchFragmentInContainer<SplashFragment>().onFragment { fragment ->
            // Setting the navigation graph for the NavController
            navController.setGraph(R.navigation.intro_nav_graph)

            // Sets the NavigationController for the specified View
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.iv_icon_splash))
            .check(matches(isDisplayed()))

        onView(ViewMatchers.withId(R.id.tv_app_name_splash))
            .check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.tv_app_name_splash))
            .check(matches(withText(R.string.app_name)))

        onView(ViewMatchers.withId(R.id.progressBar_splash))
            .check(matches(isDisplayed()))

        onView(ViewMatchers.withId(R.id.tv_loading_splash))
            .check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.tv_loading_splash))
            .check(matches(withText(R.string.loading)))
    }
}