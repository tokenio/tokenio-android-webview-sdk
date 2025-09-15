# Token.io WebView Integration for Android

This guide shows you how to integrate Token.io's web-based payment flows into your existing Android payment interface using a secure WebView implementation.

**Perfect for developers who already have a payment interface and just need to launch Token.io's web app securely.**

---

## 📋 Table of Contents

1. [Quick Overview](#quick-overview)
2. [What You Need](#what-you-need)
3. [WebView Components](#webview-components)
4. [Integration Steps](#integration-steps)
5. [Launching Token.io Web App](#launching-tokenio-web-app)
6. [Handling Payment Results](#handling-payment-results)
7. [Troubleshooting WebView Issues](#troubleshooting-webview-issues)

---

## 🎯 Quick Overview

If you already have a payment interface, you only need to:

1. **Add a secure WebView component** to launch Token.io's web app
2. **Handle payment callbacks** from the web app back to your app
3. **Process payment results** in your existing flow

**⚠️ Security First:**
> Use the provided WebView implementation - it handles security, SSL certificates, and proper callback routing that a basic WebView cannot.

---

## 🛠️ What You Need

Just **3 core files** and **2 simple steps**:

### Files to Copy:
1. **`PaymentWebViewActivity.kt`** - The secure WebView that launches Token.io
2. **`UnifiedWebViewClient.kt`** - Handles payment callbacks and redirects
3. **`activity_payment_webview.xml`** - WebView layout

### Steps:
1. **Add WebView Activity to your manifest**
2. **Launch WebView from your existing payment flow**

That's it! 🎉

---

## 📱 WebView Components

### Core WebView Files You Need:

```
📁 From this demo project:
├── PaymentWebViewActivity.kt        # Main WebView Activity
├── UnifiedWebViewClient.kt          # Callback Handler  
└── res/layout/activity_payment_webview.xml  # WebView Layout
```

**What each file does:**
- **`PaymentWebViewActivity`**: Secure WebView that loads Token.io's web app
- **`UnifiedWebViewClient`**: Intercepts payment success/failure callbacks
- **`activity_payment_webview.xml`**: Simple WebView layout with progress bar

---

## ⚡ Integration Steps

### Step 1: Add the WebView Activity

Add to your `AndroidManifest.xml`:

```xml
<activity
    android:name=".PaymentWebViewActivity"
    android:exported="false"
    android:screenOrientation="portrait" />
```

### Step 2: Launch Token.io from Your Payment Flow

From your existing payment button/method:

```kotlin
// In your existing payment activity/fragment
private lateinit var tokenWebViewLauncher: ActivityResultLauncher<Intent>

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Set up the WebView launcher
    tokenWebViewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleTokenResult(result)
    }
}

// When user clicks your "Pay with Bank" button
private fun launchTokenWebView() {
    val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
        putExtra("PAYMENT_URL", "https://your-token-payment-url.com")
        putExtra("CALLBACK_SCHEME", "yourapp")  // Your app's callback scheme
    }
    tokenWebViewLauncher.launch(intent)
}

private fun handleTokenResult(result: ActivityResult) {
    when (result.resultCode) {
        Activity.RESULT_OK -> {
            val paymentId = result.data?.getStringExtra("PAYMENT_ID")
            // Payment successful - continue your flow
            onPaymentSuccess(paymentId)
        }
        Activity.RESULT_CANCELED -> {
            val error = result.data?.getStringExtra("ERROR_MESSAGE")
            // Payment failed - handle error
            onPaymentFailed(error)
        }
    }
}
```

---

## 🚀 Launching Token.io Web App

### Option A: With Your Token.io Payment URL

If you already generate Token.io payment URLs:

```kotlin
private fun launchTokenPayment(tokenPaymentUrl: String) {
    val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
        putExtra("PAYMENT_URL", tokenPaymentUrl)
        putExtra("CALLBACK_SCHEME", "yourapp")
        putExtra("TITLE", "Complete Payment")  // Optional: Custom title
    }
    tokenWebViewLauncher.launch(intent)
}
```

### Option B: Generate URL and Launch

If you need to create the payment first:

```kotlin
private fun startTokenPayment(amount: String, currency: String, recipientName: String) {
    // Your existing payment creation logic
    val paymentUrl = createTokenPayment(amount, currency, recipientName)
    
    // Launch WebView with the URL
    launchTokenPayment(paymentUrl)
}
```

### Option C: Generate Payment Session and Launch WebView

To create a payment session and get the web app URL:

```kotlin
// Add these dependencies for Token.io API calls:
// implementation 'com.squareup.retrofit2:retrofit:2.9.0'
// implementation 'com.squareup.retrofit2:converter-moshi:2.9.0'
// implementation 'com.squareup.moshi:moshi-kotlin:1.15.0'

// 1. Set up API service
interface TokenApiService {
    @POST("payment-requests")
    suspend fun createPaymentRequest(
        @Header("Authorization") authorization: String,
        @Body paymentRequest: PaymentRequest
    ): PaymentResponse
}

// 2. Create payment session and launch WebView
private fun createPaymentSessionAndLaunch() {
    lifecycleScope.launch {
        try {
            // Create payment request with proper locale formatting
            val paymentRequest = PaymentRequest(
                initiation = Initiation(
                    refId = UUID.randomUUID().toString(),
                    flowType = "FULL_HOSTED_PAGES",
                    remittanceInformationPrimary = "Payment for services",
                    remittanceInformationSecondary = "Order #${System.currentTimeMillis()}",
                    amount = Amount(
                        value = formatAmountForApi(100.50), // Always use English locale
                        currency = "GBP" // or "EUR"
                    ),
                    localInstrument = "FASTER_PAYMENTS", // or "SEPA_CREDIT_TRANSFER"
                    creditor = Creditor(
                        name = "Your Business Name",
                        sortCode = "123456",        // For UK payments
                        accountNumber = "12345678"  // For UK payments
                        // iban = "GB29NWBK60161331926819" // For EUR/SEPA payments
                    ),
                    callbackUrl = "yourapp://payment-complete",
                    callbackState = UUID.randomUUID().toString()
                ),
                pispConsentAccepted = true
            )

            // Call Token.io API to create payment session
            val response = tokenApiService.createPaymentRequest(
                authorization = "Bearer ${BuildConfig.TOKEN_API_KEY}",
                paymentRequest = paymentRequest
            )
            
            // Launch WebView with the payment URL
            launchTokenPayment(response.paymentUrl)
            
        } catch (e: Exception) {
            Log.e("Payment", "Failed to create payment session", e)
            handleApiError(e)
        }
    }
}

// 3. Helper function to ensure English locale for amounts
private fun formatAmountForApi(amount: Double): String {
    return String.format(Locale.ENGLISH, "%.2f", amount)
}

// 4. Data classes for API communication
@JsonClass(generateAdapter = true)
data class PaymentRequest(
    val initiation: Initiation,
    val pispConsentAccepted: Boolean
)

@JsonClass(generateAdapter = true)
data class Initiation(
    val refId: String,
    val flowType: String,
    val remittanceInformationPrimary: String,
    val remittanceInformationSecondary: String,
    val amount: Amount,
    val localInstrument: String,
    val creditor: Creditor,
    val callbackUrl: String,
    val callbackState: String
)

@JsonClass(generateAdapter = true)
data class Amount(
    val value: String,    // Always formatted with English locale (e.g., "100.50")
    val currency: String  // "GBP", "EUR", etc.
)

@JsonClass(generateAdapter = true)
data class Creditor(
    val name: String,
    val sortCode: String? = null,      // For UK payments
    val accountNumber: String? = null, // For UK payments  
    val iban: String? = null          // For EUR/SEPA payments
)

@JsonClass(generateAdapter = true)
data class PaymentResponse(
    val paymentUrl: String,
    val paymentId: String,
    val status: String
)
```

---

## 🔄 Handling Payment Results

The WebView will return results to your app in two ways:

### In-App Results (Most Common)
When payment completes within the WebView:

```kotlin
private fun handleTokenResult(result: ActivityResult) {
    when (result.resultCode) {
        Activity.RESULT_OK -> {
            // ✅ Payment successful
            val paymentId = result.data?.getStringExtra("PAYMENT_ID")
            val amount = result.data?.getStringExtra("AMOUNT")
            
            // Continue your app's payment flow
            showPaymentSuccess(paymentId, amount)
            navigateToConfirmationScreen()
        }
        
        Activity.RESULT_CANCELED -> {
            // ❌ Payment failed or cancelled
            val error = result.data?.getStringExtra("ERROR_MESSAGE")
            
            if (error != null) {
                showPaymentError(error)
            } else {
                showPaymentCancelled()
            }
        }
    }
}
```

### External Browser Results (For Some Banks)
Some banks redirect outside the app. Handle these with a deep link:

**1. Add to your `AndroidManifest.xml`:**
```xml
<activity android:name=".YourPaymentActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="yourapp" android:host="payment-complete" />
    </intent-filter>
</activity>
```

**2. Handle the callback:**
```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    
    if (intent.data?.scheme == "yourapp") {
        val paymentId = intent.data?.getQueryParameter("payment-id")
        val error = intent.data?.getQueryParameter("error")
        
        when {
            paymentId != null -> onPaymentSuccess(paymentId)
            error != null -> onPaymentFailed(error)
            else -> onPaymentCancelled()
        }
    }
}
```

---

## 🎯 WebView Flow Summary

```
Your Payment Button
        ↓
Launch PaymentWebViewActivity 
        ↓
Token.io Web App loads
        ↓
User completes bank payment
        ↓
Result returns to your app
        ↓
Continue your payment flow
```

**That's it! 🎉 Your existing payment interface can now launch Token.io's secure web app.**

---

## 🔍 Troubleshooting WebView Issues

### WebView Not Loading:
```kotlin
// Check these in your PaymentWebViewActivity:
webView.settings.javaScriptEnabled = true
webView.settings.domStorageEnabled = true
webView.settings.loadWithOverviewMode = true
webView.settings.useWideViewPort = true
```

### Payment Callbacks Not Working:
1. **Check your callback scheme** matches in both:
   - AndroidManifest.xml: `<data android:scheme="yourapp" />`
   - WebView intent: `putExtra("CALLBACK_SCHEME", "yourapp")`

2. **Test callback manually:**
   ```bash
   adb shell am start -a android.intent.action.VIEW \
   -d "yourapp://payment-complete?payment-id=test123" \
   com.yourpackage.yourapp
   ```

### WebView SSL/Security Issues:
- The provided `UnifiedWebViewClient` handles SSL certificates
- Never disable SSL verification in production
- Ensure your app targets API 28+ for network security

### Debug WebView Loading:
```kotlin
// Add to your WebView setup for debugging
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}
```

---

## 🔐 Production Checklist

Before going live:

- [ ] **Remove debug logs** from WebView components  
- [ ] **Test on real devices** with actual bank accounts
- [ ] **Verify SSL certificates** are properly validated
- [ ] **Test both callback methods** (WebView + deep link)
- [ ] **Handle network connectivity** issues gracefully
- [ ] **Add loading states** for better UX

---

## 💰 Currency and Locale Handling

**Important:** Always use English locale for amount formatting to ensure API compatibility:

```kotlin
// ✅ Correct - English locale formatting
private fun formatAmountForApi(amount: Double): String {
    return String.format(Locale.ENGLISH, "%.2f", amount)
}

// Examples:
formatAmountForApi(100.5)   // Returns "100.50" ✅
formatAmountForApi(1000.0)  // Returns "1000.00" ✅

// ❌ Wrong - Device locale might use comma separator
String.format("%.2f", 100.5) // Could return "100,50" on some devices
```

**Supported Currencies:**
- **GBP** (UK Faster Payments): Requires `sortCode` and `accountNumber`
- **EUR** (SEPA Credit Transfer): Requires `iban`
- **Other currencies** as supported by Token.io

---

**🚀 Ready to integrate?** Copy the 3 WebView files and you're set!

**📱 Questions?** Check the demo app to see the WebView in action.
