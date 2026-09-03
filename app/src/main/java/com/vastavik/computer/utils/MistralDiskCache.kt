package com.vastavik.computer.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistent disk cache for Mistral AI problem overviews, solutions, and dynamic questions.
 * Uses Android SharedPreferences to guarantee persistence across app restarts and phone reboots.
 */
object MistralDiskCache {
    private const val PREFS_NAME = "mistral_disk_cache_v2"
    private const val PREFIX_SOL = "sol_"
    private const val KEY_AI_MCQS = "ai_custom_mcqs"
    private const val KEY_AI_PREDICT_OUTPUT = "ai_custom_predict_output"
    private const val KEY_AI_CODING = "ai_custom_coding"
    private const val KEY_AI_PYQS = "ai_custom_pyqs"

    /**
     * Retrieves a permanently cached problem overview / solution markdown.
     */
    fun getSolution(context: Context, key: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREFIX_SOL + key, null)?.takeIf { it.isNotBlank() }
    }

    /**
     * Permanently stores a problem overview / solution markdown to device disk.
     */
    fun saveSolution(context: Context, key: String, solution: String) {
        if (solution.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREFIX_SOL + key, solution).apply()
    }

    /**
     * Removes a cached solution from disk.
     */
    fun removeSolution(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREFIX_SOL + key).apply()
    }

    /**
     * Loads custom/dynamically generated AI MCQs from persistent storage.
     */
    fun getSavedMCQs(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_AI_MCQS, null) ?: return emptyList()
        return try {
            val jsonArr = JSONArray(raw)
            val result = mutableListOf<Pair<String, String>>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                result.add(Pair(obj.getString("title"), obj.getString("sub")))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMCQs(context: Context, items: List<Pair<String, String>>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArr = JSONArray()
        items.forEach { (title, sub) ->
            val obj = JSONObject().apply {
                put("title", title)
                put("sub", sub)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_AI_MCQS, jsonArr.toString()).apply()
    }

    /**
     * Loads custom/dynamically generated AI Coding challenges from persistent storage.
     */
    fun getSavedCoding(context: Context): List<Triple<String, String, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_AI_CODING, null) ?: return emptyList()
        return try {
            val jsonArr = JSONArray(raw)
            val result = mutableListOf<Triple<String, String, String>>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                result.add(Triple(obj.getString("title"), obj.getString("difficulty"), obj.getString("topic")))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCoding(context: Context, items: List<Triple<String, String, String>>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArr = JSONArray()
        items.forEach { (title, difficulty, topic) ->
            val obj = JSONObject().apply {
                put("title", title)
                put("difficulty", difficulty)
                put("topic", topic)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_AI_CODING, jsonArr.toString()).apply()
    }

    /**
     * Loads custom/dynamically generated AI PYQs from persistent storage.
     */
    fun getSavedPYQs(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_AI_PYQS, null) ?: return emptyList()
        return try {
            val jsonArr = JSONArray(raw)
            val result = mutableListOf<Pair<String, String>>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                result.add(Pair(obj.getString("title"), obj.getString("questions")))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePYQs(context: Context, items: List<Pair<String, String>>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArr = JSONArray()
        items.forEach { (title, questions) ->
            val obj = JSONObject().apply {
                put("title", title)
                put("questions", questions)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_AI_PYQS, jsonArr.toString()).apply()
    }

    /**
     * Loads custom/dynamically generated AI Predict Output sets from persistent storage.
     */
    fun getSavedPredictOutput(context: Context): List<Triple<String, String, String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_AI_PREDICT_OUTPUT, null) ?: return emptyList()
        return try {
            val jsonArr = JSONArray(raw)
            val result = mutableListOf<Triple<String, String, String>>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                result.add(Triple(obj.getString("title"), obj.getString("questions"), obj.getString("difficulty")))
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePredictOutput(context: Context, items: List<Triple<String, String, String>>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArr = JSONArray()
        items.forEach { (title, questions, difficulty) ->
            val obj = JSONObject().apply {
                put("title", title)
                put("questions", questions)
                put("difficulty", difficulty)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_AI_PREDICT_OUTPUT, jsonArr.toString()).apply()
    }
}
