// ENTRY POINT: Initializes ApiClient, ViewModels (VM), and Jetpack Compose Navigation between Views (V).

package com.example.queueup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.example.queueup.clevertap.CleverTapManager
import com.example.queueup.clevertap.QueueCleverTapBridge
import com.example.queueup.network.ApiClient
import com.example.queueup.network.NotificationHelper
import com.example.queueup.network.SessionManager
import com.example.queueup.viewmodels.ActivityInspectorViewModel
import com.example.queueup.viewmodels.AuthViewModel
import com.example.queueup.viewmodels.ProfileViewModel
import com.example.queueup.viewmodels.QueueViewModel
import com.example.queueup.views.screens.*
import com.example.queueup.views.theme.QueueUpTheme

class MainActivity : FragmentActivity(), CTPushNotificationListener, InAppNotificationButtonListener {

    private var inspectorViewModelRef: ActivityInspectorViewModel? = null

    // Called by Android when the app launches; sets up SessionManager, ApiClient, and the Compose UI.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        ApiClient.init(sessionManager)

        // Register CleverTap Push Notification and In-App Action listeners
        val cleverTap = CleverTapManager.getInstance().getCleverTapInstance()
        cleverTap?.setCTPushNotificationListener(this)
        cleverTap?.setInAppNotificationButtonListener(this)

        // On Android 13+ (API 33+), prompt CleverTap Local In-App Push Primer if notification permission is not granted
        promptPushPrimerIfRequired()

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
                        },
                        onInspectorAttached = { vm ->
                            inspectorViewModelRef = vm
                        }
                    )
                }
            }
        }
    }

    // Displays CleverTap Local In-App Push Primer dialog on Android 13+ devices
    private fun promptPushPrimerIfRequired() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                CleverTapManager.getInstance().promptPushPrimer(this)
            }
        }
    }

    // Callback when a CleverTap push notification is clicked by the user
    override fun onNotificationClickedPayloadReceived(payload: java.util.HashMap<String, Any>?) {
        Log.d("MainActivity", "CleverTap Push clicked with payload: $payload")
        val safeMap = payload?.mapValues { it.value } ?: emptyMap()
        inspectorViewModelRef?.logEvent(
            eventName = "Push_Notification_Clicked",
            summary = "User opened push notification",
            properties = safeMap
        )
    }

    // Callback when an interactive button in a CleverTap In-App message is clicked
    override fun onInAppButtonClick(payload: java.util.HashMap<String, String>?) {
        Log.d("MainActivity", "CleverTap In-App button clicked: $payload")
        val safeMap = payload?.mapValues { it.value } ?: emptyMap()
        inspectorViewModelRef?.logEvent(
            eventName = "InApp_Button_Clicked",
            summary = "User clicked In-App button action",
            properties = safeMap
        )
    }
}

// Connects ViewModels (VM) to Compose Screens (V) and manages screen navigation routes.
@Composable
fun QueueUpNavigationApp(
    sessionManager: SessionManager,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onInspectorAttached: (ActivityInspectorViewModel) -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Create helper & ViewModel instances (MVVM Architecture)
    val notificationHelper = remember { NotificationHelper(context) }
    val inspectorViewModel = remember { ActivityInspectorViewModel(sessionManager) }
    val authViewModel = remember { AuthViewModel(sessionManager, inspectorViewModel) }
    val queueViewModel = remember { QueueViewModel(notificationHelper, inspectorViewModel) }
    val profileViewModel = remember { ProfileViewModel(sessionManager, inspectorViewModel) }

    LaunchedEffect(inspectorViewModel) {
        onInspectorAttached(inspectorViewModel)

        // Connect custom template logging to ActivityInspectorViewModel
        com.example.queueup.clevertap.CleverTapCustomTemplates.onTemplateEventLogged = { eventName, summary, props ->
            inspectorViewModel.logEvent(eventName, summary, props)
        }

        // Sync FCM token with CleverTap and log in inspector for live push verification
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    sessionManager.saveFcmToken(token)
                    com.example.queueup.clevertap.CleverTapManager.getInstance().pushFcmToken(token)
                    inspectorViewModel.logSystemEvent(
                        actionName = "FCM_Push_Token_Registered",
                        summary = "FCM token synced with CleverTap for mobile push delivery",
                        properties = mapOf(
                            "fcm_token" to token,
                            "token_preview" to token.take(24) + "..."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Firebase Messaging not initialized or unavailable", e)
        }
    }

    // Restore CleverTap session if the user already has saved credentials on the device
    LaunchedEffect(Unit) {
        authViewModel.restoreSessionIfLoggedIn()
    }

    // Automatically records screen views in CleverTap SDK per documentation
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: "Unknown"
            val screenName = when {
                route.startsWith("service_detail") -> "Service Detail"
                route == "splash" -> "Splash"
                route == "login" -> "Login"
                route == "register" -> "Register"
                route == "home" -> "Home"
                route == "queue_status" -> "Queue Status"
                route == "queue_history" -> "Queue History"
                route == "profile" -> "Profile"
                route == "activity_inspector" -> "Activity Inspector"
                else -> route
            }
            QueueCleverTapBridge.onScreenViewed(screenName)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
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

    // CleverTap Custom Code In-App Banner overlay
    com.example.queueup.clevertap.CleverTapCustomTemplates.activeCustomTemplate?.let { template ->
        com.example.queueup.views.components.CustomInAppBanner(
            model = template,
            onAction = {
                com.example.queueup.clevertap.CleverTapCustomTemplates.triggerAction(context)
            },
            onDismiss = {
                com.example.queueup.clevertap.CleverTapCustomTemplates.dismissActiveTemplate()
            },
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
        )
    }
    }
}
