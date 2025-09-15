package com.example.paymentdemoandroid

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentdemoandroid.model.Amount
import com.example.paymentdemoandroid.model.Creditor
import com.example.paymentdemoandroid.model.Initiation
import com.example.paymentdemoandroid.model.PaymentRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import java.util.UUID

class PaymentSdkTest {
    private val dummyActivity = mock<AppCompatActivity>()
    private val dummyLauncher = mock<ActivityResultLauncher<Intent>>()

    private fun buildPaymentRequest(): PaymentRequest = PaymentRequest(
        initiation = Initiation(
            refId = UUID.randomUUID().toString(),
            flowType = "FULL_HOSTED_PAGES",
            remittanceInformationPrimary = "Test",
            remittanceInformationSecondary = "Test2",
            amount = Amount("10.00", "GBP"),
            localInstrument = "FASTER_PAYMENTS",
            creditor = Creditor(name = "Test Creditor", sortCode = "123456", accountNumber = "12345678", iban = null),
            callbackUrl = "paymentdemoapp://payment-complete",
            callbackState = UUID.randomUUID().toString()
        ),
        pispConsentAccepted = true
    )

    @Test
    fun `PaymentResult Success delivers paymentId`() {
        val intent = Intent().apply {
            data = android.net.Uri.parse("paymentdemoapp://payment-complete?payment-id=abc123")
        }
        var result: PaymentResult? = null
        PaymentSdk.handleWebViewResult(Activity.RESULT_OK, intent) {
            println("Callback called with: $it")
            result = it
        }
        if (result == null) fail("Callback was not called; result is still null after handleWebViewResult.")
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be Success but was $result", result is PaymentResult.Success)
        assertEquals("abc123", (result as PaymentResult.Success).paymentId)
    }

    @Test
    fun `PaymentResult Failure delivers error on missing paymentId`() {
        val intent = Intent().apply {
            data = android.net.Uri.parse("paymentdemoapp://payment-complete")
        }
        var result: PaymentResult? = null
        PaymentSdk.handleWebViewResult(Activity.RESULT_OK, intent) {
            println("Callback called with: $it")
            result = it
        }
        if (result == null) fail("Callback was not called; result is still null after handleWebViewResult.")
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be Failure but was $result", result is PaymentResult.Failure)
        assertTrue((result as PaymentResult.Failure).error.contains("missing", ignoreCase = true))
    }

    @Test
    fun `PaymentResult Cancelled is delivered`() {
        var result: PaymentResult? = null
        PaymentSdk.handleWebViewResult(PaymentWebViewActivity.RESULT_PAYMENT_CANCELLED, null) {
            result = it
        }
        assertTrue(result is PaymentResult.Cancelled)
    }

    // You can add more tests here for edge cases, e.g., null intents, unexpected result codes, etc.
}
