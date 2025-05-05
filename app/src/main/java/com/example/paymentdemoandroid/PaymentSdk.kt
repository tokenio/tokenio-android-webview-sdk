package com.example.paymentdemoandroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentdemoandroid.model.PaymentRequest
import com.example.paymentdemoandroid.model.PaymentResponse
import com.example.paymentdemoandroid.repository.PaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Result of the payment flow, delivered via callback.
 */
sealed class PaymentResult {
    data class Success(val paymentId: String) : PaymentResult()
    data class Failure(val error: String) : PaymentResult()
    object Cancelled : PaymentResult()
}

/**
 * Main entry point for integrating the payment SDK into your app.
 * Use [startPaymentFlow] to initiate a payment and [handleWebViewResult] to process results.
 */
object PaymentSdk {
    private const val TAG = "PaymentSdk"

    /**
     * Launches the payment flow: initiates payment, opens WebView, and delivers result via callback.
     *
     * @param activity The Activity context to use for launching flows.
     * @param paymentRequest The payment request object.
     * @param launcher The ActivityResultLauncher for launching PaymentWebViewActivity.
     * @param onResult Callback delivering the payment result.
     */
    /**
     * Initiates the payment flow: creates payment, opens WebView, and delivers result via callback.
     *
     * @param activity The Activity context to use for launching flows.
     * @param paymentRequest The payment request object.
     * @param launcher The ActivityResultLauncher for launching PaymentWebViewActivity.
     * @param onResult Callback delivering the payment result.
     */
    fun startPaymentFlow(
        activity: AppCompatActivity,
        paymentRequest: PaymentRequest,
        launcher: ActivityResultLauncher<Intent>,
        onResult: (PaymentResult) -> Unit
    ) {
        // Launch payment initiation in coroutine
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val repository = PaymentRepository()
                val response = repository.initiatePayment(paymentRequest)
                if (response.isSuccessful && response.body() != null) {
                    val paymentResponse = response.body()!!
                    val paymentUrl = paymentResponse.payment.authentication.redirectUrl
                    // Launch the WebView flow
                    val intent = Intent(activity, PaymentWebViewActivity::class.java)
                    intent.putExtra(PaymentWebViewActivity.EXTRA_URL, paymentUrl)
                    launcher.launch(intent)
                    // The result will be handled in ActivityResult callback (see below)
                } else {
                    val errorMsg = "Failed to initiate payment. Code: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    onResult(PaymentResult.Failure(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during payment initiation", e)
                onResult(PaymentResult.Failure("Error initiating payment: ${e.localizedMessage}"))
            }
        }
    }

    /**
     * Handles the callback from PaymentWebViewActivity and delivers the result.
     * Call this from your ActivityResultLauncher callback.
     */
    /**
     * Handles the callback from PaymentWebViewActivity and delivers the result.
     * Call this from your ActivityResultLauncher callback.
     */
    fun handleWebViewResult(resultCode: Int, data: Intent?, onResult: (PaymentResult) -> Unit) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                // The payment was completed, extract payment ID from callback URI
                val uri: Uri? = data?.data
                val errorParam = uri?.getQueryParameter("error")
                val messageParam = uri?.getQueryParameter("message")
                if (errorParam == "access_denied" || messageParam?.contains("User Cancelled", ignoreCase = true) == true) {
                    onResult(PaymentResult.Cancelled)
                } else {
                    val paymentId = uri?.getQueryParameter("payment-id")
                    if (paymentId != null) {
                        onResult(PaymentResult.Success(paymentId))
                    } else {
                        onResult(PaymentResult.Failure("Payment completed but payment ID missing in callback."))
                    }
                }
            }
            PaymentWebViewActivity.RESULT_PAYMENT_CANCELLED -> {
                onResult(PaymentResult.Cancelled)
            }
            else -> {
                onResult(PaymentResult.Failure("Payment flow failed or was cancelled."))
            }
        }
    }
}
