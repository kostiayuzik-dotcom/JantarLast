package pl.jantar.kadryonline

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.webkit.*
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var status: TextView

    private val portalUrl =
        "https://kadryonline.jantar.eu.com/xpertis/prv/view/0x915ac1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        status = TextView(this).apply {
            text = "Uruchamianie Jantar Kadry..."
            textSize = 12f
            setTextColor(Color.BLACK)
            setPadding(12, 12, 12, 12)
        }

        webView = WebView(this)

        layout.addView(status)
        layout.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(layout)

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
                consoleMessage: ConsoleMessage
            ): Boolean {
                status.text =
                    "JAVASCRIPT ERROR:\n" +
                    consoleMessage.message() +
                    "\nLine: " +
                    consoleMessage.lineNumber()

                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                status.text = "Ładowanie strony:\n$url"
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                status.text =
                    "Adres: $url\n" +
                    "Title: ${view?.title ?: "brak"}"
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    status.text =
                        "BŁĄD STRONY\n" +
                        "Kod: ${error?.errorCode}\n" +
                        "Opis: ${error?.description}\n" +
                        "Adres: ${request.url}"
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (request?.isForMainFrame == true) {
                    status.text =
                        "HTTP ERROR: ${errorResponse?.statusCode}\n" +
                        "Adres: ${request.url}"
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: android.net.http.SslError?
            ) {
                status.text = "SSL ERROR: $error"
                handler?.cancel()
            }
        }

        webView.loadUrl(portalUrl)
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }
}
