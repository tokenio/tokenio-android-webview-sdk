package com.example.paymentdemoandroid

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/**
 * Unified WebViewClient for handling SDK redirects, callbacks, and external links.
 * Use this client in both PaymentWebViewActivity and WebViewActivity for consistency and maintainability.
 */
class UnifiedWebViewClient(
    private val context: Context,
    private val callbackScheme: String,
    private val callbackHost: String,
    private val allowedHost: String?, // can be null if not restricting to a specific domain
    private val onCallback: (Uri?) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url
        android.util.Log.i("UnifiedWebViewClient", "Intercepted URL: ${uri}")
        if (uri != null) {
            if (uri.scheme == callbackScheme && uri.host == callbackHost) {
                android.util.Log.i("UnifiedWebViewClient", "onCallback called with URI: ${uri}")
                onCallback(uri)
                return true
            }
        }

        // 2. Internal domain (allow in WebView)
        if ((uri?.scheme == "http" || uri?.scheme == "https") && allowedHost != null && uri.host == allowedHost) {
            return false
        }

        // 3. All other URLs (external redirect)
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Cannot open link: No app found to handle it.", Toast.LENGTH_SHORT).show()
            true
        }
    }
}
