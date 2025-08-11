package com.example.paymentdemoandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentdemoandroid.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    companion object {
        const val EXTRA_URL = "com.example.paymentdemoandroid.URL"
        const val TAG = "WebViewActivity"
        const val CALLBACK_SCHEME = "example" // From Constants.CALLBACK_URL
        const val CALLBACK_HOST = "callback"   // From Constants.CALLBACK_URL
        // TODO: Confirm the actual domain used by Nuvei payment pages
        const val PAYMENT_PROVIDER_DOMAIN = "nuvei.com" 
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra(EXTRA_URL)

        if (url == null) {
            Log.e(TAG, "URL is missing, finishing activity.")
            finish()
            return
        }

        Log.d(TAG, "Loading URL: $url")

        // Configure WebView settings
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true // Often needed
        // Optional: Improve viewport handling
        // binding.webView.settings.useWideViewPort = true
        // binding.webView.settings.loadWithOverviewMode = true

        // Set the unified WebViewClient
        binding.webView.webViewClient = UnifiedWebViewClient(
            context = this,
            callbackScheme = CALLBACK_SCHEME,
            callbackHost = CALLBACK_HOST,
            allowedHost = PAYMENT_PROVIDER_DOMAIN,
            onCallback = {
                finish()
            }
        )

        // Load the initial URL
        binding.webView.loadUrl(url)

        // Modern back press handling
        onBackPressedDispatcher.addCallback(this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        Log.d(TAG, "Navigating back in WebView history.")
                        binding.webView.goBack()
                    } else {
                        Log.d(TAG, "User cancelled WebView via back button.")
                        finish()
                    }
                }
            }
        )
    }


}
