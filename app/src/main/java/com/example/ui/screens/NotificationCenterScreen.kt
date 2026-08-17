package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationItemEntity
import com.example.data.model.NotificationType
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: CraneViewModel,
    onNavigateToOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications & Alerts", fontWeight = FontWeight.Bold) },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Text("Mark All Read", color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Notifications Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("You will receive real-time order updates and Crane Club reward alerts here.", color = Slate600, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        item = notification,
                        onClick = {
                            viewModel.markNotificationAsRead(notification.id)
                            if (notification.relatedOrderId != null) {
                                viewModel.selectOrderForTracking(notification.relatedOrderId)
                                onNavigateToOrder(notification.relatedOrderId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    item: NotificationItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconTint, iconBg) = when (item.type) {
        NotificationType.ORDER_STATUS -> Triple(Icons.Default.TwoWheeler, BrandAccentDark, BrandAccentLight)
        NotificationType.LOYALTY_REWARD -> Triple(Icons.Default.Stars, BrandAccentDark, Color(0xFFFEF9C3))
        NotificationType.PROMOTION -> Triple(Icons.Default.LocalOffer, BrandPrimary, BrandPrimaryLight)
        NotificationType.PAYMENT_CONFIRMATION -> Triple(Icons.Default.CheckCircle, BrandTealDark, BrandTealLight)
        NotificationType.SYSTEM -> Triple(Icons.Default.Info, Slate700, Slate100)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) MaterialTheme.colorScheme.surface else Slate50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!item.isRead) 2.dp else 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.title,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!item.isRead) MaterialTheme.colorScheme.onSurface else Slate700
                    )
                    if (!item.isRead) {
                        Surface(
                            shape = CircleShape,
                            color = BrandAccent,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    UgxFormatter.formatDateTime(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }
    }
}
