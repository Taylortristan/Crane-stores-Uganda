package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItemWithProduct
import com.example.data.model.DeliveryMethod
import com.example.data.model.PaymentMethod
import com.example.ui.components.ProductImageView
import com.example.ui.components.UgxPriceDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    viewModel: CraneViewModel,
    onNavigateBack: () -> Unit,
    onOrderSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val appliedVoucher by viewModel.appliedVoucher.collectAsState()
    val voucherError by viewModel.voucherError.collectAsState()

    // No auto-filled personal data
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Kampala") }
    var customerNotes by remember { mutableStateOf("") }
    var voucherCodeInput by remember { mutableStateOf("") }

    var checkoutValidationError by remember { mutableStateOf<String?>(null) }

    var selectedDeliveryMethod by remember { mutableStateOf(DeliveryMethod.BODA_EXPRESS) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.MTN_MOMO) }

    val cities = listOf("Kampala", "Entebbe", "Wakiso", "Jinja", "Mbarara", "Gulu", "Mbale", "Masaka")

    val subtotal = cartItems.sumOf { it.totalPriceUgx }
    val deliveryFee = selectedDeliveryMethod.baseFeeUgx
    val discount = appliedVoucher?.discountAmountUgx ?: 0.0
    val grandTotal = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
    val pointsToEarn = (grandTotal / 1000).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart & Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear cart", tint = UgandaRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (checkoutValidationError != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = UgandaRedLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = UgandaRed, modifier = Modifier.size(16.dp))
                                    Text(
                                        checkoutValidationError ?: "",
                                        color = UgandaRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Grand Total", style = MaterialTheme.typography.labelMedium, color = Slate600)
                                Text(
                                    UgxFormatter.format(grandTotal),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandPrimaryDark
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandAccentLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = BrandAccentDark, modifier = Modifier.size(14.dp))
                                    Text("+$pointsToEarn Points", color = BrandAccentDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (customerName.trim().isBlank()) {
                                    checkoutValidationError = "Please enter the recipient's name."
                                    return@Button
                                }
                                if (customerPhone.trim().isBlank() || customerPhone.length < 8) {
                                    checkoutValidationError = "Please enter a valid Ugandan phone number (e.g. +256...)."
                                    return@Button
                                }
                                if (deliveryAddress.trim().isBlank()) {
                                    checkoutValidationError = "Please enter your delivery street address."
                                    return@Button
                                }

                                checkoutValidationError = null

                                viewModel.initiateCheckout(
                                    customerName = customerName.trim(),
                                    customerPhone = customerPhone.trim(),
                                    deliveryAddress = deliveryAddress.trim(),
                                    city = selectedCity,
                                    deliveryMethod = selectedDeliveryMethod,
                                    paymentMethod = selectedPaymentMethod,
                                    customerNotes = customerNotes.trim()
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("place_order_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (selectedPaymentMethod) {
                                    PaymentMethod.MTN_MOMO -> CraneMomoYellow
                                    PaymentMethod.AIRTEL_MONEY -> UgandaRed
                                    else -> BrandPrimary
                                },
                                contentColor = when (selectedPaymentMethod) {
                                    PaymentMethod.MTN_MOMO -> Slate950
                                    else -> Color.White
                                }
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (selectedPaymentMethod) {
                                        PaymentMethod.MTN_MOMO, PaymentMethod.AIRTEL_MONEY -> Icons.Default.PhoneAndroid
                                        PaymentMethod.CARD -> Icons.Default.CreditCard
                                        PaymentMethod.CASH_ON_DELIVERY -> Icons.Default.LocalAtm
                                    },
                                    contentDescription = null
                                )
                                Text(
                                    text = "Pay ${UgxFormatter.format(grandTotal)} with ${selectedPaymentMethod.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Your Cart is Empty",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Browse quality non-food goods, electronics, fashion, and verified items on Crane Stores Uganda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explore Marketplace")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Cart Items
                item {
                    Text(
                        "Order Items (${cartItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(cartItems, key = { it.cartItem.productId }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.updateCartQuantity(item.cartItem.productId, item.cartItem.quantity + 1) },
                        onDecrease = { viewModel.updateCartQuantity(item.cartItem.productId, item.cartItem.quantity - 1) },
                        onRemove = { viewModel.removeFromCart(item.cartItem.productId) }
                    )
                }

                // Section: Voucher Promo Code
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BrandAccent)
                                Text("Discount Voucher / Coupon", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedVoucher != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BrandTealLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                "Code Applied: ${appliedVoucher?.code}",
                                                fontWeight = FontWeight.Bold,
                                                color = BrandTealDark
                                            )
                                            Text(
                                                appliedVoucher?.title ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BrandTealDark
                                            )
                                        }
                                        IconButton(onClick = { viewModel.removeAppliedVoucher() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = BrandTealDark)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = voucherCodeInput,
                                        onValueChange = { voucherCodeInput = it },
                                        placeholder = { Text("e.g. CRANE5K, FREEDEL") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                    )
                                    Button(
                                        onClick = { viewModel.applyVoucherCode(voucherCodeInput) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                        modifier = Modifier.height(52.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }
                                if (voucherError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        voucherError ?: "",
                                        color = UgandaRed,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Delivery Details
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary)
                                Text("Delivery Address in Uganda", fontWeight = FontWeight.Bold)
                            }

                            // City selector chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                cities.take(4).forEach { city ->
                                    FilterChip(
                                        selected = selectedCity == city,
                                        onClick = { selectedCity = city },
                                        label = { Text(city, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BrandPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = {
                                    customerName = it
                                    checkoutValidationError = null
                                },
                                label = { Text("Your Full Name (Recipient)") },
                                placeholder = { Text("Enter your name") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("recipient_name_input")
                            )

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = {
                                    customerPhone = it
                                    checkoutValidationError = null
                                },
                                label = { Text("Ugandan Phone Number (for Boda Rider / MoMo)") },
                                placeholder = { Text("+256 700 000 000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("recipient_phone_input")
                            )

                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = {
                                    deliveryAddress = it
                                    checkoutValidationError = null
                                },
                                label = { Text("Street / Building / Plot Address") },
                                placeholder = { Text("e.g. Plot 12 Bukoto Street, Nakawa") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("recipient_address_input")
                            )

                            OutlinedTextField(
                                value = customerNotes,
                                onValueChange = { customerNotes = it },
                                label = { Text("Delivery Instructions / Landmark (Optional)") },
                                placeholder = { Text("e.g. Near Shell fuel station, green gate") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section: Delivery Method
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandPrimary)
                                Text("Delivery Method", fontWeight = FontWeight.Bold)
                            }

                            DeliveryMethod.values().forEach { method ->
                                val isSelected = selectedDeliveryMethod == method
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BrandPrimaryLight.copy(alpha = 0.5f) else Slate50,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) BrandPrimary else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDeliveryMethod = method }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedDeliveryMethod = method },
                                                colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                            )
                                            Column {
                                                Text(method.displayName, fontWeight = FontWeight.Bold)
                                                Text(method.durationText, style = MaterialTheme.typography.bodySmall, color = Slate600)
                                            }
                                        }
                                        Text(
                                            UgxFormatter.format(method.baseFeeUgx),
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Secure Payment Gateways
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = BrandPrimary)
                                Text("Secure Ugandan Payment Gateway", fontWeight = FontWeight.Bold)
                            }

                            PaymentMethod.values().forEach { method ->
                                val isSelected = selectedPaymentMethod == method
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) {
                                        when (method) {
                                            PaymentMethod.MTN_MOMO -> Color(0xFFFEF9C3)
                                            PaymentMethod.AIRTEL_MONEY -> Color(0xFFFEE2E2)
                                            else -> BrandPrimaryLight
                                        }
                                    } else Slate50,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) {
                                            when (method) {
                                                PaymentMethod.MTN_MOMO -> Color(0xFFCA8A04)
                                                PaymentMethod.AIRTEL_MONEY -> UgandaRed
                                                else -> BrandPrimary
                                            }
                                        } else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPaymentMethod = method }
                                        .testTag("payment_method_${method.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPaymentMethod = method },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = when (method) {
                                                        PaymentMethod.MTN_MOMO -> Color(0xFFCA8A04)
                                                        PaymentMethod.AIRTEL_MONEY -> UgandaRed
                                                        else -> BrandPrimary
                                                    }
                                                )
                                            )
                                            Column {
                                                Text(method.displayName, fontWeight = FontWeight.Bold)
                                                Text(method.subtitle, style = MaterialTheme.typography.bodySmall, color = Slate600)
                                            }
                                        }

                                        if (method == PaymentMethod.MTN_MOMO) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = CraneMomoYellow
                                            ) {
                                                Text(
                                                    "MoMo",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp,
                                                    color = Slate950,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        } else if (method == PaymentMethod.AIRTEL_MONEY) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = CraneAirtelRed
                                            ) {
                                                Text(
                                                    "Airtel",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Bill Breakdown
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Payment Summary", fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Items Subtotal", color = Slate600)
                                Text(UgxFormatter.format(subtotal), fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Fee (${selectedDeliveryMethod.displayName})", color = Slate600)
                                Text(UgxFormatter.format(deliveryFee), fontWeight = FontWeight.Medium)
                            }
                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Loyalty Voucher Discount", color = BrandTealDark, fontWeight = FontWeight.Bold)
                                    Text("-${UgxFormatter.format(discount)}", color = BrandTealDark, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = Slate200)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total to Pay", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    UgxFormatter.format(grandTotal),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = BrandPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemWithProduct,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductImageView(
                imageUri = item.product.imageUri,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Text(
                    text = "${UgxFormatter.format(item.product.finalPriceUgx)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = UgxFormatter.format(item.totalPriceUgx),
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandPrimaryDark,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // Quantity controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = {
                        if (item.cartItem.quantity == 1) onRemove() else onDecrease()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Slate100, shape = CircleShape)
                ) {
                    Icon(
                        if (item.cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (item.cartItem.quantity == 1) UgandaRed else Slate800,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${item.cartItem.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrandPrimaryLight, shape = CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = BrandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
