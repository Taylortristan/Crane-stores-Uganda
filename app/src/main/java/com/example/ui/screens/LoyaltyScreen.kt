package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoyaltyTier
import com.example.data.model.LoyaltyVoucherEntity
import com.example.ui.components.LoyaltyTierBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyScreen(
    viewModel: CraneViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.loyaltyProfile.collectAsState()
    val vouchers by viewModel.loyaltyVouchers.collectAsState()
    val isSpinning by viewModel.isSpinningWheel.collectAsState()
    val spinRewardWon by viewModel.spinRewardWon.collectAsState()

    val currentPoints = profile?.pointsBalance ?: 0
    val currentTier = profile?.let { LoyaltyTier.fromPoints(it.lifetimePoints) } ?: LoyaltyTier.SILVER
    val nextTier = when (currentTier) {
        LoyaltyTier.BRONZE -> LoyaltyTier.SILVER
        LoyaltyTier.SILVER -> LoyaltyTier.GOLD
        LoyaltyTier.GOLD -> LoyaltyTier.PLATINUM
        LoyaltyTier.PLATINUM -> null
    }

    var showRedeemSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crane Club Rewards", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Member VIP Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        BrandPrimaryDark,
                                        BrandPrimary,
                                        Color(0xFF1E3A8A)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "CRANE CLUB VIP",
                                        color = BrandAccent,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        profile?.customerName ?: "Valued Customer",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                LoyaltyTierBadge(tier = currentTier)
                            }

                            // Center Balance
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "$currentPoints",
                                    color = Color.White,
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Crane Points",
                                    color = BrandAccentLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "≈ ${UgxFormatter.format((currentPoints * 10).toDouble())} Value",
                                    color = BrandTealLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // Tier Progress
                            if (nextTier != null) {
                                val progress = (profile?.lifetimePoints ?: 0).toFloat() / nextTier.minPoints.toFloat()
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Tier Progress",
                                            color = Slate300,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                            "${nextTier.minPoints - (profile?.lifetimePoints ?: 0)} pts to ${nextTier.tierName}",
                                            color = BrandAccent,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = BrandAccent,
                                        trackColor = Slate700
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Lucky Wheel Mini-Game
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Casino, contentDescription = null, tint = BrandAccent)
                                Text("Daily Lucky Crane Spin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandAccentLight
                            ) {
                                Text(
                                    "FREE DAILY",
                                    color = BrandAccentDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            "Spin every 24 hours to win bonus Crane Points and discount vouchers!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            textAlign = TextAlign.Center
                        )

                        // Animated Wheel Visual
                        val infiniteTransition = rememberInfiniteTransition(label = "SpinWheel")
                        val rotationAnim by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 300, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "WheelRotation"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .size(130.dp)
                                    .rotate(if (isSpinning) rotationAnim else 0f)
                            ) {
                                val colors = listOf(
                                    Color(0xFFEAB308),
                                    Color(0xFF10B981),
                                    Color(0xFFDC2626),
                                    Color(0xFF38BDF8),
                                    Color(0xFF8B5CF6),
                                    Color(0xFFF97316)
                                )
                                val sweep = 360f / colors.size
                                colors.forEachIndexed { i, color ->
                                    drawArc(
                                        color = color,
                                        startAngle = i * sweep,
                                        sweepAngle = sweep,
                                        useCenter = true
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Slate900,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = BrandAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.spinLoyaltyWheel() },
                            enabled = !isSpinning,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("spin_wheel_button")
                        ) {
                            if (isSpinning) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Spinning the Wheel...")
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.RotateRight, contentDescription = null)
                                    Text("Spin to Win Points", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section: Redeemable Loyalty Vouchers
            item {
                Text(
                    "Redeem Points for Vouchers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(vouchers, key = { it.id }) { voucher ->
                LoyaltyVoucherCard(
                    voucher = voucher,
                    userPoints = currentPoints,
                    onRedeem = {
                        viewModel.redeemLoyaltyVoucher(voucher) { success ->
                            if (success) {
                                showRedeemSuccessMessage = "Successfully redeemed ${voucher.code}! Copy and use at checkout."
                            }
                        }
                    }
                )
            }

            // Tier Benefits List
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "${currentTier.tierName} Perks",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        currentTier.perks.forEach { perk ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(16.dp))
                                Text(perk, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Spin Win Dialog
    if (spinRewardWon != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSpinReward() },
            icon = {
                Icon(
                    Icons.Default.Celebration,
                    contentDescription = null,
                    tint = BrandAccent,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "You Won +$spinRewardWon Points! 🎉",
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Your Crane points have been deposited to your account balance. Use them for instant discounts on local Ugandan products.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSpinReward() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Awesome!")
                }
            }
        )
    }

    // Voucher Redemption Success Alert
    if (showRedeemSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { showRedeemSuccessMessage = null },
            icon = {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BrandTealDark, modifier = Modifier.size(44.dp))
            },
            title = { Text("Voucher Unlocked!", fontWeight = FontWeight.Bold) },
            text = { Text(showRedeemSuccessMessage ?: "") },
            confirmButton = {
                Button(
                    onClick = { showRedeemSuccessMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun LoyaltyVoucherCard(
    voucher: LoyaltyVoucherEntity,
    userPoints: Int,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canRedeem = userPoints >= voucher.pointsRequired && !voucher.isRedeemed

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandAccentLight
                    ) {
                        Text(
                            voucher.code,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = BrandAccentDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        voucher.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    voucher.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Min spend: ${UgxFormatter.format(voucher.minSpendUgx)} • Exp: ${voucher.expiryDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (voucher.isRedeemed) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100
                ) {
                    Text(
                        "Redeemed",
                        color = Slate600,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick = onRedeem,
                    enabled = canRedeem,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        disabledContainerColor = Slate200
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Redeem", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${voucher.pointsRequired} Pts", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
