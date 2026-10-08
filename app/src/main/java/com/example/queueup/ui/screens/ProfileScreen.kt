// FILE TYPE: UI Screen
// PURPOSE: Displays user details, queue statistics, notifications/privacy toggles, and logout.
// USED BY: MainActivity Navigation
// CALLS: ProfileViewModel

package com.example.queueup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.ui.components.CustomTextField
import com.example.queueup.ui.components.StatCard
import com.example.queueup.ui.theme.AccentOrange
import com.example.queueup.ui.theme.PrimaryIndigo
import com.example.queueup.ui.theme.SecondaryTeal
import com.example.queueup.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onBack: () -> Unit,
    onLogoutDone: () -> Unit
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
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
            if (profileViewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                // Profile Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = user?.name ?: "User",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Role: ${user?.role ?: "USER"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Queue Statistics
                Text(
                    text = "Queue Activity Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Joined",
                        value = "${stats?.totalJoined ?: 0}",
                        accentColor = PrimaryIndigo,
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

                // Edit Profile Fields
                Text(
                    text = "Edit Profile Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = profileViewModel.editName,
                    onValueChange = { profileViewModel.editName = it },
                    label = "Full Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = profileViewModel.editOccupation,
                    onValueChange = { profileViewModel.editOccupation = it },
                    label = "Occupation",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = profileViewModel.editInterests,
                    onValueChange = { profileViewModel.editInterests = it },
                    label = "Interests",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { profileViewModel.updateProfileInfo() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Preferences & Settings
                Text(
                    text = "Preferences",
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
                            text = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Turn Notifications", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = profileViewModel.notificationsEnabled,
                        onCheckedChange = { profileViewModel.toggleNotifications(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Analytics & Privacy Consent", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = profileViewModel.privacyConsentGiven,
                        onCheckedChange = { profileViewModel.togglePrivacyConsent(it) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { profileViewModel.logout(onLogoutDone) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}
