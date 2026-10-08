// FILE TYPE: Reusable UI Component
// PURPOSE: Renders active queue status card showing Position #, People Ahead, and Estimated Wait Time.
// USED BY: HomeScreen, QueueStatusScreen
// DATA SOURCE: ActiveQueueData

package com.example.queueup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueup.data.model.ActiveQueueData
import com.example.queueup.ui.theme.PrimaryIndigo
import com.example.queueup.ui.theme.SecondaryTeal

@Composable
fun QueueCard(
    activeQueueData: ActiveQueueData,
    onRefresh: () -> Unit,
    onLeaveQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = activeQueueData.queueEntry
    val service = entry.service

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val isUserTurn = entry.position == 1 && activeQueueData.peopleAhead == 0
                    Text(
                        text = if (isUserTurn) "Status: READY (Your Turn)" else "Status: WAITING",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isUserTurn) MaterialTheme.colorScheme.primary else SecondaryTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Queue",
                        tint = PrimaryIndigo
                    )
                }
            }

            val isUserTurn = entry.position == 1 && activeQueueData.peopleAhead == 0
            if (isUserTurn) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SecondaryTeal.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SecondaryTeal.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎉 It's Your Turn! Please proceed to the counter.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prominent Position Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isUserTurn) SecondaryTeal.copy(alpha = 0.15f) else PrimaryIndigo.copy(alpha = 0.1f))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isUserTurn) "Now Serving" else "Your Position",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isUserTurn) SecondaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isUserTurn) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "#${entry.position}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUserTurn) SecondaryTeal else PrimaryIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "People ahead",
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "People Ahead",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${activeQueueData.peopleAhead}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Estimated wait",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Est. Wait Time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${activeQueueData.estimatedWaitMinutes} min",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onLeaveQueue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Leave Queue")
            }
        }
    }
}
