package com.vastavik.computer.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import com.vastavik.computer.utils.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {
    val isDarkMode: Flow<Boolean> = themePreferences.isDarkMode
    fun setDarkMode(isDark: Boolean) { themePreferences.setDarkMode(isDark) }
    val isNeoBrutalish: Flow<Boolean> = themePreferences.isNeoBrutalish
    fun setNeoBrutalish(v: Boolean){ themePreferences.setNeoBrutalish(v) }
    val neoBrutalAccentIndex: Flow<Int> = themePreferences.neoBrutalAccentIndex
    fun setNeoBrutalAccentIndex(v: Int){ themePreferences.setNeoBrutalAccentIndex(v) }
}
