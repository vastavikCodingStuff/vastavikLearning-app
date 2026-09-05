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
class WebRtcSignalingClient @Inject constructor(
    private val client: OkHttpClient
) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    var onSignalReceived: ((signalType: String, payloadJson: String, senderId: String) -> Unit)? = null
    var onConnectionStateChanged: ((isConnected: Boolean) -> Unit)? = null

    fun connect(
        classId: String,
        userId: String,
        role: String = "student",
        wsUrl: String = ApiConfig.WS_BASE_URL
    ) {
        val cleanWsUrl = wsUrl.removeSuffix("/")
        val request = Request.Builder()
            .url("$cleanWsUrl/ws/signaling/$classId?user_id=$userId&role=$role")
            .build()
        webSocket = client.newWebSocket(request, this)
    }

    fun sendSignal(targetId: String?, signalType: String, payloadJson: String) {
        val json = JSONObject().apply {
            put("target_id", targetId ?: "")
            put("signal_type", signalType) // "OFFER", "ANSWER", "ICE", "WHITEBOARD"
            put("payload_json", payloadJson)
        }
        webSocket?.send(json.toString())
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        onConnectionStateChanged?.invoke(true)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val json = JSONObject(text)
            val type = json.optString("signal_type")
            val payload = json.optString("payload_json")
            val sender = json.optString("sender_id")
            onSignalReceived?.invoke(type, payload, sender)
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
        webSocket?.close(1000, "Class Ended")
        webSocket = null
        onConnectionStateChanged?.invoke(false)
    }
}
