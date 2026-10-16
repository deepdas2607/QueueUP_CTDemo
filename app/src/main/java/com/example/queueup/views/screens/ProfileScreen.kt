// VIEW LAYER (V in MVVM): User Profile screen showing personal info, queue stats, preferences, and inspector link.

package com.example.queueup.views.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.clevertap.CleverTapManager
import com.example.queueup.viewmodels.ActivityInspectorViewModel
import com.example.queueup.viewmodels.ProfileViewModel
import com.example.queueup.views.components.CustomTextField
import com.example.queueup.views.components.StatCard
import com.example.queueup.views.theme.AccentOrange
import com.example.queueup.views.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    inspectorViewModel: ActivityInspectorViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onNavigateToInspector: () -> Unit,
    onBack: () -> Unit,
    onLogoutDone: () -> Unit
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    val user = profileViewModel.user
    val stats = profileViewModel.userStats

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToInspector) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Open Payload Inspector",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (profileViewModel.isLoading && user == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // User Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = user?.name ?: "Student",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Role: ${user?.role ?: "USER"} • ${user?.occupation ?: "Student"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Queue Activity Statistics
                Text(
                    text = "Queue Activity Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Joined",
                        value = "${stats?.totalJoined ?: 0}",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed",
                        value = "${stats?.completed ?: 0}",
                        accentColor = SecondaryTeal,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Cancelled",
                        value = "${stats?.cancelled ?: 0}",
                        accentColor = AccentOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Editable Student Attributes
                Text(
                    text = "Edit Profile Attributes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                CustomTextField(
                    value = profileViewModel.editName,
                    onValueChange = { profileViewModel.editName = it },
                    label = "Full Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(10.dp))

                CustomTextField(
                    value = profileViewModel.editPhone,
                    onValueChange = { profileViewModel.editPhone = it },
                    label = "Phone Number",
                    leadingIcon = Icons.Default.Phone
                )

                Spacer(modifier = Modifier.height(10.dp))

                CustomTextField(
                    value = profileViewModel.editOccupation,
                    onValueChange = { profileViewModel.editOccupation = it },
                    label = "Department / Occupation",
                    leadingIcon = Icons.Default.Work
                )

                Spacer(modifier = Modifier.height(10.dp))

                CustomTextField(
                    value = profileViewModel.editInterests,
                    onValueChange = { profileViewModel.editInterests = it },
                    label = "Campus Interests",
                    leadingIcon = Icons.Default.Category
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { profileViewModel.saveUpdatedProfile() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile Attributes")
                }

                profileViewModel.statusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryTeal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Preferences & Toggles
                Text(
                    text = "Preferences & Consent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (isDarkMode) "OLED Dark theme active" else "Light theme active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Queue Turn Notifications", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Alerts when you reach #1 in line",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = profileViewModel.notificationsEnabled,
                        onCheckedChange = { profileViewModel.togglePushNotifications(it) }
                    )
                }

                val currentContext = LocalContext.current
                OutlinedButton(
                    onClick = {
                        (currentContext as? androidx.fragment.app.FragmentActivity)?.let { activity ->
                            CleverTapManager.getInstance().promptPushPrimer(activity)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Permission via Push Primer", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Analytics & Privacy Consent", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Consent to behavioral telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = profileViewModel.privacyConsentGiven,
                        onCheckedChange = { profileViewModel.togglePrivacyConsent(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Developer Tools Shortcut Card
                OutlinedCard(
                    onClick = onNavigateToInspector,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Activity & Payload Inspector",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inspect real-time event JSON payloads & profile sync",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { profileViewModel.logoutUser(onLogoutDone) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout Session")
                }
            }
        }
    }
}
