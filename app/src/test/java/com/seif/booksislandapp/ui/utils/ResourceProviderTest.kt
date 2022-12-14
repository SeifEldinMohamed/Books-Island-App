package com.seif.booksislandapp.ui.utils

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.seif.booksislandapp.R
import com.seif.booksislandapp.utils.ResourceProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(MockitoJUnitRunner::class)
class ResourceProviderTest {

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun prepareMock() {
        mockContext = mock {
            on { getString(R.string.app_name) } doReturn "Books Island"
        }
    }

    @Test
    fun `getString() from ResourceProvider, then return app name`() {
        // Arrange
        val resourceProvider = ResourceProvider.Base(mockContext)
        val expected = "Books Island"
        // Act
        val actual = resourceProvider.string(R.string.app_name)
        // Assert
        assertThat(actual).isEqualTo(expected)
    }
}