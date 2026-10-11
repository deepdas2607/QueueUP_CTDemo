// FILE TYPE: ViewModel
// PURPOSE: Holds UI state and business logic for Login and Register screens.
// USED BY: LoginScreen, RegisterScreen, SplashScreen
// CALLS: AuthRepository

package com.example.queueup.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.data.model.User
import com.example.queueup.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * AuthViewModel manages user onboarding, input validation, authentication calls, and logout cleanup.
 *
 * It holds two-way binding form states for both LoginScreen and RegisterScreen:
 * - Login fields: `loginEmail`, `loginPassword`
 * - Register fields: `registerName`, `registerEmail`, `registerPassword`, `registerOccupation`, `registerInterests`
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Login Form State
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // Registration Form State
    var registerName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerOccupation by mutableStateOf("")
    var registerInterests by mutableStateOf("")

    // UI Feedback State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var authSuccessUser by mutableStateOf<User?>(null)

    /**
     * Checks if a valid JWT token already exists in SharedPreferences.
     * Called by SplashScreen to decide whether to route to HomeScreen or LoginScreen.
     */
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    /**
     * Validates user credentials and executes the login network request.
     * @param onSuccess Callback lambda triggered upon 200 OK response, routing user to HomeScreen.
     */
    fun login(onSuccess: () -> Unit) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            errorMessage = "Please fill in email and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.login(loginEmail.trim(), loginPassword)
            isLoading = false
            result.onSuccess { user ->
                authSuccessUser = user
                onSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Login failed"
            }
        }
    }

    /**
     * Validates new user input and sends registration payload to the Express backend.
     * @param onSuccess Callback lambda invoked on success to route user to HomeScreen.
     */
    fun register(onSuccess: () -> Unit) {
        if (registerName.isBlank() || registerEmail.isBlank() || registerPassword.isBlank()) {
            errorMessage = "Name, email, and password are required"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = authRepository.register(
                name = registerName.trim(),
                email = registerEmail.trim(),
                password = registerPassword,
                occupation = registerOccupation.ifBlank { null },
                interests = registerInterests.ifBlank { null }
            )
            isLoading = false
            result.onSuccess { user ->
                authSuccessUser = user
                onSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Registration failed"
            }
        }
    }

    /**
     * Clears error banner when user begins typing or dismisses the dialog.
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Clears saved authentication credentials and resets form fields to protect student privacy.
     * @param onLogoutDone Callback lambda navigating back to LoginScreen and clearing backstack.
     */
    fun logout(onLogoutDone: () -> Unit) {
        authRepository.logout()
        authSuccessUser = null
        loginEmail = ""
        loginPassword = ""
        onLogoutDone()
    }
}
