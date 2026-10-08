// FILE TYPE: Data Model / Request Payload
// PURPOSE: Authentication request and response objects for Login and Registration.
// USED BY: AuthRepository, ApiService, AuthViewModel
// DATA SOURCE: Express REST API Auth Endpoints

package com.example.queueup.data.model

data class AuthResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val occupation: String? = null,
    val interests: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val name: String? = null,
    val occupation: String? = null,
    val interests: String? = null
)
