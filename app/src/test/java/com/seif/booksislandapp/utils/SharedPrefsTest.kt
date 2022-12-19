package com.seif.booksislandapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class SharedPrefsTest {
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var sharedPreferences: SharedPreferences
    @Mock
    private lateinit var sharedPreferenceEditor: SharedPreferences.Editor

    private lateinit var sharedPrefs: SharedPrefs

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        sharedPrefs = SharedPrefs(context)
    }

    @Test
    fun `put() and get(), when put username, then retrieve it successfully`() {
        // Arrange
        val key = "user"
        val username = "seif"
        whenever(sharedPreferenceEditor.putString(anyString(), anyString())).thenReturn(null)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferenceEditor)
        whenever(sharedPreferences.getString(anyString(), anyString())).thenReturn(username)

        // Act
        sharedPrefs.put(key, username)
        val actual = sharedPrefs.get(key, String::class.java)

        // Assert
        Truth.assertThat(actual).isEqualTo(username)
    }
}