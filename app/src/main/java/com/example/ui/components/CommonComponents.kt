package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.LoyaltyTier
import com.example.data.model.OrderStatus
import com.example.ui.theme.*
import com.example.util.UgxFormatter
import java.io.File

@Composable
fun ProductImageView(
    imageUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    when {
        imageUri == "hero_uganda_market" -> {
            Image(
                painter = painterResource(id = R.drawable.hero_uganda_market),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        imageUri == "ic_crane_logo" -> {
            Image(
                painter = painterResource(id = R.drawable.ic_crane_logo),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        imageUri.startsWith("/") -> {
            val file = remember(imageUri) { File(imageUri) }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(file)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}

@Composable
fun UgxPriceDisplay(
    priceUgx: Double,
    discountPercent: Int = 0,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val finalPrice = if (discountPercent > 0) priceUgx * (1.0 - discountPercent / 100.0) else priceUgx

    Column(modifier = modifier) {
        if (discountPercent > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = UgxFormatter.format(priceUgx),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = if (large) 13.sp else 11.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = UgandaRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "-$discountPercent%",
                        color = UgandaRed,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Text(
            text = UgxFormatter.format(finalPrice),
            style = if (large) MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandPrimary
            ) else MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun OrderStatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        OrderStatus.PLACED -> Triple(BrandAccentLight, BrandAccentDark, Icons.Default.Receipt)
        OrderStatus.CONFIRMED -> Triple(BrandPrimaryLight, BrandPrimary, Icons.Default.CheckCircle)
        OrderStatus.PACKING -> Triple(Color(0xFFFEF08A), Color(0xFF854D0E), Icons.Default.Inventory2)
        OrderStatus.OUT_FOR_DELIVERY -> Triple(BrandTealLight, BrandTealDark, Icons.Default.TwoWheeler)
        OrderStatus.DELIVERED -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.Verified)
        OrderStatus.CANCELLED -> Triple(UgandaRedLight, UgandaRed, Icons.Default.Cancel)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status.title,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun LoyaltyTierBadge(
    tier: LoyaltyTier,
    modifier: Modifier = Modifier
) {
    val tierColor = Color(tier.badgeColorHex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tierColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = tierColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = tier.tierName,
                color = tierColor,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
fun SimulatedDriverRouteMap(
    progress: Float,
    riderName: String,
    destinationAddress: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Pulse"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Road grid lines (Stylized Kampala Map)
                val gridColor = Color(0xFF1E293B)
                for (x in 0..width.toInt() step 60) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..height.toInt() step 50) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y.toFloat()),
                        end = Offset(width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }

                // Delivery route path (from Nakasero Hub -> Kololo / Destination)
                val p0 = Offset(width * 0.15f, height * 0.80f) // Crane Hub
                val p1 = Offset(width * 0.40f, height * 0.55f) // Jinja Rd / Wandegeya
                val p2 = Offset(width * 0.65f, height * 0.40f) // Acacia Ave
                val p3 = Offset(width * 0.85f, height * 0.25f) // Customer gate

                // Draw background road
                val roadColor = Color(0xFF334155)
                drawLine(roadColor, p0, p1, strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(roadColor, p1, p2, strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(roadColor, p2, p3, strokeWidth = 8f, cap = StrokeCap.Round)

                // Draw dashed active route
                val pathColor = Color(0xFF10B981)
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                drawLine(pathColor, p0, p1, strokeWidth = 4f, pathEffect = dashedEffect)
                drawLine(pathColor, p1, p2, strokeWidth = 4f, pathEffect = dashedEffect)
                drawLine(pathColor, p2, p3, strokeWidth = 4f, pathEffect = dashedEffect)

                // Draw Origin Hub Point
                drawCircle(color = Color(0xFFF59E0B), radius = 10f, center = p0)
                drawCircle(color = Color.White, radius = 5f, center = p0)

                // Draw Destination Point
                drawCircle(color = Color(0xFFEF4444), radius = 12f, center = p3)
                drawCircle(color = Color.White, radius = 6f, center = p3)

                // Calculate current rider position based on progress (0.0 to 1.0)
                val currentRiderPos = when {
                    progress < 0.33f -> {
                        val segProgress = progress / 0.33f
                        Offset(
                            p0.x + (p1.x - p0.x) * segProgress,
                            p0.y + (p1.y - p0.y) * segProgress
                        )
                    }
                    progress < 0.66f -> {
                        val segProgress = (progress - 0.33f) / 0.33f
                        Offset(
                            p1.x + (p2.x - p1.x) * segProgress,
                            p1.y + (p2.y - p1.y) * segProgress
                        )
                    }
                    else -> {
                        val segProgress = (progress - 0.66f) / 0.34f
                        Offset(
                            p2.x + (p3.x - p2.x) * segProgress,
                            p2.y + (p3.y - p2.y) * segProgress
                        )
                    }
                }

                // Draw Rider Pulse Radar
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.35f),
                    radius = pulseRadius,
                    center = currentRiderPos,
                    style = Stroke(width = 2f)
                )

                // Draw Rider Marker
                drawCircle(color = Color(0xFF10B981), radius = 14f, center = currentRiderPos)
                drawCircle(color = Color.White, radius = 7f, center = currentRiderPos)
            }

            // Map overlays: Top Hub badge and Bottom Destination Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate800.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandTeal)
                        )
                        Text(
                            text = "LIVE BODA GPS TRACKING",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandAccent.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "ETA: ~${((1f - progress) * 25).toInt().coerceAtLeast(3)} mins",
                        color = Slate950,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Driver Status Bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                color = Slate800.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = BrandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = riderName,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "En route to $destinationAddress",
                                color = Slate400,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = BrandTeal,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}
