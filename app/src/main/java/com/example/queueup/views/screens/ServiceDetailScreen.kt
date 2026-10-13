// VIEW LAYER (V in MVVM): Detailed view of a campus counter allowing the student to join the line.

package com.example.queueup.views.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.viewmodels.ActivityInspectorViewModel
import com.example.queueup.viewmodels.QueueViewModel
import com.example.queueup.views.components.ErrorBanner
import com.example.queueup.views.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    queueViewModel: QueueViewModel,
    inspectorViewModel: ActivityInspectorViewModel,
    onBack: () -> Unit,
    onJoinedSuccess: () -> Unit
) {
    LaunchedEffect(serviceId) {
        queueViewModel.loadServiceDetail(serviceId)
    }

    val service = queueViewModel.selectedService

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Counter Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        service?.let {
                            inspectorViewModel.logEvent(
                                eventName = "Counter_Bookmarked",
                                summary = "Saved ${it.name} to favorites",
                                properties = mapOf("service_id" to it.id, "service_name" to it.name)
                            )
                        }
                    }) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "Bookmark Counter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            if (queueViewModel.isLoading && service == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (service != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Category Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = service.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = service.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Metrics Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Currently Waiting", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "${service.currentWaitingCount} Students In Line",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Estimated Turn Time", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "~${service.currentWaitingCount * service.averageServiceTime} mins (${service.averageServiceTime}m/person)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    ErrorBanner(
                        errorMessage = queueViewModel.errorMessage,
                        onDismiss = { queueViewModel.errorMessage = null },
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            queueViewModel.joinQueueForService(service.id, onJoinedSuccess = onJoinedSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = service.isOpen && !queueViewModel.isLoading
                    ) {
                        Text(
                            text = if (service.isOpen) "Join Queue Token" else "Counter Closed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
