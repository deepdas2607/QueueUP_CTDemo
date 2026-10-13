// MODEL LAYER (M in MVVM): Data classes for User accounts, Login/Register payloads, and Profile stats.

package com.example.queueup.models

// Represents a registered student or user in the QueueUp database.
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = "",
    val occupation: String? = "Student",
    val interests: String? = "Campus Life",
    val role: String = "USER",
    val createdAt: String? = null
)

// Request body sent to POST /api/auth/login when a user signs in.
data class LoginRequest(
    val email: String,
    val password: String
)

// Request body sent to POST /api/auth/register when creating a new account.
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val occupation: String? = null,
    val interests: String? = null
)

// Response returned by the backend after successful login or registration.
data class AuthResponse(
    val token: String,
    val user: User
)

// Request body sent to PUT /api/profile when the user saves edited profile details.
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val occupation: String? = null,
    val interests: String? = null
)

// Holds summary counts of how many queues a user has joined, completed, or cancelled.
data class UserQueueStats(
    val totalJoined: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0
)

// Combined response returned by GET /api/profile containing user info and queue stats.
data class ProfileResponse(
    val user: User,
    val stats: UserQueueStats
)
