// FILE TYPE: UI Screen
// PURPOSE: Displays detailed info about a service counter and allows joining the queue.
// USED BY: MainActivity Navigation
// CALLS: QueueViewModel

package com.example.queueup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.ui.components.ErrorBanner
import com.example.queueup.ui.theme.PrimaryIndigo
import com.example.queueup.ui.theme.SecondaryTeal
import com.example.queueup.viewmodel.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    queueViewModel: QueueViewModel,
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
                title = { Text("Service Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (queueViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryIndigo
                )
            } else if (service != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = service.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Currently Waiting", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        "${service.currentWaitingCount} People",
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
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Average Service Time", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        "${service.averageServiceTime} mins per person",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    ErrorBanner(
                        errorMessage = queueViewModel.errorMessage,
                        modifier = Modifier.padding(top = 16.dp),
                        onDismiss = { queueViewModel.errorMessage = null }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            queueViewModel.joinQueue(service.id, onSuccess = onJoinedSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        enabled = service.isOpen && !queueViewModel.isLoading
                    ) {
                        Text(
                            text = if (service.isOpen) "Join Queue Token" else "Service Closed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
