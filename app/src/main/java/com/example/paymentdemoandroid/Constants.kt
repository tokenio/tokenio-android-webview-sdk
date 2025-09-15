package com.example.paymentdemoandroid

import com.example.paymentdemoandroid.BuildConfig

object Constants {
    const val CALLBACK_SCHEME = "paymentdemoapp"
    const val CALLBACK_HOST = "payment-complete"
    const val CALLBACK_URL = "$CALLBACK_SCHEME://$CALLBACK_HOST"

    // --- Environment Configuration ---

    enum class Environment(val displayName: String, val apiKey: String, val baseUrl: String) {
        BETA(
            "Beta",
            BuildConfig.BETA_API_KEY,
            "https://api.beta.token.io/" // Corrected Beta Base URL
        )
    }

    // Default environment - set to BETA since it's the only option
    var selectedEnvironment: Environment = Environment.BETA

    // IMPORTANT: Storing API keys directly in code is insecure for production apps.
    // Consider using BuildConfig fields or other secure methods.
}
