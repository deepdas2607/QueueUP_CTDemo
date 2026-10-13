// VIEW LAYER (V in MVVM): Main Home Dashboard displaying Active Queue Token, Category Chips, and 12 Campus Counters.

package com.example.queueup.views.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.queueup.viewmodels.ActivityInspectorViewModel
import com.example.queueup.viewmodels.QueueViewModel
import com.example.queueup.views.components.CampusInboxSheet
import com.example.queueup.views.components.QueueCard
import com.example.queueup.views.components.ServiceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    queueViewModel: QueueViewModel,
    inspectorViewModel: ActivityInspectorViewModel,
    userName: String,
    onNavigateToServiceDetail: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInspector: () -> Unit
) {
    var showAnnouncementsSheet by remember { mutableStateOf(false) }
    val categories = listOf("All", "Academic", "Admin & Finance", "Campus Life", "Tech & Health")

    LaunchedEffect(Unit) {
        queueViewModel.loadHomeDashboardData()
    }

    if (showAnnouncementsSheet) {
        CampusInboxSheet(
            announcements = inspectorViewModel.announcements,
            onDismiss = { showAnnouncementsSheet = false },
            onMarkAllRead = { inspectorViewModel.markAllAnnouncementsRead() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, $userName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "QueueUp Campus Services",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // 1. Live Activity & Payload Inspector Icon
                    IconButton(onClick = onNavigateToInspector) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Live Activity & Payload Inspector",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 2. Campus Announcements / Inbox Bell Icon
                    IconButton(onClick = {
                        showAnnouncementsSheet = true
                        inspectorViewModel.logEvent(
                            eventName = "Announcements_Opened",
                            summary = "Opened Campus Announcements drawer",
                            properties = mapOf("unread_count" to inspectorViewModel.announcements.count { it.isUnread })
                        )
                    }) {
                        val hasUnread = inspectorViewModel.announcements.any { it.isUnread }
                        BadgedBox(
                            badge = {
                                if (hasUnread) {
                                    Badge(containerColor = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Announcements")
                        }
                    }

                    // 3. Refresh Services & Active Queue
                    IconButton(onClick = { queueViewModel.loadHomeDashboardData() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Dashboard")
                    }

                    // 4. Queue History
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Queue History")
                    }

                    // 5. User Profile & Settings
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile & Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // Active Queue Token Section (if user is currently waiting in a line)
            queueViewModel.activeQueueData?.let { activeData ->
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active Queue Token",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    QueueCard(
                        activeQueueData = activeData,
                        onRefreshAndAdvance = { queueViewModel.refreshAndAdvanceQueueTurn() },
                        onCompleteService = { queueViewModel.completeActiveQueue() },
                        onLeaveQueue = { queueViewModel.leaveActiveQueue() }
                    )
                }
            }

            // Helpful Campus Tip Banner
            if (queueViewModel.queueTips.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Campus Queue Tip",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = queueViewModel.queueTips.first(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Available Services Header + Category Filter Chips
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available Campus Counters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = queueViewModel.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                queueViewModel.selectedCategory = category
                                inspectorViewModel.logEvent(
                                    eventName = "Category_Filtered",
                                    summary = "Selected counter category: $category",
                                    properties = mapOf("category" to category)
                                )
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // Services List or Loading State
            val filteredServices = queueViewModel.getFilteredServices()

            if (queueViewModel.isLoading && filteredServices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (filteredServices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No campus counters found in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredServices) { service ->
                    ServiceCard(
                        service = service,
                        onClick = { onNavigateToServiceDetail(service.id) }
                    )
                }
            }
        }
    }
}
