// NETWORK LAYER: Singleton HTTP client configuring Retrofit, OkHttp, JWT auth header, and emulator fallback.

package com.example.queueup.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://127.0.0.1:3000/"
    private var sessionManager: SessionManager? = null

    // Connects SessionManager so outgoing HTTP requests can automatically attach the saved JWT token.
    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    // Appends the JWT token to HTTP headers: "Authorization: Bearer <token>" when the user is logged in.
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        val token = sessionManager?.getAuthToken()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    // Automatically falls back from 127.0.0.1 to 10.0.2.2 if running in an emulator without reverse proxy.
    private val emulatorFallbackInterceptor = Interceptor { chain ->
        val original = chain.request()
        try {
            chain.proceed(original)
        } catch (e: Exception) {
            val originalUrl = original.url
            val alternateHost = if (originalUrl.host == "127.0.0.1") "10.0.2.2" else "127.0.0.1"
            val fallbackUrl = originalUrl.newBuilder().host(alternateHost).build()
            val fallbackRequest = original.newBuilder().url(fallbackUrl).build()
            chain.proceed(fallbackRequest)
        }
    }

    // OkHttp client with sensible connection timeouts and logging.
    private val okHttpClient = OkHttpClient.Builder()
        .proxy(java.net.Proxy.NO_PROXY)
        .retryOnConnectionFailure(true)
        .addInterceptor(emulatorFallbackInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Lazy singleton instance of ApiService ready for all ViewModels to call directly.
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
