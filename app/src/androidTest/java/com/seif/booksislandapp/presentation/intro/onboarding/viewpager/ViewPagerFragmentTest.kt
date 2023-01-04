package com.seif.booksislandapp.presentation.intro.onboarding.viewpager

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.seif.booksislandapp.R
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewPagerFragmentTest {

    @Test
    fun testNav() {
        // Getting the NavController for test
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        // Launches the Fragment in isolation
        launchFragmentInContainer<ViewPagerFragment>().onFragment { fragment ->
            // Setting the navigation graph for the NavController
            navController.setGraph(R.navigation.intro_nav_graph)

            // Sets the NavigationController for the specified View
            Navigation.setViewNavController(fragment.requireView(), navController)
            navController.setCurrentDestination(R.id.viewPagerFragment)
        }

        onView(ViewMatchers.withId(R.id.btn_prev))
            .check(matches(not(isDisplayed())))
        onView(ViewMatchers.withId(R.id.btn_next))
            .check(matches(isDisplayed()))

        onView(ViewMatchers.withId(R.id.btn_next))
            .perform(click()) // first click

        onView(ViewMatchers.withId(R.id.btn_next))
            .check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.btn_prev))
            .check(matches(isDisplayed()))

        onView(ViewMatchers.withId(R.id.btn_next))
            .perform(click()) // second click

        onView(ViewMatchers.withId(R.id.btn_next))
            .check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.btn_prev))
            .check(matches(isDisplayed()))
        onView(ViewMatchers.withId(R.id.btn_next))
            .check(matches(withText(R.string.finish)))

        onView(ViewMatchers.withId(R.id.btn_next))
            .perform(click()) // finish click
        assertEquals(
            navController.currentDestination?.id,
            R.id.introFragment
        )
    }
}