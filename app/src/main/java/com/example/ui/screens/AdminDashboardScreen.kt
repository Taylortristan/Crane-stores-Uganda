package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.ProductImageView
import com.example.ui.components.UgxPriceDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: CraneViewModel,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analyticsSummary.collectAsState()
    val allProducts by viewModel.products.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    var selectedAdminTab by remember { mutableStateOf(0) } // 0: Analytics, 1: Products, 2: Orders Dispatch
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var assignDriverOrder by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshAnalytics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandPrimary)
                        Text("Crane Admin Dashboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                },
                actions = {
                    if (selectedAdminTab == 1) {
                        Button(
                            onClick = { showAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_add_product_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Item", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Admin Navigation Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandPrimary
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("Analytics Review", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("Products (${allProducts.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("Dispatch (${allOrders.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) }
                )
            }

            when (selectedAdminTab) {
                0 -> AdminAnalyticsView(analytics, allOrders.size)
                1 -> AdminProductsView(
                    products = allProducts,
                    onEdit = { editingProduct = it },
                    onDelete = { viewModel.deleteProductByAdmin(it.id) }
                )
                2 -> AdminOrdersDispatchView(
                    orders = allOrders,
                    onStatusUpdate = { id, status -> viewModel.updateOrderStatusByAdmin(id, status) },
                    onAssignDriver = { assignDriverOrder = it }
                )
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        ProductFormDialog(
            product = null,
            onDismiss = { showAddProductDialog = false },
            onSave = { name, cat, desc, price, discount, stock, origin, unit, isFeatured, imgUri ->
                viewModel.addNewProductByAdmin(
                    name = name,
                    category = cat,
                    description = desc,
                    priceUgx = price,
                    discountPercent = discount,
                    stock = stock,
                    originRegion = origin,
                    unitLabel = unit,
                    isFeatured = isFeatured,
                    imageUri = imgUri
                )
                showAddProductDialog = false
            }
        )
    }

    // Edit Product Dialog
    if (editingProduct != null) {
        ProductFormDialog(
            product = editingProduct,
            onDismiss = { editingProduct = null },
            onSave = { name, cat, desc, price, discount, stock, origin, unit, isFeatured, imgUri ->
                editingProduct?.let { existing ->
                    viewModel.updateProductByAdmin(
                        existing.copy(
                            name = name,
                            category = cat,
                            description = desc,
                            priceUgx = price,
                            discountPercent = discount,
                            stockQuantity = stock,
                            originRegion = origin,
                            unitLabel = unit,
                            isFeatured = isFeatured,
                            imageUri = imgUri
                        )
                    )
                }
                editingProduct = null
            }
        )
    }

    // Assign Driver Dialog
    if (assignDriverOrder != null) {
        AssignDriverDialog(
            orderId = assignDriverOrder!!,
            onDismiss = { assignDriverOrder = null },
            onConfirm = { rider, phone, plate ->
                viewModel.assignDriverByAdmin(assignDriverOrder!!, rider, phone, plate)
                assignDriverOrder = null
            }
        )
    }
}

@Composable
fun AdminAnalyticsView(
    analytics: com.example.data.repository.AnalyticsSummary?,
    ordersCount: Int
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsKpiCard(
                title = "Total Revenue",
                value = UgxFormatter.format(analytics?.totalRevenueUgx ?: 885000.0),
                subtext = "+18.4% this week",
                icon = Icons.Default.MonetizationOn,
                color = BrandAccent,
                modifier = Modifier.weight(1f)
            )
            AnalyticsKpiCard(
                title = "Total Orders",
                value = "$ordersCount",
                subtext = "Boda Express 94%",
                icon = Icons.Default.ShoppingBag,
                color = BrandTeal,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsKpiCard(
                title = "Avg Order Value",
                value = UgxFormatter.format(analytics?.averageOrderValueUgx ?: 68200.0),
                subtext = "High basket size",
                icon = Icons.Default.TrendingUp,
                color = BrandPrimary,
                modifier = Modifier.weight(1f)
            )
            AnalyticsKpiCard(
                title = "Active Shoppers",
                value = "142",
                subtext = "+24 new today",
                icon = Icons.Default.People,
                color = Color(0xFF0EA5E9),
                modifier = Modifier.weight(1f)
            )
        }

        // Weekly Sales Trend Bar Visual
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Uganda Weekly Sales Trend (UGX)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                val days = listOf("Mon" to 0.45f, "Tue" to 0.60f, "Wed" to 0.55f, "Thu" to 0.80f, "Fri" to 0.95f, "Sat" to 1.0f, "Sun" to 0.70f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { (day, fraction) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height((fraction * 90).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (fraction >= 0.9f) BrandAccent else Slate200)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(day, style = MaterialTheme.typography.labelSmall, color = Slate600)
                        }
                    }
                }
            }
        }

        // Category Sales Breakdown (Non-Food Only)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Sales Distribution by Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                CategoryProgressRow("Phones & Electronics", 0.42f, BrandPrimary, "42%")
                CategoryProgressRow("Fashion & Kitenge Fabrics", 0.28f, BrandAccent, "28%")
                CategoryProgressRow("Vehicles & Safety Gear", 0.16f, BrandTeal, "16%")
                CategoryProgressRow("Ugandan Crafts & Furniture", 0.14f, Color(0xFF8B5CF6), "14%")
            }
        }
    }
}

@Composable
fun CategoryProgressRow(title: String, fraction: Float, color: Color, percentageText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(percentageText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Slate100
        )
    }
}

@Composable
fun AnalyticsKpiCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Slate600)
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(subtext, style = MaterialTheme.typography.labelSmall, color = BrandTealDark, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminProductsView(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products, key = { it.id }) { product ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductImageView(
                        imageUri = product.imageUri,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            "${product.category} • ${product.originRegion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            UgxPriceDisplay(priceUgx = product.priceUgx, discountPercent = product.discountPercent)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (product.stockQuantity > 5) BrandTealLight else UgandaRedLight
                            ) {
                                Text(
                                    "Stock: ${product.stockQuantity}",
                                    color = if (product.stockQuantity > 5) BrandTealDark else UgandaRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { onEdit(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandAccent)
                        }
                        IconButton(onClick = { onDelete(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UgandaRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersDispatchView(
    orders: List<OrderEntity>,
    onStatusUpdate: (String, OrderStatus) -> Unit,
    onAssignDriver: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(orders, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Order #${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(order.customerName, style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                        OrderStatusChip(status = order.status)
                    }

                    Text("Items: ${order.itemsSummary}", style = MaterialTheme.typography.bodySmall)
                    Text("Delivery: ${order.deliveryAddress}, ${order.deliveryCity} (${order.deliveryMethod})", style = MaterialTheme.typography.labelSmall, color = Slate600)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Courier: ${order.riderName} (${order.riderPlate})", style = MaterialTheme.typography.labelSmall, color = BrandTealDark, fontWeight = FontWeight.Bold)
                        Text(UgxFormatter.format(order.totalUgx), fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
                    }

                    HorizontalDivider(color = Slate200)

                    // Status Progression Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (order.status == OrderStatus.PLACED) {
                            Button(
                                onClick = { onStatusUpdate(order.id, OrderStatus.CONFIRMED) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirm", fontSize = 11.sp)
                            }
                        }
                        if (order.status == OrderStatus.CONFIRMED) {
                            Button(
                                onClick = { onStatusUpdate(order.id, OrderStatus.PACKING) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pack Items", fontSize = 11.sp)
                            }
                        }
                        if (order.status == OrderStatus.PACKING) {
                            Button(
                                onClick = { onStatusUpdate(order.id, OrderStatus.OUT_FOR_DELIVERY) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandTealDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dispatch Boda", fontSize = 11.sp)
                            }
                        }
                        if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                            Button(
                                onClick = { onStatusUpdate(order.id, OrderStatus.DELIVERED) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Delivered", fontSize = 11.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { onAssignDriver(order.id) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reassign Driver", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        description: String,
        priceUgx: Double,
        discountPercent: Int,
        stock: Int,
        originRegion: String,
        unitLabel: String,
        isFeatured: Boolean,
        imageUri: String
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(product?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: ProductCategory.PHONES.name) }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var priceUgxText by remember { mutableStateOf(product?.priceUgx?.toInt()?.toString() ?: "75000") }
    var discountPercentText by remember { mutableStateOf(product?.discountPercent?.toString() ?: "0") }
    var stockText by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "30") }
    var originRegion by remember { mutableStateOf(product?.originRegion ?: "Kampala, Uganda") }
    var unitLabel by remember { mutableStateOf(product?.unitLabel ?: "per item") }
    var isFeatured by remember { mutableStateOf(product?.isFeatured ?: false) }
    var imageUri by remember { mutableStateOf(product?.imageUri ?: "hero_uganda_market") }

    // Image Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = com.example.util.ImageStorageHelper.saveUriToInternalStorage(context, uri)
            if (savedPath != null) {
                imageUri = savedPath
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (product == null) "Add Ugandan Product" else "Edit Product",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = Slate200)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Product Photo (Upload / Select)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate100)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ProductImageView(
                            imageUri = imageUri,
                            contentDescription = "Selected product photo",
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate900.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Upload Image", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Quick Sample Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imageUri = "hero_uganda_market" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Market Banner", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { imageUri = "ic_crane_logo" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Crane Emblem", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Title") },
                        placeholder = { Text("e.g. Kitenge Fabric / Samsung TV") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Category Selector (Non-Food Only)
                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var expandedCat by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedCat = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ProductCategory.valueOf(selectedCategory).displayName)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false }
                        ) {
                            ProductCategory.values().filter { it != ProductCategory.ALL }.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceUgxText,
                            onValueChange = { priceUgxText = it },
                            label = { Text("Price (UGX)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = discountPercentText,
                            onValueChange = { discountPercentText = it },
                            label = { Text("Discount %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Stock Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = originRegion,
                            onValueChange = { originRegion = it },
                            label = { Text("Origin Region") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = unitLabel,
                        onValueChange = { unitLabel = it },
                        label = { Text("Unit Label (e.g. 6 Yards Bolt, Box set)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isFeatured,
                            onCheckedChange = { isFeatured = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandPrimary)
                        )
                        Text("Feature on Homepage / Flash Deals", fontWeight = FontWeight.Medium)
                    }
                }

                Button(
                    onClick = {
                        val price = priceUgxText.toDoubleOrNull() ?: 20000.0
                        val discount = discountPercentText.toIntOrNull() ?: 0
                        val stock = stockText.toIntOrNull() ?: 20

                        onSave(name, selectedCategory, description, price, discount, stock, originRegion, unitLabel, isFeatured, imageUri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_product_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Save Product", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AssignDriverDialog(
    orderId: String,
    onDismiss: () -> Unit,
    onConfirm: (rider: String, phone: String, plate: String) -> Unit
) {
    var riderName by remember { mutableStateOf("Kato Sulaiman") }
    var riderPhone by remember { mutableStateOf("+256 772 884 192") }
    var riderPlate by remember { mutableStateOf("UFF 842K (Bajaj Boxer)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Courier for #$orderId", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = riderName,
                    onValueChange = { riderName = it },
                    label = { Text("Driver Name") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = riderPhone,
                    onValueChange = { riderPhone = it },
                    label = { Text("Driver Phone Number") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = riderPlate,
                    onValueChange = { riderPlate = it },
                    label = { Text("Boda Number Plate / Vehicle") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(riderName, riderPhone, riderPlate) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Confirm Dispatch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
