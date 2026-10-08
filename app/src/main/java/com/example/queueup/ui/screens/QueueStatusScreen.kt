// FILE TYPE: UI Screen
// PURPOSE: Dedicated Queue Status screen displaying position #, people ahead, estimated wait time, refresh & leave.
// USED BY: MainActivity Navigation
// CALLS: QueueViewModel

package com.example.queueup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.queueup.ui.components.ErrorBanner
import com.example.queueup.ui.components.QueueCard
import com.example.queueup.ui.theme.PrimaryIndigo
import com.example.queueup.viewmodel.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueStatusScreen(
    queueViewModel: QueueViewModel,
    onBack: () -> Unit,
    onLeftQueue: () -> Unit
) {
    LaunchedEffect(Unit) {
        queueViewModel.fetchActiveQueue()
    }

    val activeData = queueViewModel.activeQueueData

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Queue Status", fontWeight = FontWeight.Bold) },
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
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (queueViewModel.isLoading) {
                CircularProgressIndicator(color = PrimaryIndigo)
            } else if (activeData != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    ErrorBanner(
                        errorMessage = queueViewModel.errorMessage,
                        modifier = Modifier.padding(bottom = 12.dp),
                        onDismiss = { queueViewModel.errorMessage = null }
                    )
                    QueueCard(
                        activeQueueData = activeData,
                        onRefresh = { queueViewModel.refreshActiveQueue() },
                        onLeaveQueue = {
                            queueViewModel.leaveQueue(onLeftSuccess = onLeftQueue)
                        }
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "You are not currently in any queue",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Browse Services")
                    }
                }
            }
        }
    }
}
