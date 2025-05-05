package com.example.paymentdemoandroid.network

import android.util.Log
import com.example.paymentdemoandroid.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY 
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val apiKey = Constants.selectedEnvironment.apiKey
            Log.d("AuthInterceptor", "Using API Key for ${Constants.selectedEnvironment.displayName}") 
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Basic $apiKey") 
                .header("Content-Type", "application/json") 
                .build()
            return chain.proceed(newRequest)
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor()) 
        .addInterceptor(loggingInterceptor) 
        .build()

    fun getApiService(): ApiService {
        val currentEnvironment = Constants.selectedEnvironment
        Log.d("RetrofitClient", "Creating ApiService for ${currentEnvironment.displayName} with URL: ${currentEnvironment.baseUrl}")
        return Retrofit.Builder()
            .baseUrl(currentEnvironment.baseUrl) 
            .client(okHttpClient) 
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
