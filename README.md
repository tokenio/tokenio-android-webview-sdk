# TokenTestAndroid SDK Integration Guide

This guide will help you quickly integrate the TokenTestAndroid payment SDK into your own Android app with minimal effort and maximum clarity.

---

## 1. Overview

The SDK simplifies the process of initiating payments, handling redirects and callbacks, and retrieving payment results. Integration requires only a few lines of code—no need to manage networking, WebView logic, or result parsing yourself.

**Important:**

> **Do NOT implement your own WebView for payment flows. Always use the provided `PaymentWebViewActivity` for all payment operations. This ensures security, compatibility, and a seamless user experience.**

---

## 2. Prerequisites

- Android app targeting API 23+
- Add the SDK module or source files to your project
- Add required dependencies (see below)

---

## 3. Dependencies

Add these to your `build.gradle`:

```groovy
implementation 'com.squareup.moshi:moshi:1.14.0'
implementation 'com.squareup.moshi:moshi-kotlin:1.14.0'
implementation 'com.squareup.okhttp3:okhttp:4.9.3'
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-moshi:2.9.0'
```

---

## 4. Manifest Setup

Add the payment callback scheme and host to your `AndroidManifest.xml`:

```xml
<activity android:name=".PaymentWebViewActivity" />
<activity android:name=".PaymentResultActivity" />

<!-- Deep link intent filter for payment callback (must be on MainActivity) -->
<activity android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="paymentdemoapp" android:host="payment-complete" />
    </intent-filter>
</activity>
```

**Note:** The intent filter for `paymentdemoapp://payment-complete` must be declared on `MainActivity` to ensure the app can receive callbacks from both the in-app WebView and external browsers.

---

## 5. Minimal Integration Example

```kotlin
// 1. Prepare the payment request
val paymentRequest = PaymentRequest(
    initiation = Initiation(
        refId = "YOUR_REF_ID",
        flowType = "FULL_HOSTED_PAGES",
        remittanceInformationPrimary = "Invoice #123",
        remittanceInformationSecondary = "Payment for Goods",
        amount = Amount(value = "100.00", currency = "GBP"),
        localInstrument = "FASTER_PAYMENTS",
        creditor = Creditor(
            name = "Recipient Name",
            sortCode = "123456",
            accountNumber = "12345678"
        ),
        callbackUrl = Constants.CALLBACK_URL,
        callbackState = "random-state-string"
    ),
    pispConsentAccepted = true
)

// 2. Register an ActivityResultLauncher in your Activity
private lateinit var paymentWebViewLauncher: ActivityResultLauncher<Intent>

paymentWebViewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    PaymentSdk.handleWebViewResult(result.resultCode, result.data) { paymentResult ->
        when (paymentResult) {
            is PaymentResult.Success -> {
                // Handle successful payment
            }
            is PaymentResult.Failure -> {
                // Handle failure
            }
            is PaymentResult.Cancelled -> {
                // Handle cancellation
            }
        }
    }
}

// 3. Start the payment flow (e.g. on button click)
PaymentSdk.startPaymentFlow(
    activity = this,
    paymentRequest = paymentRequest,
    launcher = paymentWebViewLauncher
) { paymentResult ->
    // Optionally handle result here as well
}
```

---

## 6. Payment Result Handling

The SDK now robustly handles payment result callbacks from both the in-app WebView and external browsers:

- If the payment provider redirects inside the WebView, the SDK intercepts the callback and shows the result screen.
- If the provider opens the callback in an external browser (common for some banks/providers), the app will still receive the callback via the intent filter on `MainActivity` and show the result screen.
- The user will always see a payment result screen after returning from the bank, regardless of callback method or whether a payment ID is present in the callback URI.

The SDK delivers one of the following results via callback:
- `PaymentResult.Success(paymentId: String)`
- `PaymentResult.Failure(error: String)`
- `PaymentResult.Cancelled`

You can use the `paymentId` to poll for payment status if desired.

---

## 7. Customization & Advanced Usage

- Customize the `PaymentRequest` as needed for your use case (EUR/GBP, IBAN, etc.).
- To display payment status, use the provided `PaymentResultActivity` or your own implementation.
- All networking and redirect logic is handled internally for you.

---

## 7a. Troubleshooting & Testing Payment Callbacks

- **Testing WebView callbacks:** The result screen should appear automatically after payment if the provider redirects in-app.
- **Testing browser callbacks:** If the provider opens the callback in an external browser, your app will still receive the callback and show the result screen (thanks to the intent filter on MainActivity).
- **Logs:** Detailed logs are written for all callback events. If the result screen does not appear, check Logcat for messages from `UnifiedWebViewClient`, `PaymentWebViewActivity`, and `MainActivity`.
- **Manual test:** You can manually test the callback by opening a URL like `paymentdemoapp://payment-complete?payment-id=test123` in your device browser.

---

## 8. Security & Environment

- API keys and environment selection are managed via the `Constants` object.
- Never hard-code production API keys in your app. Use secure storage or build config fields.
