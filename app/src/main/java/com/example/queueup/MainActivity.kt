// ENTRY POINT: Initializes ApiClient, ViewModels (VM), and Jetpack Compose Navigation between Views (V).

package com.example.queueup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.queueup.network.ApiClient
import com.example.queueup.network.NotificationHelper
import com.example.queueup.network.SessionManager
import com.example.queueup.viewmodels.ActivityInspectorViewModel
import com.example.queueup.viewmodels.AuthViewModel
import com.example.queueup.viewmodels.ProfileViewModel
import com.example.queueup.viewmodels.QueueViewModel
import com.example.queueup.views.screens.*
import com.example.queueup.views.theme.QueueUpTheme

class MainActivity : ComponentActivity() {

    // Called by Android when the app launches; sets up SessionManager, ApiClient, and the Compose UI.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        ApiClient.init(sessionManager)

        setContent {
            var isDarkMode by remember { mutableStateOf(sessionManager.isDarkModeEnabled()) }

            QueueUpTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QueueUpNavigationApp(
                        sessionManager = sessionManager,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { enabled ->
                            sessionManager.setDarkModeEnabled(enabled)
                            isDarkMode = enabled
                        }
                    )
                }
            }
        }
    }
}

// Connects ViewModels (VM) to Compose Screens (V) and manages screen navigation routes.
@Composable
fun QueueUpNavigationApp(
    sessionManager: SessionManager,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Create helper & ViewModel instances (MVVM Architecture)
    val notificationHelper = remember { NotificationHelper(context) }
    val inspectorViewModel = remember { ActivityInspectorViewModel(sessionManager) }
    val authViewModel = remember { AuthViewModel(sessionManager, inspectorViewModel) }
    val queueViewModel = remember { QueueViewModel(notificationHelper, inspectorViewModel) }
    val profileViewModel = remember { ProfileViewModel(sessionManager, inspectorViewModel) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            val userName = sessionManager.getUserName() ?: "Student"
            HomeScreen(
                queueViewModel = queueViewModel,
                inspectorViewModel = inspectorViewModel,
                userName = userName,
                onNavigateToServiceDetail = { serviceId ->
                    navController.navigate("service_detail/$serviceId")
                },
                onNavigateToHistory = {
                    navController.navigate("queue_history")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToInspector = {
                    navController.navigate("activity_inspector")
                }
            )
        }

        composable(
            route = "service_detail/{serviceId}",
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            ServiceDetailScreen(
                serviceId = serviceId,
                queueViewModel = queueViewModel,
                inspectorViewModel = inspectorViewModel,
                onBack = { navController.popBackStack() },
                onJoinedSuccess = {
                    navController.navigate("queue_status") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable("queue_status") {
            QueueStatusScreen(
                queueViewModel = queueViewModel,
                onBack = { navController.popBackStack() },
                onLeftQueue = { navController.popBackStack() }
            )
        }

        composable("queue_history") {
            QueueHistoryScreen(
                queueViewModel = queueViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                profileViewModel = profileViewModel,
                inspectorViewModel = inspectorViewModel,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { enabled ->
                    onToggleDarkMode(enabled)
                    inspectorViewModel.logEvent(
                        eventName = "Dark_Mode_Toggled",
                        summary = "Switched theme to ${if (enabled) "Dark Mode" else "Light Mode"}",
                        properties = mapOf("is_dark_mode" to enabled)
                    )
                },
                onNavigateToInspector = {
                    navController.navigate("activity_inspector")
                },
                onBack = { navController.popBackStack() },
                onLogoutDone = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("activity_inspector") {
            ActivityInspectorScreen(
                inspectorViewModel = inspectorViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
