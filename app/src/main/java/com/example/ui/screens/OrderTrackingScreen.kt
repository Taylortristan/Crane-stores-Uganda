package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.SimulatedDriverRouteMap
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    viewModel: CraneViewModel,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val selectedOrderId by viewModel.selectedOrderId.collectAsState()
    val orderItems by viewModel.selectedOrderItems.collectAsState()

    var showOrderPickerSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live Order Tracking", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            selectedOrder?.id?.let { "Order #$it" } ?: "Select an order",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showOrderPickerSheet = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("All Orders (${allOrders.size})", fontWeight = FontWeight.Bold, color = BrandPrimary)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BrandPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (selectedOrder == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Slate400, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Active Orders Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Place an order from the shop to track real-time delivery.", color = Slate600, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            val order = selectedOrder!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live GPS Map Simulation
                item {
                    SimulatedDriverRouteMap(
                        progress = order.driverProgress,
                        riderName = order.riderName,
                        destinationAddress = "${order.deliveryAddress}, ${order.deliveryCity}"
                    )
                }

                // Status Banner Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Order Status", style = MaterialTheme.typography.labelSmall, color = Slate600)
                                    Text(
                                        order.status.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (order.status) {
                                                OrderStatus.DELIVERED -> BrandTealDark
                                                OrderStatus.OUT_FOR_DELIVERY -> BrandPrimary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    )
                                }
                                OrderStatusChip(status = order.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = order.status.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Slate200)
                            Spacer(modifier = Modifier.height(16.dp))

                            // 5-Stage Stepper
                            TrackingStepper(currentStatus = order.status)
                        }
                    }
                }

                // Driver Dispatch Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Delivery Courier", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrandPrimaryLight,
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TwoWheeler,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(order.riderName, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = BrandTealDark, modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        order.riderPlate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(14.dp))
                                        Text(" ${order.riderRating} • Crane Certified Driver", style = MaterialTheme.typography.labelSmall, color = Slate600)
                                    }
                                }

                                // Quick Call Action
                                IconButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${order.riderPhone}")
                                        }
                                        try { context.startActivity(dialIntent) } catch (e: Exception) {}
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(BrandPrimary, shape = CircleShape)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Driver", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Delivery Destination & Instructions
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Delivery Address", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(order.deliveryAddress, fontWeight = FontWeight.Medium)
                                    Text("${order.deliveryCity}, Uganda", color = Slate600, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (order.customerNotes.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate50,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Note: ${order.customerNotes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Order Items & Payment Receipt
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Receipt Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    UgxFormatter.formatDateTime(order.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                            }

                            Text(
                                "Items: ${order.itemsSummary}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate800
                            )

                            HorizontalDivider(color = Slate200)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Method", color = Slate600)
                                Text(order.paymentMethod, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transaction Ref", color = Slate600)
                                Text(order.transactionRef, style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Crane Points Earned", color = BrandAccentDark, fontWeight = FontWeight.Bold)
                                Text("+${order.pointsEarned} Pts", color = BrandAccentDark, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(color = Slate200)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Paid", fontWeight = FontWeight.ExtraBold)
                                Text(
                                    UgxFormatter.format(order.totalUgx),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for choosing past/other orders
    if (showOrderPickerSheet) {
        AlertDialog(
            onDismissRequest = { showOrderPickerSheet = false },
            title = { Text("Your Orders", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allOrders) { ord ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (ord.id == selectedOrderId) BrandPrimaryLight else Slate50
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectOrderForTracking(ord.id)
                                    showOrderPickerSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("#${ord.id}", fontWeight = FontWeight.Bold)
                                    Text(ord.itemsSummary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    Text(UgxFormatter.format(ord.totalUgx), fontWeight = FontWeight.Bold, color = BrandPrimary)
                                }
                                OrderStatusChip(status = ord.status)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOrderPickerSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun TrackingStepper(
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        OrderStatus.PLACED,
        OrderStatus.CONFIRMED,
        OrderStatus.PACKING,
        OrderStatus.OUT_FOR_DELIVERY,
        OrderStatus.DELIVERED
    )

    val currentStepIndex = currentStatus.stepIndex

    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Stepper Dot & Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> BrandPrimary
                                    else -> Slate200
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text("${index + 1}", color = Slate600, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(28.dp)
                                .background(if (index < currentStepIndex) BrandPrimary else Slate200)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 16.dp else 0.dp)) {
                    Text(
                        text = step.title,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) BrandPrimary else if (isCompleted) Slate900 else Slate400,
                        fontSize = 14.sp
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) Slate600 else Slate400,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
