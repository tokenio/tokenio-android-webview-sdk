package com.example.paymentdemoandroid.repository

import com.example.paymentdemoandroid.Constants
import com.example.paymentdemoandroid.model.PaymentRequest
import com.example.paymentdemoandroid.model.PaymentResponse
import com.example.paymentdemoandroid.model.PaymentStatusApiResponse
import com.example.paymentdemoandroid.model.PaymentStatusResponse
import com.example.paymentdemoandroid.network.RetrofitClient
import retrofit2.Response

/**
 * [INTERNAL] Handles API calls for payment initiation and status.
 * Not intended for direct use by SDK integrators.
 */
internal class PaymentRepository {

    // Updated to call the function that builds ApiService dynamically
    suspend fun initiatePayment(paymentRequest: PaymentRequest): Response<PaymentResponse> {
        // Get the ApiService instance configured for the selected environment
        val apiService = RetrofitClient.getApiService()
        return apiService.initiatePayment(paymentRequest)
        // Old call: return RetrofitClient.instance.initiatePayment(paymentRequest)
    }

    suspend fun getPaymentStatus(paymentId: String): Response<PaymentStatusResponse> {
        val apiResponse: Response<PaymentStatusApiResponse> = RetrofitClient.getApiService().getPaymentStatus(paymentId)

        // Handle the response and extract the nested PaymentStatusResponse
        return if (apiResponse.isSuccessful && apiResponse.body()?.payment != null) {
            // Create a new successful Response containing the unwrapped PaymentStatusResponse
            Response.success(apiResponse.body()!!.payment, apiResponse.raw())
        } else if (!apiResponse.isSuccessful) {
            // Propagate the error response
            Response.error(apiResponse.code(), apiResponse.errorBody() ?: okhttp3.ResponseBody.create(null, ""))
        } else {
            // Handle the case where the response was successful but the body or nested payment was null
            // Return an error or a specific representation indicating missing data
            // For simplicity, returning an error similar to a non-successful response
            // You might want a custom error handling strategy here.
            Response.error(500, okhttp3.ResponseBody.create(null, "")) // Or a more specific error code/body
        }
    }
}
