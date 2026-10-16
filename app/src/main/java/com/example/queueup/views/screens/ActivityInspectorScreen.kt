// VIEW LAYER (V in MVVM): Dedicated screen for inspecting live app events, user profile sync, and JSON payloads.

package com.example.queueup.views.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.clevertap.CleverTapManager
import com.example.queueup.models.ActivityLogCategory
import com.example.queueup.viewmodels.ActivityInspectorViewModel
import com.example.queueup.views.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityInspectorScreen(
    inspectorViewModel: ActivityInspectorViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showProfileSnapshot by remember { mutableStateOf(false) }
    var showEventSandbox by remember { mutableStateOf(false) }

    val filteredLogs = remember(inspectorViewModel.activityLogs, inspectorViewModel.selectedFilter) {
        val filter = inspectorViewModel.selectedFilter
        if (filter == null) {
            inspectorViewModel.activityLogs
        } else {
            inspectorViewModel.activityLogs.filter { it.category == filter }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Activity & Payload Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time Event & Profile Telemetry",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { inspectorViewModel.clearAllLogs() }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Section 1: User Profile & Identity Snapshot Card
            item {
                Card(
                    onClick = { showProfileSnapshot = !showProfileSnapshot },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Current User Profile Snapshot",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (showProfileSnapshot) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (showProfileSnapshot) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = inspectorViewModel.getCurrentProfileJsonSnapshot(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Custom Event Test Sandbox
            item {
                Card(
                    onClick = { showEventSandbox = !showEventSandbox },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Custom Event Test Sandbox",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (showEventSandbox) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (showEventSandbox) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = inspectorViewModel.customEventName,
                                onValueChange = { inspectorViewModel.customEventName = it },
                                label = { Text("Event Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = inspectorViewModel.customPropertyKey,
                                    onValueChange = { inspectorViewModel.customPropertyKey = it },
                                    label = { Text("Property Key") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = inspectorViewModel.customPropertyValue,
                                    onValueChange = { inspectorViewModel.customPropertyValue = it },
                                    label = { Text("Property Value") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { inspectorViewModel.triggerCustomTestEvent() },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trigger Test Event")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { inspectorViewModel.triggerTestChargedPurchaseEvent() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Charged", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { inspectorViewModel.triggerIncrementQueuesJoined() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Increment (+1)", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        (context as? androidx.fragment.app.FragmentActivity)?.let {
                                            CleverTapManager.getInstance().promptPushPrimer(it)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Push Primer", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { CleverTapManager.getInstance().showAppInbox() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("App Inbox", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        com.example.queueup.clevertap.CleverTapCustomTemplates.syncTemplatesWithDashboard(
                                            CleverTapManager.getInstance().getCleverTapInstance()
                                        )
                                        inspectorViewModel.logSystemEvent(
                                            actionName = "Custom_Templates_Synced",
                                            summary = "Synced Campus_Queue_Alert_Banner to CleverTap dashboard",
                                            properties = mapOf("status" to "syncRegisteredInAppTemplates() invoked")
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync Templates", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        com.example.queueup.clevertap.CleverTapCustomTemplates.simulateLocalTemplate()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simulate In-App", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = inspectorViewModel.selectedFilter == null,
                        onClick = { inspectorViewModel.selectedFilter = null },
                        label = { Text("All (${inspectorViewModel.activityLogs.size})") }
                    )
                    FilterChip(
                        selected = inspectorViewModel.selectedFilter == ActivityLogCategory.EVENT,
                        onClick = { inspectorViewModel.selectedFilter = ActivityLogCategory.EVENT },
                        label = { Text("Events") }
                    )
                    FilterChip(
                        selected = inspectorViewModel.selectedFilter == ActivityLogCategory.PROFILE,
                        onClick = { inspectorViewModel.selectedFilter = ActivityLogCategory.PROFILE },
                        label = { Text("Profile") }
                    )
                    FilterChip(
                        selected = inspectorViewModel.selectedFilter == ActivityLogCategory.IDENTITY,
                        onClick = { inspectorViewModel.selectedFilter = ActivityLogCategory.IDENTITY },
                        label = { Text("Identity") }
                    )
                    FilterChip(
                        selected = inspectorViewModel.selectedFilter == ActivityLogCategory.SYSTEM,
                        onClick = { inspectorViewModel.selectedFilter = ActivityLogCategory.SYSTEM },
                        label = { Text("System") }
                    )
                }
            }

            // Section 4: Live Activity Log Stream
            if (filteredLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activity logs recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Badge + Action Name + Timestamp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeBg, badgeText) = when (log.category) {
                                        ActivityLogCategory.EVENT -> Pair(Color(0xFF3B82F6), Color.White)
                                        ActivityLogCategory.PROFILE -> Pair(Color(0xFF10B981), Color.White)
                                        ActivityLogCategory.IDENTITY -> Pair(Color(0xFF8B5CF6), Color.White)
                                        ActivityLogCategory.SYSTEM -> Pair(Color(0xFF64748B), Color.White)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = badgeBg,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = log.category.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = log.actionName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = log.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = log.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Formatted JSON Payload Box
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = log.payloadJson,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
