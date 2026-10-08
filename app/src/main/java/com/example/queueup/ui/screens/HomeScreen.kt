// FILE TYPE: UI Screen
// PURPOSE: Home dashboard showing greeting, active queue banner (if joined), and available services list.
// USED BY: MainActivity Navigation
// CALLS: HomeViewModel, QueueViewModel

package com.example.queueup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.queueup.ui.components.QueueCard
import com.example.queueup.ui.components.ServiceCard
import com.example.queueup.ui.theme.PrimaryIndigo
import com.example.queueup.viewmodel.HomeViewModel
import com.example.queueup.viewmodel.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    queueViewModel: QueueViewModel,
    userName: String,
    onNavigateToServiceDetail: (String) -> Unit,
    onNavigateToQueueStatus: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    LaunchedEffect(Unit) {
        homeViewModel.loadHomeData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hello, $userName 👋",
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
                    IconButton(onClick = { homeViewModel.loadHomeData() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Services")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Queue History")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Active Queue Section
            homeViewModel.activeQueue?.let { activeData ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Active Queue Token",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                QueueCard(
                    activeQueueData = activeData,
                    onRefresh = { homeViewModel.loadHomeData() },
                    onLeaveQueue = {
                        queueViewModel.activeQueueData = activeData
                        queueViewModel.leaveQueue {
                            homeViewModel.loadHomeData()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Queue Tips Banner
            if (homeViewModel.queueTips.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryIndigo.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Campus Tip",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = homeViewModel.queueTips.first(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Available Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (homeViewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else if (homeViewModel.services.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No services available right now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(homeViewModel.services) { service ->
                        ServiceCard(
                            service = service,
                            onClick = { onNavigateToServiceDetail(service.id) }
                        )
                    }
                }
            }
        }
    }
}
