package com.vastavik.codeoss

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var initialCode: String = ""
    private var initialLanguage: String = "Python"
    private var initialQuestion: String = ""
    private var initialAction: String = "EDIT"
    private var webViewInstance: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent(intent)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF1E1E1E),
                    surface = Color(0xFF252526),
                    primary = Color(0xFF007ACC)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E))
                ) {
                    CodeOssWebView(
                        onWebViewReady = { webViewInstance = it },
                        initialCode = initialCode,
                        initialLanguage = initialLanguage,
                        initialQuestion = initialQuestion
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
        injectPayloadIntoWebView()
    }

    private fun parseIntent(intent: Intent?) {
        if (intent == null) return
        initialCode = intent.getStringExtra("extra_code") ?: ""
        initialLanguage = intent.getStringExtra("extra_language") ?: "Python"
        initialQuestion = intent.getStringExtra("extra_question") ?: ""
        initialAction = intent.getStringExtra("extra_action") ?: "EDIT"
        val passedKey = intent.getStringExtra("extra_mistral_api_key")
        if (!passedKey.isNullOrBlank()) {
            MistralAiClient.apiKey = passedKey
        }
    }

    private fun injectPayloadIntoWebView() {
        val wv = webViewInstance ?: return
        val qCode = org.json.JSONObject.quote(initialCode)
        val qLang = org.json.JSONObject.quote(initialLanguage)
        val qQuestion = org.json.JSONObject.quote(initialQuestion)
        wv.post {
            wv.evaluateJavascript(
                "if (window.loadPayload) { window.loadPayload($qCode, $qLang, $qQuestion); }",
                null
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun CodeOssWebView(
        onWebViewReady: (WebView) -> Unit,
        initialCode: String,
        initialLanguage: String,
        initialQuestion: String
    ) {
        val context = this
        val scope = rememberCoroutineScope()

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        javaScriptCanOpenWindowsAutomatically = false
                    }

                    // Register Javascript bridge
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onReady() {
                                val qCode = org.json.JSONObject.quote(initialCode)
                                val qLang = org.json.JSONObject.quote(initialLanguage)
                                val qQuestion = org.json.JSONObject.quote(initialQuestion)
                                post {
                                    evaluateJavascript(
                                        "if (window.loadPayload) { window.loadPayload($qCode, $qLang, $qQuestion); }",
                                        null
                                    )
                                }
                            }

                            @JavascriptInterface
                            fun isCodeServerOnline(): Boolean {
                                return UbuntuTerminalEngine.isCodeServerRunning()
                            }

                            @JavascriptInterface
                            fun connectCodeServer(serverUrl: String) {
                                val target = if (serverUrl.isBlank()) "http://127.0.0.1:8080/" else serverUrl
                                val parsed = try { android.net.Uri.parse(target) } catch (_: Exception) { null }
                                val host = parsed?.host?.lowercase() ?: ""
                                if (host != "127.0.0.1" && host != "localhost") {
                                    android.util.Log.e("CodeOSS", "Blocked external host connection: $host")
                                    return
                                }
                                post {
                                    loadUrl(target)
                                }
                            }

                            @JavascriptInterface
                            fun loadOfflineIde() {
                                post {
                                    loadUrl("file:///android_asset/vscode/index.html")
                                }
                            }

                            @JavascriptInterface
                            fun listFiles(): String {
                                val list = UbuntuTerminalEngine.listWorkspaceFiles(context)
                                val jsonArray = list.joinToString(prefix = "[", postfix = "]") { f ->
                                    "{\"name\":\"${f["name"]}\",\"isDirectory\":${f["isDirectory"]},\"size\":${f["size"]}}"
                                }
                                return jsonArray
                            }

                            @JavascriptInterface
                            fun createFile(filename: String): Boolean {
                                return UbuntuTerminalEngine.createFile(context, filename)
                            }

                            @JavascriptInterface
                            fun deleteFile(filename: String): Boolean {
                                return UbuntuTerminalEngine.deleteFile(context, filename)
                            }

                            @JavascriptInterface
                            fun executeCommand(cmd: String) {
                                scope.launch {
                                    UbuntuTerminalEngine.executeCommand(context, cmd) { output ->
                                        val qEsc = org.json.JSONObject.quote(output.text)
                                        post {
                                            evaluateJavascript(
                                                "window.appendTerminalLine($qEsc, ${output.isError});",
                                                null
                                            )
                                        }
                                    }
                                }
                            }

                            @JavascriptInterface
                            fun readFile(filename: String): String {
                                return UbuntuTerminalEngine.readFileContent(context, filename)
                            }

                            @JavascriptInterface
                            fun saveFile(filename: String, content: String): Boolean {
                                return UbuntuTerminalEngine.saveFileContent(context, filename, content)
                            }

                            @JavascriptInterface
                            fun askMistral(userPrompt: String, codeContext: String, lang: String) {
                                scope.launch {
                                    val reply = MistralAiClient.queryMistral(
                                        userPrompt = userPrompt,
                                        codeContext = codeContext,
                                        language = lang
                                    )
                                    val qEsc = org.json.JSONObject.quote(reply)
                                    post {
                                        evaluateJavascript(
                                            "if (window.onMistralResponse) { window.onMistralResponse($qEsc); }",
                                            null
                                        )
                                    }
                                }
                            }
                        },
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url?.toString() ?: return false
                            // Only allow internal assets and local code-server
                            if (url.startsWith("file:///android_asset/") ||
                                url.startsWith("http://127.0.0.1:8080") ||
                                url.startsWith("http://localhost:8080")
                            ) {
                                return false
                            }
                            // Open any external web links safely in the system browser
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, request.url)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            return true
                        }
                    }

                    // Load offline client-side VS Code Web IDE
                    loadUrl("file:///android_asset/vscode/index.html")
                    onWebViewReady(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    override fun onBackPressed() {
        val wv = webViewInstance
        if (wv != null && wv.url != null && !wv.url!!.startsWith("file:///android_asset/vscode/index.html")) {
            wv.loadUrl("file:///android_asset/vscode/index.html")
        } else if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
