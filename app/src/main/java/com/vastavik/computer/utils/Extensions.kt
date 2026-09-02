package com.vastavik.computer.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.longToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 6
}

fun String.capitalizeFirst(): String {
    return if (this.isNotEmpty()) {
        this.replaceFirstChar { it.uppercase() }
    } else {
        this
    }
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.take(maxLength - 3) + "..."
    } else {
        this
    }
}

fun Long.formatTimestamp(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}

fun String.toRelativeTime(): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(this) ?: return this
        val now = Date()
        val diff = now.time - date.time

        when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            else -> {
                val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                displayFormat.format(date)
            }
        }
    } catch (e: Exception) {
        this
    }
}

fun String.formatDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(this) ?: return this
        outputFormat.format(date)
    } catch (e: Exception) {
        this
    }
}

fun String.formatDateTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(this) ?: return this
        outputFormat.format(date)
    } catch (e: Exception) {
        this
    }
}

fun String.getInitials(): String {
    return this.split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}

fun Double.formatCurrency(currency: String = "INR"): String {
    return when (currency) {
        "INR" -> "\u20B9${String.format("%.0f", this)}"
        "USD" -> "$${String.format("%.2f", this)}"
        "EUR" -> "\u20AC${String.format("%.2f", this)}"
        else -> "$currency ${String.format("%.2f", this)}"
    }
}

fun Int.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%d:%02d", minutes, seconds)
        else -> String.format("0:%02d", seconds)
    }
}

fun Uri.openInBrowser(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, this)
    context.startActivity(intent)
}

fun String.shareText(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, this@shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

@Composable
fun <T> DebouncedState(
    initialValue: T,
    debounceMs: Long = Constants.DEBOUNCE_MS
): Pair<T, (T) -> Unit> {
    var value by remember { mutableStateOf(initialValue) }
    var debouncedValue by remember { mutableStateOf(initialValue) }

    LaunchedEffect(value) {
        delay(debounceMs)
        debouncedValue = value
    }

    return debouncedValue to { newValue -> value = newValue }
}

@Composable
fun rememberDebouncedQuery(
    query: String,
    debounceMs: Long = Constants.DEBOUNCE_MS
): String {
    var debouncedQuery by remember { mutableStateOf(query) }

    LaunchedEffect(query) {
        delay(debounceMs)
        debouncedQuery = query
    }

    return debouncedQuery
}

fun <T> List<T>.Chunked(size: Int): List<List<T>> {
    if (size <= 0) throw IllegalArgumentException("Chunk size must be greater than 0")
    val chunks = mutableListOf<List<T>>()
    var index = 0
    while (index < this.size) {
        chunks.add(this.subList(index, minOf(index + size, this.size)))
        index += size
    }
    return chunks
}

fun String.isNumeric(): Boolean {
    return this.all { it.isDigit() }
}

fun String.containsCaseInsensitive(other: String): Boolean {
    return this.contains(other, ignoreCase = true)
}

fun <K, V> Map<K, V>.toQueryParams(): String {
    return this.entries.joinToString("&") { "${it.key}=${it.value}" }
}
