package com.example.paymentdemoandroid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.paymentdemoandroid.databinding.ActivityMainBinding
import com.example.paymentdemoandroid.model.Amount
import com.example.paymentdemoandroid.model.Creditor
import com.example.paymentdemoandroid.model.Initiation
import com.example.paymentdemoandroid.model.PaymentRequest
import com.example.paymentdemoandroid.repository.PaymentRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var paymentHandledByIntent = false
    private var paymentHandled = false // Prevents duplicate toasts after payment result

    private lateinit var binding: ActivityMainBinding
    // --- Activity Result Launcher --- 
    private lateinit var paymentWebViewLauncher: ActivityResultLauncher<Intent>
    // --------------------------------

    companion object {
        private const val TAG = "MainActivity"
        
        /**
         * Safely formats an amount for API calls using English locale to avoid
         * locale-specific decimal separators (e.g., comma in French locale)
         */
        private fun formatAmountForApi(amount: Double): String {
            return String.format(Locale.ENGLISH, "%.2f", amount)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCurrencySelection()
        setupPaymentButton()

        updateTotalLabel(binding.amountInput.text.toString(), "GBP") 

        binding.amountInput.doAfterTextChanged { text ->
            val selectedCurrency = if (binding.gbpRadioButton.isChecked) "GBP" else "EUR"
            updateTotalLabel(text.toString(), selectedCurrency)
        }

        paymentWebViewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (paymentHandledByIntent) {
                paymentHandledByIntent = false // reset for next payment
                return@registerForActivityResult
            }
            if (paymentHandled) {
                // Already handled, suppress any further toasts/flows
                return@registerForActivityResult
            }
            // Always launch PaymentResultActivity for ANY result
            val callbackUri = result.data?.data
            val intent = Intent(this, PaymentResultActivity::class.java).apply {
                putExtra("callback_uri", callbackUri?.toString())
                // Pass through any extras that might indicate cancellation or errors
                result.data?.extras?.let { putExtras(it) }
                if (result.resultCode == PaymentWebViewActivity.RESULT_PAYMENT_CANCELLED) {
                    putExtra("payment_cancelled", true)
                }
            }
            paymentHandled = true
            startActivity(intent)

        }
    }

    override fun onResume() {
        paymentHandled = false // Reset for new payment flow on resume

        super.onResume()
        // Ensure UI is enabled when the activity resumes
        showLoading(false)
    }

    private fun setupCurrencySelection() {
        binding.currencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val isEuroSelected = checkedId == R.id.eurRadioButton
            binding.sepaRadioGroup.visibility = if (isEuroSelected) View.VISIBLE else View.GONE
            binding.ibanInputLayout.visibility = if (isEuroSelected) View.VISIBLE else View.GONE
            binding.sortCodeInputLayout.visibility = if (!isEuroSelected) View.VISIBLE else View.GONE
            binding.accountNumberInputLayout.visibility = if (!isEuroSelected) View.VISIBLE else View.GONE

            if (isEuroSelected) {
                binding.sortCodeInputLayout.error = null
                binding.accountNumberInputLayout.error = null
            } else {
                binding.ibanInputLayout.error = null
            }

            val selectedCurrency = if (isEuroSelected) "EUR" else "GBP"
            updateTotalLabel(binding.amountInput.text.toString(), selectedCurrency)
        }
    }

    private fun setupPaymentButton() {
        binding.payButton.setOnClickListener {
            if (validateInput()) { 
                startPaymentFlow()
            }
        }
    }

    private fun validateInput(): Boolean {
        binding.amountInput.error = null 
        binding.payeeNameInputLayout.error = null
        binding.ibanInputLayout.error = null
        binding.sortCodeInputLayout.error = null
        binding.accountNumberInputLayout.error = null

        val amountStr = binding.amountInput.text.toString()
        if (amountStr.isBlank() || amountStr.toDoubleOrNull() == null || amountStr.toDouble() <= 0) {
            binding.amountInput.error = "Please enter a valid amount"
            return false
        }

        val payeeName = binding.payeeNameEditText.text.toString()
        if (payeeName.isBlank()) {
            binding.payeeNameInputLayout.error = "Payee name is required"
            return false
        }

        if (binding.eurRadioButton.isChecked) {
            val iban = binding.ibanEditText.text.toString().trim()
            if (iban.isBlank()) { 
                binding.ibanInputLayout.error = "IBAN is required for EUR payments"
                return false
            }
        } else { 
            val sortCode = binding.sortCodeEditText.text.toString()
            val accountNumber = binding.accountNumberEditText.text.toString()

            if (sortCode.isBlank()) { 
                binding.sortCodeInputLayout.error = "Sort Code is required for GBP payments"
                return false
            }
            if (accountNumber.isBlank()) { 
                binding.accountNumberInputLayout.error = "Account Number is required for GBP payments"
                return false
            }
        }
        return true
    }

    private fun startPaymentFlow() {
        showLoading(true)
        Log.d("MainActivity", "Starting payment flow...")

        val amountStr = binding.amountInput.text.toString()
        val amountValue = try { 
            val parsedAmount = amountStr.toDouble()
            val formattedAmount = formatAmountForApi(parsedAmount)  // Fixed version
            // val formattedAmount = "%.2f".format(parsedAmount)  // Problematic version - uses device locale
            Log.d("MainActivity", "Amount formatting - Input: '$amountStr', Locale: ${Locale.getDefault()}, Formatted: '$formattedAmount'")
            formattedAmount
        } catch (e: Exception) { 
            Log.e("MainActivity", "Failed to format amount: $amountStr", e)
            "0.00" 
        }
        val currency = if (binding.gbpRadioButton.isChecked) "GBP" else "EUR"
        val payeeName = binding.payeeNameEditText.text.toString()
        val localInstrument: String
        val creditor: Creditor

        if (currency == "EUR") {
            val iban = binding.ibanEditText.text.toString().trim()
            localInstrument = if (binding.sepaInstantRadioButton.isChecked) "SEPA_INSTANT" else "SEPA"
            creditor = Creditor(
                name = payeeName,
                sortCode = null,
                accountNumber = null,
                iban = iban
            )
        } else { 
            val sortCode = binding.sortCodeEditText.text.toString()
            val accountNumber = binding.accountNumberEditText.text.toString()
            localInstrument = "FASTER_PAYMENTS" 
            creditor = Creditor(
                name = payeeName,
                sortCode = sortCode,
                accountNumber = accountNumber,
                iban = null
            )
        }

        val refId = generateRandomAlphaNumericString(12)
        val paymentRequest = PaymentRequest(
            initiation = Initiation(
                refId = refId,
                flowType = if (currency == "EUR") "FULL_HOSTED_PAGES" else "FULL_HOSTED_PAGES",
                remittanceInformationPrimary = "RP$refId",
                remittanceInformationSecondary = "RS$refId",
                amount = Amount(
                    value = amountValue,
                    currency = currency
                ),
                localInstrument = localInstrument,
                creditor = creditor,
                callbackUrl = Constants.CALLBACK_URL,
                callbackState = generateRandomAlphaNumericString(12)
            ),
            pispConsentAccepted = true
        )

        Log.d("MainActivity", "PaymentRequest created - Amount: '${paymentRequest.initiation.amount.value}', Currency: '${paymentRequest.initiation.amount.currency}'")

        PaymentSdk.startPaymentFlow(
            activity = this,
            paymentRequest = paymentRequest,
            launcher = paymentWebViewLauncher
        ) { paymentResult ->
            showLoading(false)
            when (paymentResult) {
                is PaymentResult.Success -> {
                    val intent = Intent(this, PaymentResultActivity::class.java).apply {
                        putExtra(PaymentResultActivity.EXTRA_PAYMENT_ID, paymentResult.paymentId)
                    }
                    startActivity(intent)
                }
                is PaymentResult.Failure -> {
                    // Launch PaymentResultActivity and pass the error message
                    val intent = Intent(this, PaymentResultActivity::class.java).apply {
                        putExtra("payment_error", paymentResult.error)
                    }
                    startActivity(intent)
                }
                is PaymentResult.Cancelled -> {
                    Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateRandomAlphaNumericString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.payButton.isEnabled = !isLoading
        binding.amountInput.isEnabled = !isLoading
        binding.payeeNameInputLayout.isEnabled = !isLoading
        binding.currencyRadioGroup.isEnabled = !isLoading 
        binding.sepaRadioGroup.isEnabled = !isLoading
        binding.ibanInputLayout.isEnabled = !isLoading
        binding.sortCodeInputLayout.isEnabled = !isLoading
        binding.accountNumberInputLayout.isEnabled = !isLoading
    }

    // No longer needed: all error/success messages are handled in PaymentResultActivity
    // private fun showError(message: String) {
    //     showLoading(false) 
    //     Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    // }

    private fun updateTotalLabel(amountStr: String?, currency: String) {
        val symbol = if (currency == "GBP") "£" else "€"
        val amount = amountStr?.toDoubleOrNull() ?: 0.00
        binding.totalValueTextView.text = "$symbol${String.format("%.2f", amount)}"
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle browser intent callback if activity is already running
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null && intent.data?.scheme == "paymentdemoapp" && intent.data?.host == "payment-complete") {
            android.util.Log.i("MainActivity", "Received browser callback intent (onNewIntent): action=${intent.action}, data=${intent.data}")
            if (!paymentHandledByIntent) {
                paymentHandledByIntent = true
                val callbackUri = intent.data
                val resultIntent = Intent(this, PaymentResultActivity::class.java).apply {
                    putExtra("callback_uri", callbackUri?.toString())
                }
                startActivity(resultIntent)
            }
        }
        // No other payment result handling here; handled via ActivityResult
    }
}
