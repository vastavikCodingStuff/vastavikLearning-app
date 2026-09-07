package com.vastavik.computer.utils

/**
 * Centralised display-name resolution so every screen shows the same greeting.
 *
 * Rules:
 *  1. If the current user is the admin, always return "Admin".
 *  2. Otherwise return the FIRST word of the saved name (so a name like
 *     "Chandree Prakash Singh" becomes "Chandree").
 *  3. Fall back to the email local-part first letter uppercased.
 *  4. Final fallback is "Student".
 */
object DisplayName {

    const val ADMIN = "Admin"
    const val STUDENT_FALLBACK = "Student"

    fun resolve(rawName: String?): String {
        val cleaned = rawName?.trim().orEmpty()
        if (cleaned.isEmpty()) return STUDENT_FALLBACK
        val firstToken = cleaned.split(Regex("\\s+")).firstOrNull { it.isNotBlank() }
            ?: return STUDENT_FALLBACK
        return firstToken.replaceFirstChar { it.uppercase() }
    }

    fun resolveForUser(rawName: String?, isAdmin: Boolean): String {
        if (isAdmin) return ADMIN
        return resolve(rawName)
    }
}
