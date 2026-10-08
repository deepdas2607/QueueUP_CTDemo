// FILE TYPE: Retrofit API Service Interface
// PURPOSE: Defines all REST API endpoint contracts for QueueUp backend.
// USED BY: ApiClient, AuthRepository, ServiceRepository, QueueRepository, ProfileRepository
// DATA SOURCE: Express REST API Endpoints

package com.example.queueup.data.remote

import com.example.queueup.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<User>

    // --- Services ---
    @GET("services")
    suspend fun getServices(): Response<List<Service>>

    @GET("services/{id}")
    suspend fun getServiceById(@Path("id") id: String): Response<Service>

    // --- Queues ---
    @POST("queues/join")
    suspend fun joinQueue(@Body request: JoinQueueRequest): Response<JoinQueueResponse>

    @GET("queues/my-active")
    suspend fun getMyActiveQueue(): Response<ActiveQueueResponse>

    @GET("queues/history")
    suspend fun getQueueHistory(): Response<List<QueueEntry>>

    @GET("queues/{id}")
    suspend fun getQueueById(@Path("id") id: String): Response<ActiveQueueData>

    @POST("queues/{id}/leave")
    suspend fun leaveQueue(@Path("id") id: String): Response<QueueEntry>

    @POST("queues/{id}/refresh")
    suspend fun refreshQueue(@Path("id") id: String): Response<ActiveQueueResponse>

    // --- Profile & Dashboard ---
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<User>

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("content/queue-tips")
    suspend fun getQueueTips(): Response<QueueTipsResponse>
}
