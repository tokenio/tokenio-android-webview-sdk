package com.example.paymentdemoandroid.network

import com.example.paymentdemoandroid.model.PaymentRequest
import com.example.paymentdemoandroid.model.PaymentResponse
import com.example.paymentdemoandroid.model.PaymentStatusApiResponse
import com.example.paymentdemoandroid.model.PaymentStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("v2/payments")
    suspend fun initiatePayment(
        // Remove the Authorization Header parameter, it's handled by the interceptor
        // @Header("Authorization") apiKey: String,
        @Body paymentRequest: PaymentRequest
    ): Response<PaymentResponse>

    @GET("v2/payments/{paymentId}")
    suspend fun getPaymentStatus(
        @Path("paymentId") paymentId: String
    ): Response<PaymentStatusApiResponse>

}
