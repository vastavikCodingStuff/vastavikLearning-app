package com.vastavik.computer.utils

import android.content.Context

object BoardLanguage {

    fun defaultFor(board: String): String = when (board.trim()) {
        "ICSE" -> "Java"
        "CBSE" -> "Python"
        "West Bengal Board" -> "C"
        else -> "Java"
    }

    fun supportedLanguages(): List<String> = listOf("Java", "Python", "C", "C++", "JavaScript")

    fun getPreferred(context: Context): String {
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("language", null)?.takeIf { it.isNotBlank() }
        if (savedLang != null) return savedLang
        val board = prefs.getString("board", "ICSE") ?: "ICSE"
        return defaultFor(board)
    }

    fun savePreferred(context: Context, language: String) {
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            .edit().putString("language", language).apply()
    }

    fun saveBoardAndDefaultLanguage(context: Context, board: String, currentLanguage: String? = null): String {
        val default = defaultFor(board)
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val existingLang = currentLanguage ?: prefs.getString("language", null)
        val isCustom = existingLang != null && existingLang != defaultFor(prefs.getString("board", "ICSE") ?: "ICSE")
        val languageToSave = if (isCustom && existingLang != null) existingLang else default
        prefs.edit().putString("board", board).putString("language", languageToSave).apply()
        return languageToSave
    }
}
