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

/**
 * AuthRepository coordinates user authentication, token storage, and session lifecycle.
 *
 * Responsibilities:
 * 1. Submits login/registration HTTP requests to Express backend.
 * 2. Saves the returned JWT authentication token to SharedPreferences via SessionManager.
 * 3. Persists user profile information so HomeScreen and ProfileScreen have instant access.
 */
class AuthRepository(private val sessionManager: SessionManager) {

    /**
     * Registers a new student account in the backend database.
     * @param name Full name of the student.
     * @param email Campus email address.
     * @param password Raw password (will be hashed with bcrypt on backend).
     * @param occupation Optional field (e.g. "Computer Science Undergraduate").
     * @param interests Optional field (e.g. "Robotics, AI, Gaming").
     */
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
                // Persist authentication state locally so the user remains logged in across app restarts
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
