package com.example.paymentdemoandroid.model

/**
 * Public data classes for creating a payment request with the SDK.
 * These are the only model classes customers should use directly.
 */

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Using Moshi for JSON serialization
@JsonClass(generateAdapter = true)
/**
 * Represents the payload to initiate a payment via the SDK.
 */
data class PaymentRequest(
    @Json(name = "initiation") val initiation: Initiation,
    @Json(name = "pispConsentAccepted") val pispConsentAccepted: Boolean = true
)

@JsonClass(generateAdapter = true)
/**
 * Details of the payment to be initiated (amount, creditor, remittance, etc).
 */
data class Initiation(
    @Json(name = "refId") val refId: String,
    @Json(name = "flowType") val flowType: String = "FULL_HOSTED_PAGES",
    @Json(name = "remittanceInformationPrimary") val remittanceInformationPrimary: String,
    @Json(name = "remittanceInformationSecondary") val remittanceInformationSecondary: String,
    @Json(name = "amount") val amount: Amount,
    @Json(name = "localInstrument") val localInstrument: String = "FASTER_PAYMENTS",
    @Json(name = "creditor") val creditor: Creditor,
    @Json(name = "callbackUrl") val callbackUrl: String,
    @Json(name = "callbackState") val callbackState: String
)

@JsonClass(generateAdapter = true)
/**
 * Amount and currency for the payment.
 */
data class Amount(
    @Json(name = "value") val value: String,
    @Json(name = "currency") val currency: String
)

@JsonClass(generateAdapter = true)
/**
 * Recipient bank details (IBAN or sort code/account number).
 */
data class Creditor(
    @Json(name = "name") val name: String,
    @Json(name = "sortCode") val sortCode: String? = null,
    @Json(name = "accountNumber") val accountNumber: String? = null,
    @Json(name = "iban") val iban: String? = null 
)
