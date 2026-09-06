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
        val escapedCode = initialCode.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
        val escapedQuestion = initialQuestion.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
        wv.post {
            wv.evaluateJavascript(
                "if (window.loadPayload) { window.loadPayload('$escapedCode', '$initialLanguage', '$escapedQuestion'); }",
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
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    // Register Javascript bridge
                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onReady() {
                                val escapedCode = initialCode.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
                                val escapedQuestion = initialQuestion.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
                                post {
                                    evaluateJavascript(
                                        "if (window.loadPayload) { window.loadPayload('$escapedCode', '$initialLanguage', '$escapedQuestion'); }",
                                        null
                                    )
                                }
                            }

                            @JavascriptInterface
                            fun executeCommand(cmd: String) {
                                scope.launch {
                                    UbuntuTerminalEngine.executeCommand(context, cmd) { output ->
                                        val esc = output.text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
                                        post {
                                            evaluateJavascript(
                                                "window.appendTerminalLine('$esc', ${output.isError});",
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
                                    val esc = reply.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "")
                                    post {
                                        evaluateJavascript(
                                            "if (window.onMistralResponse) { window.onMistralResponse('$esc'); }",
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
                            return false
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
}
