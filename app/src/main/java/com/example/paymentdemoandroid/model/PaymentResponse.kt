package com.example.paymentdemoandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
/**
 * [INTERNAL] Used for parsing payment initiation API responses. Not for public SDK use.
 */
data class PaymentResponse(
    @Json(name = "payment") val payment: PaymentDetails
)

@JsonClass(generateAdapter = true)
/**
 * [INTERNAL] Details of the payment returned from the API. Not for public SDK use.
 */
data class PaymentDetails(
    @Json(name = "id") val id: String,
    @Json(name = "authentication") val authentication: Authentication
    // Add other fields if needed, e.g., status, createdDateTime
)

@JsonClass(generateAdapter = true)
/**
 * [INTERNAL] Authentication/redirect URL details. Not for public SDK use.
 */
data class Authentication(
    @Json(name = "redirectUrl") val redirectUrl: String
)
