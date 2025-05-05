package com.example.paymentdemoandroid

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.paymentdemoandroid.databinding.ActivityPaymentResultBinding
import com.example.paymentdemoandroid.model.PaymentStatusResponse
import com.example.paymentdemoandroid.repository.PaymentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PaymentResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentResultBinding
    private val paymentRepository = PaymentRepository()
    private var paymentId: String? = null
    private var pollingJob: Job? = null

    companion object {
        const val EXTRA_PAYMENT_ID = "extra_payment_id"
        private const val POLLING_INTERVAL_MS = 3000L // Poll every 3 seconds
        private val POLLING_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2) // Stop polling after 2 minutes
        private const val TAG = "PaymentResultActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paymentCancelled = intent.getBooleanExtra("payment_cancelled", false)
        if (paymentCancelled) {
            // Show cancelled UI, skip polling
            binding.textViewStatusValue.text = "CANCELLED"
            binding.textViewStatusValue.setTextColor(getStatusColor("CANCELLED"))
            binding.textViewErrorLabel.visibility = View.VISIBLE
            binding.textViewErrorLabel.text = "Payment Cancelled"
            binding.textViewErrorValue.visibility = View.GONE
            binding.textViewAmountLabel.visibility = View.GONE
            binding.textViewAmountValue.visibility = View.GONE
            binding.progressBarResult.visibility = View.GONE
            binding.textViewPaymentIdValue.text = "-"
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_LONG).show()
            binding.buttonCloseResult.setOnClickListener {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish()
}
            return
        }

        paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID)
        if (paymentId == null) {
            // Try to extract payment-id from callback_uri
            val callbackUriString = intent.getStringExtra("callback_uri")
            val callbackUri = callbackUriString?.let { android.net.Uri.parse(it) }
            paymentId = callbackUri?.getQueryParameter("payment-id")
            if (paymentId == null) {
                // Optionally, try to extract a reference or state parameter for fallback polling
                val stateOrRef = callbackUri?.getQueryParameter("state") ?: callbackUri?.getQueryParameter("reference")
                if (stateOrRef != null) {
                    // Optionally, you could attempt polling by state/ref here if your backend supports it
                    Log.w(TAG, "No payment-id, but found state/reference: $stateOrRef. Implement polling by reference if possible.")
                }
                // Show a user-friendly error and do not crash
                Log.e(TAG, "Error: Payment ID not found in intent extras or callback URI.")
                binding.textViewStatusValue.text = "UNKNOWN"
                binding.textViewStatusValue.setTextColor(getStatusColor("UNKNOWN"))
                binding.textViewErrorLabel.visibility = View.VISIBLE
                binding.textViewErrorLabel.text = "Unable to retrieve payment status."
                binding.textViewErrorValue.visibility = View.VISIBLE
                binding.textViewErrorValue.text = "We could not determine your payment status. Please check your account or contact support."
                binding.textViewAmountLabel.visibility = View.GONE
                binding.textViewAmountValue.visibility = View.GONE
                binding.progressBarResult.visibility = View.GONE
                binding.textViewPaymentIdValue.text = "-"
                Toast.makeText(this, "Unable to retrieve payment status. Please check your account or contact support.", Toast.LENGTH_LONG).show()
                binding.buttonCloseResult.setOnClickListener {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish()
}
                return
            }
        }

        Log.i(TAG, "Received Payment ID: $paymentId")
        binding.textViewPaymentIdValue.text = paymentId // Display payment ID immediately
        binding.buttonCloseResult.setOnClickListener {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish()
}

        startPollingPaymentStatus()
    }

    private fun startPollingPaymentStatus() {
        Log.d(TAG, "Starting payment status polling for ID: $paymentId")
        binding.progressBarResult.visibility = View.VISIBLE
        binding.textViewStatusValue.text = "Polling..."
        hideResultDetails()

        val startTime = System.currentTimeMillis()

        pollingJob = lifecycleScope.launch { // Use lifecycleScope for automatic cancellation
            try {
                while (isActive) { // Loop while the coroutine is active
                    // Check for timeout
                    if (System.currentTimeMillis() - startTime > POLLING_TIMEOUT_MS) {
                        Log.w(TAG, "Polling timed out for payment ID: $paymentId")
                        updateUiWithError("Polling timed out. Please check status later.")
                        break // Exit loop on timeout
                    }

                    Log.d(TAG, "Polling status for payment ID: $paymentId...")
                    val response = paymentRepository.getPaymentStatus(paymentId!!)

                    if (response.isSuccessful) {
                        val paymentStatus = response.body()
                        Log.i(TAG, "Received payment status: ${paymentStatus?.status}")
                        if (paymentStatus != null) {
                            updateUiWithStatus(paymentStatus)
                            if (paymentStatus.isFinalStatus()) {
                                Log.i(TAG, "Received final status: ${paymentStatus.status}. Stopping polling.")
                                break // Exit loop on final status
                            }
                        } else {
                            Log.w(TAG, "Polling response body was null.")
                            // Optionally handle null body (retry or show error)
                        }
                    } else {
                        Log.e(TAG, "API Error fetching status: ${response.code()} - ${response.message()}")
                        // Consider showing an error message, but continue polling unless it's a fatal error like 404?
                        updateUiWithError("Error checking status: ${response.code()}")
                         // Decide if we should break here based on error type, e.g., break on 404 Not Found
                        // break
                    }

                    delay(POLLING_INTERVAL_MS) // Wait before the next poll
                }
            } catch (e: CancellationException) {
                Log.i(TAG, "Polling cancelled.") // Expected when activity is destroyed
            } catch (e: Exception) {
                Log.e(TAG, "Exception during polling", e)
                updateUiWithError("An error occurred while checking status.")
            }
        }
    }

    private fun updateUiWithStatus(statusResponse: PaymentStatusResponse) {
        binding.progressBarResult.visibility = View.GONE
        
        // Set status text and color
        val status = statusResponse.status ?: "N/A"
        binding.textViewStatusValue.text = status
        binding.textViewStatusValue.setTextColor(getStatusColor(status))
        
        // Set amount
        binding.textViewAmountValue.text =
            if (statusResponse.initiation?.amount != null)
                "${statusResponse.initiation.amount.value} ${statusResponse.initiation.amount.currency}"
            else "N/A"

        // Check final statuses associated with failure/rejection
        if (statusResponse.status in listOf("FAILURE", "REJECTED", "CANCELLED", "EXPIRED")) {
            binding.textViewErrorLabel.visibility = View.VISIBLE
            binding.textViewErrorValue.visibility = View.VISIBLE
            binding.textViewErrorValue.text = statusResponse.errorMessage ?: "Payment ended in non-success state."
        } else {
            binding.textViewErrorLabel.visibility = View.GONE
            binding.textViewErrorValue.visibility = View.GONE
        }
        showResultDetails()

        // --- Add Toast for Final Status --- 
        if (statusResponse.isFinalStatus()) {
            val toastMessage = when (status) {
                "SUCCESS", "EXECUTED", "SETTLED" -> "Payment Completed"
                "FAILURE", "REJECTED" -> "Payment Failed/Declined"
                "CANCELLED", "EXPIRED" -> "Payment Cancelled/Expired"
                "INITIATION_COMPLETED" -> "Payment Initiation Complete" // Show only this for initiation complete
                else -> null // Should not happen if isFinalStatus() is correct
            }
            // Only show toast if message is not null and status is not an intermediate state
            if (toastMessage != null) {
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
            }
        }
        // ------------------------------------
    }
    
    /**
     * Returns an appropriate color for the given payment status
     */
    private fun getStatusColor(status: String): Int {
        return when (status) {
            "SUCCESS", "EXECUTED", "SETTLED" -> 
                android.graphics.Color.parseColor("#4CAF50") // Green for success
            "INITIATION_COMPLETED" -> 
                android.graphics.Color.parseColor("#2196F3") // Blue for completed initiation
            "PENDING", "PROCESSING" -> 
                android.graphics.Color.parseColor("#FFA000") // Amber for in-progress
            "FAILURE", "REJECTED", "CANCELLED", "EXPIRED" -> 
                android.graphics.Color.parseColor("#D32F2F") // Red for failure/rejection
            else -> 
                android.graphics.Color.parseColor("#757575") // Gray for unknown status
        }
    }

    private fun updateUiWithError(errorMessage: String) {
        binding.progressBarResult.visibility = View.GONE
        binding.textViewStatusValue.text = "Error"
        binding.textViewErrorLabel.visibility = View.VISIBLE
        binding.textViewErrorValue.visibility = View.VISIBLE
        binding.textViewErrorValue.text = errorMessage
        hideResultDetails(keepErrorVisible = true) // Keep error visible but hide others if needed
    }

    // Helper to hide specific result fields initially or on error
    private fun hideResultDetails(keepErrorVisible: Boolean = false) {
        binding.textViewAmountLabel.visibility = View.GONE
        binding.textViewAmountValue.visibility = View.GONE
        if (!keepErrorVisible) {
            binding.textViewErrorLabel.visibility = View.GONE
            binding.textViewErrorValue.visibility = View.GONE
        }
    }

     // Helper to show result fields once data is available
    private fun showResultDetails() {
        binding.textViewAmountLabel.visibility = View.VISIBLE
        binding.textViewAmountValue.visibility = View.VISIBLE
        // Error visibility is handled in updateUiWithStatus/updateUiWithError
    }


    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel() // Cancel the polling job when the activity is destroyed
        Log.d(TAG, "onDestroy: Polling job cancelled.")
    }
}
