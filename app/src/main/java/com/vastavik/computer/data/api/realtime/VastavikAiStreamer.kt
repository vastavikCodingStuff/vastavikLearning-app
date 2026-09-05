package com.vastavik.computer.data.api.realtime

import com.vastavik.computer.data.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VastavikAiStreamer @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    fun streamChat(
        prompt: String,
        model: String = "mistral-god",
        baseUrl: String = ApiConfig.BASE_URL.removeSuffix("/")
    ): Flow<String> = flow {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val encodedModel = URLEncoder.encode(model, "UTF-8")
        val request = Request.Builder()
            .url("$baseUrl/api/v1/ai/chat/stream?prompt=$encodedPrompt&model=$encodedModel")
            .header("Accept", "text/event-stream")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Stream failed with HTTP ${response.code}")

            val reader = response.body?.byteStream()?.bufferedReader() ?: return@flow
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line
                if (currentLine != null && currentLine.startsWith("data: ")) {
                    val jsonStr = currentLine.substring(6).trim()
                    if (jsonStr == "[DONE]") break

                    try {
                        val json = JSONObject(jsonStr)
                        val delta = json.optString("delta_text", "")
                        val isFinished = json.optBoolean("is_finished", false)

                        if (delta.isNotEmpty()) {
                            emit(delta)
                        }
                        if (isFinished) {
                            break
                        }
                    } catch (_: Exception) {
                        if (jsonStr.isNotBlank()) emit(jsonStr)
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
