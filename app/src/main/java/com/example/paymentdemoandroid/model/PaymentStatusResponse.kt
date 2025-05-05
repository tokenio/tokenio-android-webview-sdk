package com.example.paymentdemoandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the data structure for the payment status response from GET /v2/payments/{paymentId}.
 * Note: The actual API response nests this structure under a top-level "payment" key.
 * Retrofit/Moshi needs to be configured to handle this (e.g., using a wrapper or adapter).
 */
@JsonClass(generateAdapter = true)
/**
 * [INTERNAL] Used for parsing payment status API responses. Not for public SDK use.
 */
data class PaymentStatusResponse(
    @Json(name = "id") val id: String?,
    @Json(name = "status") val status: String?, // e.g., "INITIATION_COMPLETED", "EXECUTED", "FAILED"
    @Json(name = "memberId") val memberId: String?,
    @Json(name = "initiation") val initiation: Initiation?,
    @Json(name = "bankPaymentId") val bankPaymentId: String?,
    @Json(name = "bankTransactionId") val bankTransactionId: String?,
    @Json(name = "createdDateTime") val createdDateTime: String?, // Consider parsing to Date/Timestamp
    @Json(name = "updatedDateTime") val updatedDateTime: String?, // Consider parsing to Date/Timestamp
    @Json(name = "bankPaymentStatus") val bankPaymentStatus: String?, // e.g., "AcceptedSettlementCompleted"
    @Json(name = "errorMessage") val errorMessage: String? // Populated on failure/rejection
) {

    @JsonClass(generateAdapter = true)
    /**
 * [INTERNAL] Initiation details for status API. Not for public SDK use.
 */
data class Initiation(
        @Json(name = "bankId") val bankId: String?,
        @Json(name = "refId") val refId: String?,
        @Json(name = "remittanceInformationPrimary") val remittanceInformationPrimary: String?,
        @Json(name = "remittanceInformationSecondary") val remittanceInformationSecondary: String?,
        @Json(name = "amount") val amount: Amount?,
        @Json(name = "localInstrument") val localInstrument: String?,
        @Json(name = "creditor") val creditor: Creditor?,
        @Json(name = "callbackUrl") val callbackUrl: String?,
        @Json(name = "callbackState") val callbackState: String?,
        @Json(name = "flowType") val flowType: String?
    )

    @JsonClass(generateAdapter = true)
    /**
 * [INTERNAL] Amount details for status API. Not for public SDK use.
 */
data class Amount(
        @Json(name = "value") val value: String?,
        @Json(name = "currency") val currency: String?
    )

    @JsonClass(generateAdapter = true)
    /**
 * [INTERNAL] Creditor details for status API. Not for public SDK use.
 */
data class Creditor(
        @Json(name = "name") val name: String?,
        @Json(name = "sortCode") val sortCode: String?,
        @Json(name = "accountNumber") val accountNumber: String?
    )

    /**
     * Checks if the current payment status is considered final (non-polling).
     * Note: This list might need adjustment based on the exact final statuses returned by the API.
     */
    fun isFinalStatus(): Boolean {
        // Add all known terminal statuses here based on API documentation
        return status in listOf(
            "SUCCESS", // Hypothetical final success status
            "EXECUTED", // Likely final success
            "SETTLED", // Likely final success
            "FAILURE",
            "REJECTED",
            "CANCELLED",
            "EXPIRED",
            "INITIATION_COMPLETED" // Now considered final for polling purposes
        )
    }
}
