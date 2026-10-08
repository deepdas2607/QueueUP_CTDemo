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

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    var registerName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerOccupation by mutableStateOf("")
    var registerInterests by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var authSuccessUser by mutableStateOf<User?>(null)

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun login(onSuccess: () -> Unit) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            errorMessage = "Please fill in email and password"
            return;
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

    fun clearError() {
        errorMessage = null
    }

    fun logout(onLogoutDone: () -> Unit) {
        authRepository.logout()
        authSuccessUser = null
        loginEmail = ""
        loginPassword = ""
        onLogoutDone()
    }
}
