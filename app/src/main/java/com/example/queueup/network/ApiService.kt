// NETWORK LAYER: Retrofit interface defining all REST API endpoints for the QueueUp backend.

package com.example.queueup.network

import com.example.queueup.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication ---
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>

    // --- Campus Services ---
    @GET("api/services")
    suspend fun getAllCampusServices(): Response<List<Service>>

    @GET("api/services/{id}")
    suspend fun getCampusServiceById(@Path("id") id: String): Response<Service>

    @GET("api/content/queue-tips")
    suspend fun getCampusTips(): Response<QueueTipsResponse>

    // --- Queue Management ---
    @POST("api/queues/join")
    suspend fun joinQueue(@Body request: JoinQueueRequest): Response<ActiveQueueData>

    @GET("api/queues/my-active")
    suspend fun getActiveQueue(): Response<ActiveQueueResponse>

    @POST("api/queues/{id}/refresh")
    suspend fun refreshQueue(@Path("id") id: String): Response<ActiveQueueResponse>

    @POST("api/queues/{id}/complete")
    suspend fun completeQueue(@Path("id") id: String): Response<QueueEntry>

    @POST("api/queues/{id}/leave")
    suspend fun leaveQueue(@Path("id") id: String): Response<QueueEntry>

    @GET("api/queues/history")
    suspend fun getQueueHistory(): Response<List<QueueEntry>>

    // --- User Profile ---
    @GET("api/profile")
    suspend fun getUserProfile(): Response<ProfileResponse>

    @PUT("api/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): Response<User>
}
