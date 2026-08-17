package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.DeliveryMethod
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBuyScreen(
    viewModel: CraneViewModel,
    onNavigateBack: () -> Unit,
    onBuyRequestSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.PHONES) }
    var targetBudgetInput by remember { mutableStateOf("") }
    var itemSpecifications by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("Kampala, Central") }
    var conditionPreference by remember { mutableStateOf("Brand New") }
    var buyerPhone by remember { mutableStateOf("+256 770 123 456") }
    var buyerName by remember { mutableStateOf("Taylor Tristan") }
    var deliverySpeed by remember { mutableStateOf("Same-Day Express Boda 🛵") }

    var formError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val ugandanDistricts = listOf(
        "Kampala, Central",
        "Kampala, Nakasero",
        "Kampala, Kololo",
        "Wakiso, Entebbe Road",
        "Wakiso, Kira",
        "Mukono Town",
        "Jinja, Eastern",
        "Mbarara, Western",
        "Gulu, Northern",
        "Mbale, Bugisu"
    )

    val conditionsList = listOf("Brand New", "Refurbished / Open Box", "Used (Good Condition)", "Handmade / Custom")
    val deliverySpeedList = listOf("Same-Day Express Boda 🛵", "Standard Delivery (24-48h)", "Scheduled Delivery")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandPrimary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White, modifier = Modifier.padding(5.dp))
                        }
                        Text("Direct Sourcing & Order (+ BUY)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (itemName.isBlank()) {
                                formError = "Please enter what product you want to buy."
                                return@Button
                            }
                            val parsedPrice = targetBudgetInput.toDoubleOrNull()
                            if (parsedPrice == null || parsedPrice <= 0) {
                                formError = "Please enter your target budget in UGX."
                                return@Button
                            }
                            if (buyerPhone.isBlank()) {
                                formError = "Please provide your contact phone for delivery verification."
                                return@Button
                            }

                            formError = null
                            isSubmitting = true

                            // Auto-create buy request and seed product/inquiry in the system
                            viewModel.addNewProductByAdmin(
                                name = "[BUY REQUEST] $itemName",
                                category = selectedCategory.name,
                                description = "Customer Buy Order: $itemSpecifications | Speed: $deliverySpeed | Condition: $conditionPreference | Buyer: $buyerName ($buyerPhone)",
                                priceUgx = parsedPrice,
                                discountPercent = 0,
                                stock = 1,
                                originRegion = selectedLocation,
                                unitLabel = "Wanted Item",
                                isFeatured = false,
                                imageUri = "ic_crane_logo"
                            )

                            isSubmitting = false
                            showSuccessDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_buy_request_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                            Text("Submit Buy Order / Instant Match", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
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
            // Marketplace Procurement Banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPrimaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(BrandPrimary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Buy Anything Directly in Uganda", fontWeight = FontWeight.Bold, color = BrandPrimaryDark, fontSize = 14.sp)
                            Text("Submit your item requirement. We match with verified merchants & arrange Boda delivery.", style = MaterialTheme.typography.bodySmall, color = Slate700)
                        }
                    }
                }
            }

            // Error display
            if (formError != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = UgandaRedLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = UgandaRed)
                            Text(formError ?: "", color = UgandaRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // What do you want to buy Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("What would you like to buy?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        // Item name
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Product Name or Model (e.g. iPhone 15, Kitenge Fabric, Sofa)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("buy_item_name_input")
                        )

                        // Category
                        Text("Category", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Slate700)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProductCategory.values().filter { it != ProductCategory.ALL }.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Target Budget in UGX
                        OutlinedTextField(
                            value = targetBudgetInput,
                            onValueChange = { targetBudgetInput = it },
                            label = { Text("Your Target Budget (UGX)") },
                            placeholder = { Text("e.g. 150000") },
                            leadingIcon = { Text("UGX ", fontWeight = FontWeight.Bold, color = BrandPrimary, modifier = Modifier.padding(start = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("buy_budget_input")
                        )

                        // Condition Preference
                        Text("Preferred Condition", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Slate700)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            conditionsList.forEach { cond ->
                                val isSelected = conditionPreference == cond
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { conditionPreference = cond },
                                    label = { Text(cond, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandAccent,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Specifications & details
                        OutlinedTextField(
                            value = itemSpecifications,
                            onValueChange = { itemSpecifications = it },
                            label = { Text("Specifications / Brand / Color / Size") },
                            placeholder = { Text("Specify model details, preferred color, quantity, or warranty requirements...") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Delivery & Contact Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Delivery & Contact Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        // Delivery Speed
                        Text("Preferred Delivery Speed", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Slate700)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            deliverySpeedList.forEach { speed ->
                                val isSelected = deliverySpeed == speed
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { deliverySpeed = speed },
                                    label = { Text(speed, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandTeal,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Location
                        Text("Your Location in Uganda", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Slate700)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ugandanDistricts.take(6).forEach { district ->
                                val isSelected = selectedLocation == district
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedLocation = district },
                                    label = { Text(district, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Buyer Name
                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = { buyerName = it },
                            label = { Text("Your Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Buyer Phone Number
                        OutlinedTextField(
                            value = buyerPhone,
                            onValueChange = { buyerPhone = it },
                            label = { Text("Phone Number for Order Confirmation & MoMo") },
                            placeholder = { Text("+256 700 ...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("buyer_phone_input")
                        )
                    }
                }
            }

            // Buyer Guarantee Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                            Text("Crane Buyer Protection & Escrow", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "Every order placed through Crane Stores is protected. You inspect items on Boda delivery before funds are released.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBuyRequestSubmitted()
            },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(54.dp))
            },
            title = { Text("Buy Order Received! 🛒🎉", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your request for '$itemName' (Budget: UGX ${targetBudgetInput}) has been dispatched to verified suppliers in $selectedLocation. We will notify you via SMS/WhatsApp with live tracking once dispatched!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBuyRequestSubmitted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Go to Marketplace")
                }
            }
        )
    }
}
