// FILE TYPE: Repository
// PURPOSE: Handles user authentication, registration, login, and session persistence.
// USED BY: AuthViewModel, SplashViewModel
// DATA SOURCE: ApiService (Express REST API) & SessionManager

package com.example.queueup.data.repository

import com.example.queueup.data.model.AuthResponse
import com.example.queueup.data.model.LoginRequest
import com.example.queueup.data.model.RegisterRequest
import com.example.queueup.data.model.User
import com.example.queueup.data.remote.ApiClient
import com.example.queueup.utils.SessionManager

class AuthRepository(private val sessionManager: SessionManager) {

    suspend fun register(
        name: String,
        email: String,
        password: String,
        occupation: String?,
        interests: String?
    ): Result<User> {
        return try {
            val response = ApiClient.apiService.register(
                RegisterRequest(name, email, password, occupation, interests)
            )
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                sessionManager.saveAuthToken(authData.token)
                sessionManager.saveUser(
                    id = authData.user.id,
                    name = authData.user.name,
                    email = authData.user.email,
                    occupation = authData.user.occupation,
                    interests = authData.user.interests,
                    role = authData.user.role
                )
                Result.success(authData.user)
            } else {
                val rawError = response.errorBody()?.string()
                val parsed = com.example.queueup.utils.ErrorParser.parse(rawError)
                Result.failure(Exception(parsed))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = ApiClient.apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!
                sessionManager.saveAuthToken(authData.token)
                sessionManager.saveUser(
                    id = authData.user.id,
                    name = authData.user.name,
                    email = authData.user.email,
                    occupation = authData.user.occupation,
                    interests = authData.user.interests,
                    role = authData.user.role
                )
                Result.success(authData.user)
            } else {
                Result.failure(Exception("Login failed: Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = ApiClient.apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                sessionManager.saveUser(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    occupation = user.occupation,
                    interests = user.interests,
                    role = user.role
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to fetch user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun logout() {
        sessionManager.clearSession()
    }
}
