package pl.jantar.kadryonline

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
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

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.WHITE)

        status = TextView(this)
        status.text = "Uruchamianie Jantar Kadry..."
        status.textSize = 14f
        status.setTextColor(Color.BLACK)
        status.setPadding(12, 12, 12, 12)

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
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
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
                    "JavaScript: ${consoleMessage.message()}"
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                status.text = "Ładowanie: $url"
            }

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                status.text = "Adres: $url"
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    status.text =
                        "BŁĄD STRONY:\n" +
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
                status.text =
                    "BŁĄD SSL:\n${error?.toString()}"
                handler?.cancel()
            }
        }

        status.text = "Otwieranie portalu..."
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
