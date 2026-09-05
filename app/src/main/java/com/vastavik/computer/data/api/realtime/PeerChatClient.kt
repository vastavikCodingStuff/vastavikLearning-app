package com.vastavik.computer.data.api.realtime

import com.vastavik.computer.data.api.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeerChatClient @Inject constructor(
    private val client: OkHttpClient
) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    var onMessageReceived: ((sender: String, message: String) -> Unit)? = null
    var onConnectionStateChanged: ((isConnected: Boolean) -> Unit)? = null

    fun connect(
        roomId: String = "general_coding",
        userId: String,
        wsUrl: String = ApiConfig.WS_BASE_URL
    ) {
        val cleanWsUrl = wsUrl.removeSuffix("/")
        val request = Request.Builder()
            .url("$cleanWsUrl/ws/peer-chat?room=$roomId&user_id=$userId")
            .build()
        webSocket = client.newWebSocket(request, this)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val json = JSONObject().apply {
            put("type", "MESSAGE")
            put("text", text)
        }
        webSocket?.send(json.toString())
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        onConnectionStateChanged?.invoke(true)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val json = JSONObject(text)
            if (json.optString("type") == "MESSAGE") {
                onMessageReceived?.invoke(
                    json.optString("sender_id", "Fellow Student"),
                    json.optString("text", "")
                )
            }
        } catch (_: Exception) {
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        onConnectionStateChanged?.invoke(false)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        onConnectionStateChanged?.invoke(false)
    }

    fun disconnect() {
        webSocket?.close(1000, "User Left")
        webSocket = null
        onConnectionStateChanged?.invoke(false)
    }
}
