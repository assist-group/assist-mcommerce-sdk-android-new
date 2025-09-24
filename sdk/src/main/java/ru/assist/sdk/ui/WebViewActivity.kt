package ru.assist.sdk.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.BuildConfig
import ru.assist.sdk.R
import ru.assist.sdk.databinding.WebActivityBinding
import ru.assist.sdk.engine.WebProcessor
import ru.assist.sdk.scanner.CardData
import ru.assist.sdk.scanner.CardScanner
import javax.inject.Inject

internal class WebViewActivity: AppCompatActivity() {
    private val tag = "WebViewActivity"
    private val scannerRequestCode = 1
    private lateinit var binding: WebActivityBinding
    private lateinit var webView: WebView

    @Inject
    internal lateinit var webProcessor: WebProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AssistSDK.getInstance().component.inject(this)

        binding = WebActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!BuildConfig.DEBUG) window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        initUI()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initUI() {
        webView = binding.webView
        webView.apply {
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webViewClient = PayWebViewClient()
            webChromeClient = PayWebChromeClient()
        }
        postRequest()
    }

    override fun onBackPressed() =
        if (webView.canGoBack())
            webView.goBack()
        else
            showAlert()

    private fun postRequest() =
        intent.extras?.let {
            val url = it.getString("url", null)
            val content = it.getString("content", null)
            if (content.isNullOrBlank()) {
                Log.d(tag, "Load $url")
                webView.loadUrl(url) // Переходим по ссылке существующего платежа
            } else {
                Log.d(tag, "Post to $url content: ${content.take(512)}...")
                webView.postUrl(url, content.toByteArray()) // Создание нового заказа
            }
        }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showAlert() =
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dlg_title_warning))
            .setMessage(getString(R.string.dlg_msg_stop_payment_question))
            .setPositiveButton(getString(android.R.string.yes)) { _: DialogInterface?, _: Int ->
                webProcessor.stop()
                finish()
            }
            .setNegativeButton(getString(android.R.string.no)) { _: DialogInterface?, _: Int -> }
            .create()
            .show()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        when (requestCode) {
            scannerRequestCode -> {
                if (data != null) {
                    webProcessor.getCardData(data)?.let {
                        fillCardFields(it)
                    }
                }
                super.onActivityResult(requestCode, resultCode, data)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }

    private fun fillCardFields(data: CardData) {
        val script = """
            function fillForm() {
                document.getElementById('CardNumber').value = '${data.cardNumber}';
                document.getElementById('CardNumber').select();
                var month = document.getElementById('ExpireMonth');
                if (month !== null && month.getAttribute('type') != 'hidden') {
                    for (var i = 0; i < month.options.length; i++) {
                        if (month.options[i].value == '${data.cardExpMonth}') {
                            month.selectedIndex = i;
                            break;
                        }
                    }
                }
                var year = document.getElementById('ExpireYear');
                if (year !== null && year.getAttribute('type') != 'hidden') {
                    for (var i = 0; i < year.options.length; i++) {
                        if (year.options[i].value == '20${data.cardExpYear}') {
                            year.selectedIndex = i;
                            break;
                        }
                    }
                }
                var cardHolder = document.getElementById('Cardholder');
                if (cardHolder !== null) {
                    cardHolder.value = '${data.cardholderName}';
                }
                var expDate = document.getElementById('ExpireDate');
                if (expDate !== null) {
                    expDate.value = '${data.cardExpMonth}/${data.cardExpYear}';
                    expDate.select();
                }
                var cvc2 = document.getElementById('CVC2');
                if (cvc2 !== null) {
                    cvc2.select();
                }
            };
            fillForm();
        """.trimIndent()
        webView.requestFocus(View.FOCUS_DOWN)
        webView.loadUrl("javascript:$script")
    }

    inner class PayWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            Log.d(tag, "Progress: $newProgress")
            super.onProgressChanged(view, newProgress)
        }
    }

    inner class PayWebViewClient : WebViewClient() {
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            Log.e(tag, "SSL Error: $error")
            handler.cancel()
            Toast.makeText(this@WebViewActivity, R.string.ssl_error, Toast.LENGTH_LONG).show()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean =
            request.url.toString().let { url ->
                if (url.contains("result.cfm")) {
                    webProcessor.getDataFromResult("$url&type=json")
                    finish()
                    true
                } else if (url.contains("body.cfm")) {
                    webProcessor.getDataFromError(url)
                    finish()
                    true
                } else if (url.contains("mirpay://pay.mironline.ru")) {
                    startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    true
                } else
                    false
            }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            showProgress()
            webView.scrollX = 0
            webView.scrollY = 0
        }

        override fun onPageFinished(view: WebView, url: String) {
            hideProgress()
            if (url.contains("pay.cfm")) {
                webProcessor.checkScanner(url, ::launchScanner)
            }
        }

        private fun launchScanner(scanner: CardScanner) {
            val intent = scanner.getScannerIntent(this@WebViewActivity)
            if (intent != null) {
                startActivityForResult(intent, scannerRequestCode)
            }
        }
    }
}