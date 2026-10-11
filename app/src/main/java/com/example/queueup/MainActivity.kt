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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * MainActivity is the single Activity hosting the entire Jetpack Compose application.
 *
 * In modern Android development with Jetpack Compose, apps usually follow the "Single Activity" pattern.
 * Instead of creating a separate Activity for each screen (which consumes more memory and lifecycle complexity),
 * we have one MainActivity that hosts a NavHost (navigation container) swapping Composable screens.
 */
class MainActivity : ComponentActivity() {

    /**
     * onCreate() is the very first Android lifecycle method called when the system creates this Activity.
     * @param savedInstanceState: If the app was killed in the background, this bundle holds previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge() draws the UI behind the system status bar and navigation bar for a modern full-screen feel.
        enableEdgeToEdge()

        // Initialize SessionManager with the Android Application Context to read/write SharedPreferences safely.
        val sessionManager = SessionManager(applicationContext)

        // Provide sessionManager to ApiClient so Retrofit can attach JWT tokens to every outgoing HTTP request.
        ApiClient.init(sessionManager)

        // setContent is Compose's entry point, replacing the traditional XML layout setContentView(R.layout.activity_main).
        setContent {
            // remember { mutableStateOf(...) } creates an observable state variable that triggers UI recomposition on change.
            var isDarkMode by remember { mutableStateOf(sessionManager.isDarkModeEnabled()) }

            // QueueUpTheme provides dynamic colors, shapes, and typography to all nested composables.
            QueueUpTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QueueUpApp(
                        sessionManager = sessionManager,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { enabled ->
                            // Persist user preference to local storage and update Compose state
                            sessionManager.setDarkModeEnabled(enabled)
                            isDarkMode = enabled
                        }
                    )
                }
            }
        }
    }
}

/**
 * QueueUpApp is the master Composable setting up Dependency Injection and Navigation routes.
 *
 * @param sessionManager Encapsulates encrypted SharedPreferences for student authentication tokens.
 * @param isDarkMode Boolean flag controlling light/dark Material 3 color schemes.
 * @param onToggleDarkMode Callback lambda invoked when the user flips the dark mode switch in ProfileScreen.
 */
@Composable
fun QueueUpApp(
    sessionManager: SessionManager,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    // LocalContext.current retrieves the Android Context required by system services like NotificationManager.
    val context = LocalContext.current

    // rememberNavController creates and retains the NavHostController across screen recompositions.
    val navController = rememberNavController()

    // ---------------------------------------------------------------------------------------------
    // Dependency Wiring (Manual Dependency Injection):
    // In small-to-medium apps, Manual DI using `remember { ... }` avoids heavy boilerplate like Dagger/Hilt,
    // keeping class instantiations transparent, predictable, and easy for beginners to trace.
    // ---------------------------------------------------------------------------------------------
    val notificationService = remember { NotificationService(context) }
    val authRepository = remember { AuthRepository(sessionManager) }
    val serviceRepository = remember { ServiceRepository() }
    val queueRepository = remember { QueueRepository() }
    val profileRepository = remember { ProfileRepository(sessionManager) }

    // ViewModels survive screen rotation and encapsulate UI state away from Composable view functions.
    val authViewModel = remember { AuthViewModel(authRepository) }
    val homeViewModel = remember { HomeViewModel(serviceRepository, queueRepository, profileRepository) }
    val queueViewModel = remember { QueueViewModel(serviceRepository, queueRepository, notificationService) }
    val profileViewModel = remember { ProfileViewModel(profileRepository, authRepository) }

    // NavHost coordinates screen destinations and backstack history.
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
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
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
