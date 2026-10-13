// VIEWMODEL LAYER (VM in MVVM): Manages Login and Registration screen state and calls ApiClient directly.

package com.example.queueup.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.models.LoginRequest
import com.example.queueup.models.RegisterRequest
import com.example.queueup.models.User
import com.example.queueup.network.ApiClient
import com.example.queueup.network.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val inspectorViewModel: ActivityInspectorViewModel
) : ViewModel() {

    // Login screen input fields.
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // Register screen input fields.
    var registerName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerPhone by mutableStateOf("")
    var registerOccupation by mutableStateOf("")
    var registerInterests by mutableStateOf("")

    // Loading spinner and error message banner state.
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Checks if a login token is already saved on the phone.
    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    // Sends email and password to POST /api/auth/login and saves the returned JWT session.
    fun loginUser(onSuccess: () -> Unit) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.loginUser(
                    LoginRequest(email = loginEmail.trim(), password = loginPassword)
                )
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    saveUserSession(authData.token, authData.user)

                    // Log identity login and event payload in the Live Activity Inspector.
                    inspectorViewModel.logIdentityLogin(
                        actionName = "onUserLogin",
                        summary = "Identified user ${authData.user.name} (${authData.user.email})",
                        profileMap = mapOf(
                            "Identity" to authData.user.id,
                            "Name" to authData.user.name,
                            "Email" to authData.user.email,
                            "Phone" to (authData.user.phone ?: ""),
                            "Occupation" to (authData.user.occupation ?: "Student"),
                            "Interests" to (authData.user.interests ?: "Campus Life")
                        )
                    )
                    inspectorViewModel.logEvent(
                        eventName = "User_Logged_In",
                        summary = "User signed in with email and password",
                        properties = mapOf(
                            "user_id" to authData.user.id,
                            "email" to authData.user.email,
                            "role" to authData.user.role
                        )
                    )
                    isLoading = false
                    onSuccess()
                } else {
                    isLoading = false
                    errorMessage = parseErrorMessage(response.errorBody()?.string(), "Invalid email or password")
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Cannot connect to server. Make sure backend is running on port 3000."
            }
        }
    }

    // Sends new account details to POST /api/auth/register and logs the user in automatically.
    fun registerUser(onSuccess: () -> Unit) {
        if (registerName.isBlank() || registerEmail.isBlank() || registerPassword.isBlank()) {
            errorMessage = "Name, email, and password are required"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.registerUser(
                    RegisterRequest(
                        name = registerName.trim(),
                        email = registerEmail.trim(),
                        password = registerPassword,
                        phone = registerPhone.ifBlank { null },
                        occupation = registerOccupation.ifBlank { "Student" },
                        interests = registerInterests.ifBlank { "Campus Life" }
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    saveUserSession(authData.token, authData.user)

                    inspectorViewModel.logIdentityLogin(
                        actionName = "onUserLogin (New Registration)",
                        summary = "Created profile for ${authData.user.name}",
                        profileMap = mapOf(
                            "Identity" to authData.user.id,
                            "Name" to authData.user.name,
                            "Email" to authData.user.email,
                            "Phone" to (authData.user.phone ?: ""),
                            "Occupation" to (authData.user.occupation ?: "Student"),
                            "Interests" to (authData.user.interests ?: "Campus Life")
                        )
                    )
                    inspectorViewModel.logEvent(
                        eventName = "User_Registered",
                        summary = "New student account registered",
                        properties = mapOf(
                            "user_id" to authData.user.id,
                            "occupation" to (authData.user.occupation ?: "Student"),
                            "interests" to (authData.user.interests ?: "Campus Life")
                        )
                    )
                    isLoading = false
                    onSuccess()
                } else {
                    isLoading = false
                    errorMessage = parseErrorMessage(response.errorBody()?.string(), "Registration failed")
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Cannot connect to server. Make sure backend is running on port 3000."
            }
        }
    }

    // Saves the JWT token and user details into SessionManager.
    private fun saveUserSession(token: String, user: User) {
        sessionManager.saveAuthToken(token)
        sessionManager.saveUserProfile(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            occupation = user.occupation,
            interests = user.interests
        )
    }

    // Extracts the readable "error" field from a backend JSON error response.
    private fun parseErrorMessage(rawJson: String?, fallback: String): String {
        if (rawJson.isNullOrBlank()) return fallback
        return try {
            JSONObject(rawJson).optString("error", fallback)
        } catch (e: Exception) {
            fallback
        }
    }
}
