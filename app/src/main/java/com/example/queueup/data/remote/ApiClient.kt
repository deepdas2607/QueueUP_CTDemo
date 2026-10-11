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

/**
 * ApiClient is a Kotlin `object` (a thread-safe Singleton) providing configured HTTP access via Retrofit.
 *
 * It configures:
 * 1. OkHttpClient: Handles connection pooling, timeouts, and HTTP request interception.
 * 2. AuthInterceptor: Attaches the user's JWT bearer token to headers of every authenticated call.
 * 3. HostFallbackInterceptor: Auto-switches between `127.0.0.1` and `10.0.2.2` so API calls work reliably on emulators.
 * 4. Retrofit: Converts interface definitions (`ApiService.kt`) into executable HTTP calls using Gson serialization.
 */
object ApiClient {

    private var sessionManager: SessionManager? = null

    /**
     * Initializes ApiClient with a reference to SessionManager.
     * Called once during MainActivity.onCreate().
     */
    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    /**
     * OkHttp Interceptor that dynamically extracts the stored JWT authentication token
     * and appends it to the HTTP headers: `Authorization: Bearer <token>`.
     */
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        sessionManager?.getAuthToken()?.let { token ->
            if (token.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }
        chain.proceed(requestBuilder.build())
    }

    /**
     * Logs request URLs, headers, and JSON bodies in Logcat during development.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Intelligent host fallback interceptor:
     * When running inside an Android emulator:
     * - `127.0.0.1` maps to the host machine ONLY when `adb reverse tcp:3000 tcp:3000` is active.
     * - `10.0.2.2` is the special virtual gateway mapping directly to the developer's computer.
     * If the initial request fails, this interceptor automatically retries with the alternate IP.
     */
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

    // Configured HTTP Client with connection pooling and timeouts
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

    /**
     * `by lazy`: Kotlin property delegation that delays creation of ApiService
     * until the first time it is actually accessed in code.
     */
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
