package pl.jantar.kadryonline

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.webkit.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var status: TextView

    private val portalUrl =
        "https://kadryonline.jantar.eu.com/xpertis/prv/view/0x915ac1"

    private val logs = StringBuilder()

    private fun addLog(message: String) {
        runOnUiThread {
            logs.append("\n").append(message)
            status.text = logs.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        status = TextView(this).apply {
            text = "DIAGNOSTYKA JANTAR KADRY\nUruchamianie..."
            textSize = 13f
            setTextColor(Color.BLACK)
            setPadding(16, 16, 16, 16)
        }

        val scroll = ScrollView(this)
        scroll.addView(status)

        webView = WebView(this)

        layout.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                180
            )
        )

        layout.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(layout)

        WebView.setWebContentsDebuggingEnabled(true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            mixedContentMode =
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance()
            .setAcceptThirdPartyCookies(webView, true)

        webView.webChromeClient = object : WebChromeClient() {

            override fun onConsoleMessage(
                message: ConsoleMessage
            ): Boolean {
                addLog(
                    "JS ${message.messageLevel()}: " +
                    message.message() +
                    " | line: ${message.lineNumber()}"
                )
                return true
            }

            override fun onProgressChanged(
                view: WebView?,
                progress: Int
            ) {
                addLog("Завантаження: $progress%")
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                addLog("Початок: $url")
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                addLog("Сторінка завершила завантаження.")
                addLog("Адреса: $url")

                view?.evaluateJavascript(
                    """
                    (function() {
                        return JSON.stringify({
                            readyState: document.readyState,
                            title: document.title,
                            bodyExists: !!document.body,
                            bodyTextLength: document.body ?
                                document.body.innerText.length : -1,
                            htmlLength: document.body ?
                                document.body.innerHTML.length : -1,
                            userAgent: navigator.userAgent
                        });
                    })();
                    """.trimIndent()
                ) { result ->
                    addLog("РЕЗУЛЬТАТ HTML:\n$result")
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    addLog(
                        "ПОМИЛКА: ${error?.errorCode} " +
                        "${error?.description}"
                    )
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                response: WebResourceResponse?
            ) {
                if (request?.isForMainFrame == true) {
                    addLog(
                        "HTTP: ${response?.statusCode}"
                    )
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: android.net.http.SslError?
            ) {
                addLog("SSL ПОМИЛКА: $error")
                handler?.cancel()
            }
        }

        addLog("Відкриваю портал...")
        webView.loadUrl(portalUrl)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
