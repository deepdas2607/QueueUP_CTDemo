// FILE TYPE: Main Activity
// PURPOSE: Application entry point setting up Compose UI theme, Navigation, and ViewModels.
// USED BY: Android System
// CALLS: ViewModels, Navigation Screens, ApiClient, SessionManager (NO CleverTap dependencies in Core App)

package com.example.queueup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.queueup.data.remote.ApiClient
import com.example.queueup.data.repository.AuthRepository
import com.example.queueup.data.repository.ProfileRepository
import com.example.queueup.data.repository.QueueRepository
import com.example.queueup.data.repository.ServiceRepository
import com.example.queueup.notifications.NotificationService
import com.example.queueup.ui.screens.*
import com.example.queueup.ui.theme.QueueUpTheme
import com.example.queueup.utils.SessionManager
import com.example.queueup.viewmodel.AuthViewModel
import com.example.queueup.viewmodel.HomeViewModel
import com.example.queueup.viewmodel.ProfileViewModel
import com.example.queueup.viewmodel.QueueViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        ApiClient.init(sessionManager)

        setContent {
            QueueUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QueueUpApp(sessionManager = sessionManager)
                }
            }
        }
    }
}

@Composable
fun QueueUpApp(sessionManager: SessionManager) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Dependency Wiring (Manual DI to keep architecture simple)
    val notificationService = remember { NotificationService(context) }
    val authRepository = remember { AuthRepository(sessionManager) }
    val serviceRepository = remember { ServiceRepository() }
    val queueRepository = remember { QueueRepository() }
    val profileRepository = remember { ProfileRepository(sessionManager) }

    val authViewModel = remember { AuthViewModel(authRepository) }
    val homeViewModel = remember { HomeViewModel(serviceRepository, queueRepository, profileRepository) }
    val queueViewModel = remember { QueueViewModel(serviceRepository, queueRepository, notificationService) }
    val profileViewModel = remember { ProfileViewModel(profileRepository, authRepository) }

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
                homeViewModel = homeViewModel,
                queueViewModel = queueViewModel,
                userName = userName,
                onNavigateToServiceDetail = { serviceId ->
                    navController.navigate("service_detail/$serviceId")
                },
                onNavigateToQueueStatus = {
                    navController.navigate("queue_status")
                },
                onNavigateToHistory = {
                    navController.navigate("queue_history")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
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
                onBack = { navController.popBackStack() },
                onLogoutDone = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
