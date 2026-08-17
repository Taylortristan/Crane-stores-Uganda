package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.DeliveryMethod
import com.example.data.model.PaymentMethod
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.ui.viewmodel.PaymentUiState
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: CraneViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val totalCartCount by viewModel.totalCartCount.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showCartScreen by remember { mutableStateOf(false) }
    var showQuickBuyScreen by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!showCartScreen && !showQuickBuyScreen) {
                Column {
                    // Admin Mode Top Alert Banner (if Admin is currently authenticated)
                    if (isAdminLoggedIn) {
                        Surface(
                            color = UgandaRedDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(16.dp))
                                    Text("CRANE ADMIN PORTAL ACTIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.logoutAdmin() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Exit Admin", color = UgandaRedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Main App Bar
                    Surface(
                        color = BrandPrimary,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (!isAdminLoggedIn) Modifier.statusBarsPadding() else Modifier)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Logo and Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { viewModel.setCurrentTab(0) }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_crane_logo),
                                        contentDescription = "Crane Logo",
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "CRANE STORES",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            letterSpacing = 0.5.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = BrandAccent
                                        ) {
                                            Text(
                                                "EXPRESS",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp,
                                                color = Slate950,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "Uganda Verified Marketplace & Express Delivery",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            // Action Icons
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Notifications Icon with badge
                                IconButton(onClick = { viewModel.setCurrentTab(5) }) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotifications > 0) {
                                                Badge(containerColor = UgandaRed) {
                                                    Text("$unreadNotifications")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                                    }
                                }

                                // Cart Icon with badge
                                IconButton(
                                    onClick = { showCartScreen = true },
                                    modifier = Modifier.testTag("top_bar_cart_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (totalCartCount > 0) {
                                                Badge(containerColor = BrandAccent) {
                                                    Text("$totalCartCount", color = Slate950, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                                    }
                                }

                                // Discreet Admin Login portal icon
                                IconButton(
                                    onClick = {
                                        if (isAdminLoggedIn) {
                                            viewModel.setCurrentTab(4)
                                        } else {
                                            showAdminPinDialog = true
                                        }
                                    },
                                    modifier = Modifier.testTag("admin_portal_trigger")
                                ) {
                                    Icon(
                                        if (isAdminLoggedIn) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                                        contentDescription = "Admin Portal",
                                        tint = if (isAdminLoggedIn) BrandAccent else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!showCartScreen && !showQuickBuyScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // CUSTOMER TAB 0: Market
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.setCurrentTab(0) },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Market") },
                        label = { Text("Market", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = BrandPrimaryLight
                        ),
                        modifier = Modifier.testTag("nav_shop")
                    )

                    // CUSTOMER TAB 1: AI Assistant
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.setCurrentTab(1) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "AI Assistant") },
                        label = { Text("AI Assistant", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = BrandPrimaryLight
                        ),
                        modifier = Modifier.testTag("nav_chat")
                    )

                    // CUSTOMER ACTION: + BUY ITEM
                    NavigationBarItem(
                        selected = false,
                        onClick = { showQuickBuyScreen = true },
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = BrandAccent,
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = "Buy", tint = Slate950, modifier = Modifier.size(22.dp))
                                }
                            }
                        },
                        label = { Text("+ BUY", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = BrandAccentDark) },
                        modifier = Modifier.testTag("nav_buy_button")
                    )

                    // CUSTOMER TAB 2: Tracking (Live Boda Boda)
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.setCurrentTab(2) },
                        icon = { Icon(Icons.Default.TwoWheeler, contentDescription = "Track Boda") },
                        label = { Text("Tracking", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = BrandPrimaryLight
                        ),
                        modifier = Modifier.testTag("nav_tracking")
                    )

                    // CUSTOMER TAB 3: Rewards & Crane Club VIP
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.setCurrentTab(3) },
                        icon = { Icon(Icons.Default.Stars, contentDescription = "Rewards") },
                        label = { Text("Rewards", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = BrandPrimaryLight
                        ),
                        modifier = Modifier.testTag("nav_rewards")
                    )

                    // ADMIN ONLY TAB (Hidden from customers unless unlocked)
                    if (isAdminLoggedIn) {
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { viewModel.setCurrentTab(4) },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UgandaRed,
                                selectedTextColor = UgandaRed,
                                indicatorColor = UgandaRed.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_admin")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showQuickBuyScreen) {
                QuickBuyScreen(
                    viewModel = viewModel,
                    onNavigateBack = { showQuickBuyScreen = false },
                    onBuyRequestSubmitted = {
                        showQuickBuyScreen = false
                        viewModel.setCurrentTab(0)
                    }
                )
            } else if (showCartScreen) {
                CartAndCheckoutScreen(
                    viewModel = viewModel,
                    onNavigateBack = { showCartScreen = false },
                    onOrderSuccess = { orderId ->
                        showCartScreen = false
                        viewModel.selectOrderForTracking(orderId)
                    }
                )
            } else {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onOpenPostAd = { showQuickBuyScreen = true },
                        onOpenChatBot = { viewModel.setCurrentTab(1) }
                    )
                    1 -> ChatBotScreen(
                        viewModel = viewModel,
                        onNavigateToShop = { viewModel.setCurrentTab(0) },
                        onNavigateToPostAd = { showQuickBuyScreen = true },
                        onNavigateToTracking = { viewModel.setCurrentTab(2) }
                    )
                    2 -> OrderTrackingScreen(viewModel = viewModel)
                    3 -> LoyaltyScreen(viewModel = viewModel)
                    4 -> {
                        if (isAdminLoggedIn) {
                            AdminDashboardScreen(viewModel = viewModel)
                        } else {
                            // If user is not admin, fallback to Home
                            HomeScreen(
                                viewModel = viewModel,
                                onOpenPostAd = { showQuickBuyScreen = true },
                                onOpenChatBot = { viewModel.setCurrentTab(1) }
                            )
                        }
                    }
                    5 -> NotificationCenterScreen(
                        viewModel = viewModel,
                        onNavigateToOrder = { orderId ->
                            viewModel.selectOrderForTracking(orderId)
                        }
                    )
                }
            }
        }
    }

    // Secret Admin PIN Authentication Dialog
    if (showAdminPinDialog) {
        AdminLoginPinDialog(
            onConfirmPin = { pin ->
                val isValid = viewModel.verifyAdminPin(pin)
                if (isValid) {
                    showAdminPinDialog = false
                }
                isValid
            },
            onDismiss = { showAdminPinDialog = false }
        )
    }

    // Product Detail Sheet Dialog
    if (selectedProduct != null) {
        ProductDetailDialog(
            product = selectedProduct!!,
            onDismiss = { viewModel.selectProduct(null) },
            onAddToCart = { qty ->
                viewModel.addToCart(selectedProduct!!, qty)
            },
            onBuyNow = { qty ->
                viewModel.addToCart(selectedProduct!!, qty)
                showCartScreen = true
            },
            onOpenChatBot = {
                viewModel.setCurrentTab(1)
            }
        )
    }

    // Mobile Money USSD Authorization PIN Dialog
    when (val state = paymentState) {
        is PaymentUiState.UssdPinPrompt -> {
            UssdPinPromptDialog(
                method = state.method,
                amountUgx = state.amountUgx,
                phone = state.phone,
                onConfirmPin = { pin ->
                    viewModel.confirmUssdPinAndPay(
                        pin = pin,
                        customerName = state.customerName,
                        customerPhone = state.phone,
                        deliveryAddress = state.deliveryAddress,
                        city = state.city,
                        deliveryMethod = state.deliveryMethod,
                        paymentMethod = state.method,
                        customerNotes = state.customerNotes
                    )
                },
                onCancel = { viewModel.resetPaymentState() }
            )
        }
        is PaymentUiState.Processing -> {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = BrandPrimary, strokeWidth = 3.dp)
                        Text(state.stepText, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        is PaymentUiState.Success -> {
            AlertDialog(
                onDismissRequest = {
                    viewModel.resetPaymentState()
                    showCartScreen = false
                    viewModel.setCurrentTab(2)
                },
                icon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(54.dp))
                },
                title = { Text("Payment Successful! 🎉", fontWeight = FontWeight.ExtraBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Order #${state.order.id} has been confirmed!")
                        Text("Total Paid: ${UgxFormatter.format(state.order.totalUgx)}")
                        Text("Earned: +${state.order.pointsEarned} Crane Points ✨", fontWeight = FontWeight.Bold, color = BrandAccentDark)
                        Text("A Boda Boda driver is now dispatched to pick up and deliver your order.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetPaymentState()
                            showCartScreen = false
                            viewModel.setCurrentTab(2)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Track Order Live 🛵")
                    }
                }
            )
        }
        else -> {}
    }
}

@Composable
fun AdminLoginPinDialog(
    onConfirmPin: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BrandPrimaryLight,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(28.dp))
                    }
                }

                Text(
                    "Store Management Portal",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Slate900
                )

                Text(
                    "Enter your secure Master Admin PIN to access inventory controls, dispatch logs, and store analytics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) {
                            pin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Admin PIN (e.g. 2560)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = UgandaRed)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Slate300
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (pin.isBlank()) {
                                errorMessage = "Please enter PIN"
                            } else {
                                val success = onConfirmPin(pin)
                                if (!success) {
                                    errorMessage = "Invalid Admin PIN. (Default: 2560)"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("admin_pin_submit")
                    ) {
                        Text("Unlock")
                    }
                }
            }
        }
    }
}

@Composable
fun UssdPinPromptDialog(
    method: PaymentMethod,
    amountUgx: Double,
    phone: String,
    onConfirmPin: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (method == PaymentMethod.MTN_MOMO) Color(0xFF1E293B) else Color(0xFF1F1F1F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (method == PaymentMethod.MTN_MOMO) Color(0xFFFFCC00) else Color(0xFFE60000)
                ) {
                    Text(
                        if (method == PaymentMethod.MTN_MOMO) "MTN MoMo Pay (*165#)" else "Airtel Money Pay (*185#)",
                        color = if (method == PaymentMethod.MTN_MOMO) Slate950 else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    "Authorize Payment",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    "Do you approve payment of ${UgxFormatter.format(amountUgx)} to Crane Stores Uganda from $phone?",
                    color = Slate300,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 5) pin = it },
                    label = { Text("Enter 4-5 Digit PIN", color = Slate400) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = if (method == PaymentMethod.MTN_MOMO) Color(0xFFFFCC00) else Color(0xFFE60000),
                        unfocusedBorderColor = Slate600
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ussd_pin_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { if (pin.length >= 4) onConfirmPin(pin) },
                        enabled = pin.length >= 4,
                        modifier = Modifier.weight(1f).testTag("ussd_confirm_pin_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (method == PaymentMethod.MTN_MOMO) Color(0xFFFFCC00) else Color(0xFFE60000),
                            contentColor = if (method == PaymentMethod.MTN_MOMO) Slate950 else Color.White
                        )
                    ) {
                        Text("Approve", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
