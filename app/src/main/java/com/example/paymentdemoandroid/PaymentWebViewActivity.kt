package com.example.paymentdemoandroid

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import com.example.paymentdemoandroid.databinding.ActivityPaymentWebviewBinding

class PaymentWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentWebviewBinding
    private var initialPaymentUrl: String? = null
    private var initialPaymentHost: String? = null

    companion object {
        const val EXTRA_URL = "extra_url"
        const val RESULT_PAYMENT_CANCELLED = 2
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialPaymentUrl = intent.getStringExtra(EXTRA_URL)

        if (initialPaymentUrl == null) {
            Log.e("PaymentWebView", "Error: No URL provided in intent extras.")
            Toast.makeText(this, "Error: Invalid payment URL", Toast.LENGTH_LONG).show()
            finish() // Close the activity if no URL is provided
            return
        }

        // Extract host from the initial URL for comparison later
        try {
            initialPaymentHost = Uri.parse(initialPaymentUrl).host
            Log.i("PaymentWebView", "Initial payment host: $initialPaymentHost")
        } catch (e: Exception) {
            Log.e("PaymentWebView", "Error parsing initial URL host: $initialPaymentUrl", e)
            Toast.makeText(this, "Error: Invalid initial payment URL format", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.i("PaymentWebView", "Loading initial URL: $initialPaymentUrl")

        // Configure WebView settings
        binding.webView.settings.javaScriptEnabled = true // Enable JavaScript if required by the payment page
        binding.webView.settings.domStorageEnabled = true // Enable DOM storage if needed
        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // Adjust if needed

        android.util.Log.i("PaymentWebViewActivity", "onCreate: intent.action=${intent.action}, intent.data=${intent.data}")
        binding.webView.webViewClient = UnifiedWebViewClient(
            context = this,
            callbackScheme = Constants.CALLBACK_SCHEME,
            callbackHost = Constants.CALLBACK_HOST,
            allowedHost = initialPaymentHost,
            onCallback = { uri: Uri? ->
                android.util.Log.i("PaymentWebViewActivity", "onCallback received URI: $uri")
                // Parse the callback URI for payment result
                val errorParam = uri?.getQueryParameter("error")
                val messageParam = uri?.getQueryParameter("message")
                val paymentId = uri?.getQueryParameter("payment-id")
                val resultIntent = Intent().apply { data = uri }
                when {
                    errorParam == "access_denied" || messageParam?.contains("User Cancelled", ignoreCase = true) == true -> {
                        android.util.Log.i("PaymentWebViewActivity", "setResult: RESULT_PAYMENT_CANCELLED for URI: $uri")
                        setResult(RESULT_PAYMENT_CANCELLED, resultIntent)
                    }
                    else -> {
                        android.util.Log.i("PaymentWebViewActivity", "setResult: RESULT_OK for URI: $uri")
                        // Always treat as RESULT_OK for any non-cancellation callback
                        setResult(RESULT_OK, resultIntent)
                    }
                }
                finish()
            }
        )
        // Load the initial URL
        binding.webView.loadUrl(initialPaymentUrl!!)

        // Modern back press handling
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        Log.d("PaymentWebView", "Navigating back in WebView history.")
                        binding.webView.goBack()
                    } else {
                        Log.d("PaymentWebView", "User cancelled payment via back button.")
                        setResult(RESULT_PAYMENT_CANCELLED)
                        finish()
                    }
                }
            }
        )
    }

}