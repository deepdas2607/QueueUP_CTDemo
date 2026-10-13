// VIEW LAYER (V in MVVM): Dedicated Queue Status screen showing live ticket #, turn progress, advance, and leave.

package com.example.queueup.views.screens

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
import com.example.queueup.viewmodels.QueueViewModel
import com.example.queueup.views.components.ErrorBanner
import com.example.queueup.views.components.QueueCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueStatusScreen(
    queueViewModel: QueueViewModel,
    onBack: () -> Unit,
    onLeftQueue: () -> Unit
) {
    LaunchedEffect(Unit) {
        queueViewModel.fetchActiveQueueStatus()
    }

    val activeData = queueViewModel.activeQueueData

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Queue Status", fontWeight = FontWeight.Bold) },
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
            if (queueViewModel.isLoading && activeData == null) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (activeData != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    ErrorBanner(
                        errorMessage = queueViewModel.errorMessage,
                        onDismiss = { queueViewModel.errorMessage = null },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    QueueCard(
                        activeQueueData = activeData,
                        onRefreshAndAdvance = { queueViewModel.refreshAndAdvanceQueueTurn() },
                        onCompleteService = { queueViewModel.completeActiveQueue(onCompleted = onLeftQueue) },
                        onLeaveQueue = { queueViewModel.leaveActiveQueue(onLeftQueue = onLeftQueue) }
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
                    Button(onClick = onBack) {
                        Text("Browse Campus Counters")
                    }
                }
            }
        }
    }
}
