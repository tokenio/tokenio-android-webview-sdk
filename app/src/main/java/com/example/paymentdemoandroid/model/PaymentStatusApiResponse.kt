package com.example.paymentdemoandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Wrapper class to handle the top-level "payment" key in the API response
 * for GET /v2/payments/{paymentId}.
 */
@JsonClass(generateAdapter = true)
/**
 * [INTERNAL] Wrapper for payment status API responses. Not for public SDK use.
 */
data class PaymentStatusApiResponse(
    @Json(name = "payment") val payment: PaymentStatusResponse?
)
