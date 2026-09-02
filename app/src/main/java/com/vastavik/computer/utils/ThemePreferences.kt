package com.vastavik.computer.utils

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ThemePreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    val isDarkMode: Flow<Boolean> = callbackFlow {
        val initialValue = sharedPreferences.getBoolean(Constants.PREF_DARK_MODE, false)
        trySend(initialValue)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == Constants.PREF_DARK_MODE) {
                trySend(prefs.getBoolean(Constants.PREF_DARK_MODE, false))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    fun setDarkMode(isDark: Boolean) { sharedPreferences.edit().putBoolean(Constants.PREF_DARK_MODE, isDark).apply() }

    val isNeoBrutalish: Flow<Boolean> = callbackFlow {
        trySend(sharedPreferences.getBoolean("neo_brutalish", true))
        val l = SharedPreferences.OnSharedPreferenceChangeListener { prefs, k -> if (k=="neo_brutalish") trySend(prefs.getBoolean("neo_brutalish", true)) }
        sharedPreferences.registerOnSharedPreferenceChangeListener(l)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(l) }
    }
    fun setNeoBrutalish(v:Boolean){ sharedPreferences.edit().putBoolean("neo_brutalish", v).apply() }

    // 0=Yellow, 1=Pink, 2=Blue, 3=Lime, 4=Orange, 5=Purple
    val neoBrutalAccentIndex: Flow<Int> = callbackFlow {
        trySend(sharedPreferences.getInt("neo_brutal_accent", 2))
        val l = SharedPreferences.OnSharedPreferenceChangeListener { prefs, k -> if (k=="neo_brutal_accent") trySend(prefs.getInt("neo_brutal_accent", 2)) }
        sharedPreferences.registerOnSharedPreferenceChangeListener(l)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(l) }
    }
    fun setNeoBrutalAccentIndex(v:Int){ sharedPreferences.edit().putInt("neo_brutal_accent", v).apply() }

    val fontScale: Flow<Float> = callbackFlow {
        trySend(sharedPreferences.getFloat("font_scale", 1f))
        val l = SharedPreferences.OnSharedPreferenceChangeListener { prefs,k -> if(k=="font_scale") trySend(prefs.getFloat("font_scale",1f)) }
        sharedPreferences.registerOnSharedPreferenceChangeListener(l)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(l) }
    }
    fun setFontScale(v:Float){ sharedPreferences.edit().putFloat("font_scale", v).apply() }
}
