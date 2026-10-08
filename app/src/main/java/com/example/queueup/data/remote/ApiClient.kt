// FILE TYPE: Network Client Setup
// PURPOSE: Configures Retrofit and OkHttp with Authorization Interceptors.
// USED BY: Repositories
// CALLS: Express REST API via Retrofit

package com.example.queueup.data.remote

import com.example.queueup.utils.Constants
import com.example.queueup.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var sessionManager: SessionManager? = null

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        sessionManager?.getAuthToken()?.let { token ->
            if (token.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }
        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val hostFallbackInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        try {
            chain.proceed(originalRequest)
        } catch (e: Exception) {
            val url = originalRequest.url
            val alternateHost = if (url.host == "127.0.0.1") "10.0.2.2" else "127.0.0.1"
            val fallbackUrl = url.newBuilder().host(alternateHost).build()
            val fallbackRequest = originalRequest.newBuilder().url(fallbackUrl).build()
            chain.proceed(fallbackRequest)
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .proxy(java.net.Proxy.NO_PROXY)
        .retryOnConnectionFailure(true)
        .addInterceptor(hostFallbackInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
