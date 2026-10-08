// FILE TYPE: UI Screen
// PURPOSE: Allows users to log in with email and password.
// USED BY: MainActivity Navigation
// CALLS: AuthViewModel

package com.example.queueup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.ui.components.CustomTextField
import com.example.queueup.ui.components.ErrorBanner
import com.example.queueup.ui.theme.PrimaryIndigo
import com.example.queueup.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sign in to manage your campus queue status",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            ErrorBanner(
                errorMessage = authViewModel.errorMessage,
                modifier = Modifier.padding(bottom = 12.dp),
                onDismiss = { authViewModel.errorMessage = null }
            )

            CustomTextField(
                value = authViewModel.loginEmail,
                onValueChange = { authViewModel.loginEmail = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = authViewModel.loginPassword,
                onValueChange = { authViewModel.loginPassword = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.login(onSuccess = onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                enabled = !authViewModel.isLoading
            ) {
                if (authViewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
            }
        }
    }
}
