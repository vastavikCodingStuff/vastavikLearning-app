package com.vastavik.codeoss

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private var initialCode: String = ""
    private var initialLanguage: String = "Python"
    private var initialQuestion: String = ""
    private var initialAction: String = "EDIT"
    private var webViewInstance: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent(intent)
        setupWebView()
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
        val qCode = JSONObject.quote(initialCode)
        val qLang = JSONObject.quote(initialLanguage)
        val qQuestion = JSONObject.quote(initialQuestion)
        wv.post {
            wv.evaluateJavascript(
                "if (window.loadPayload) { window.loadPayload($qCode, $qLang, $qQuestion); }",
                null
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val context = this
        val scope = lifecycleScope

        // Enable debugging for devtools / chrome://inspect
        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (_: Exception) {}

        val wv = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#181818"))

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                useWideViewPort = false
                loadWithOverviewMode = false
                cacheMode = WebSettings.LOAD_NO_CACHE
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                mediaPlaybackRequiresUserGesture = false
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                defaultTextEncodingName = "UTF-8"
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d(
                        "CodeOSS_Web",
                        "${consoleMessage?.message()} [${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()}]"
                    )
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("CodeOSS", "Page loaded: $url")
                    injectPayloadIntoWebView()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e(
                        "CodeOSS",
                        "WebView error: ${error?.description} (${error?.errorCode}) for ${request?.url}"
                    )
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // Only allow internal assets and local code-server
                    if (url.startsWith("file:///android_asset/") ||
                        url.startsWith("http://127.0.0.1") ||
                        url.startsWith("http://localhost")
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

            // Register Javascript bridge
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onReady() {
                        val qCode = JSONObject.quote(initialCode)
                        val qLang = JSONObject.quote(initialLanguage)
                        val qQuestion = JSONObject.quote(initialQuestion)
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
                            Log.e("CodeOSS", "Blocked external host connection: $host")
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
                                val qEsc = JSONObject.quote(output.text)
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
                            val qEsc = JSONObject.quote(reply)
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

            loadUrl("file:///android_asset/vscode/index.html")
        }

        webViewInstance = wv
        setContentView(wv)
    }

    override fun onResume() {
        super.onResume()
        webViewInstance?.onResume()
    }

    override fun onPause() {
        webViewInstance?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webViewInstance?.destroy()
        webViewInstance = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val wv = webViewInstance
        if (wv != null && wv.url != null && !wv.url!!.startsWith("file:///android_asset/vscode/index.html")) {
            wv.loadUrl("file:///android_asset/vscode/index.html")
        } else if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
