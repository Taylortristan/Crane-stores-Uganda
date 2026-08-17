package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCategory
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductImageView
import com.example.ui.components.UgxPriceDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import com.example.ui.viewmodel.ProductSortOrder
import com.example.util.UgxFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CraneViewModel,
    onOpenPostAd: () -> Unit, // triggers + BUY direct sourcing flow
    onOpenChatBot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var isBudgetFilterExpanded by remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current

    // Only non-food categories for grid and filter
    val nonFoodCategories = remember {
        listOf(
            ProductCategory.ALL,
            ProductCategory.PHONES,
            ProductCategory.ELECTRONICS,
            ProductCategory.VEHICLES,
            ProductCategory.FASHION,
            ProductCategory.HOME_FURNITURE,
            ProductCategory.UGANDAN_CRAFTS,
            ProductCategory.HEALTH_BEAUTY,
            ProductCategory.HARDWARE,
            ProductCategory.PROPERTY,
            ProductCategory.SPORTS,
            ProductCategory.SERVICES
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        val isDesktopOrWide = maxWidth >= 768.dp

        if (isDesktopOrWide) {
            // DESKTOP / TABLET ADAPTIVE 2-PANE LAYOUT
            Row(modifier = Modifier.fillMaxSize()) {
                // Desktop Left Sidebar (Categories, Range Slider & Presets)
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight(),
                    color = Color.White,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                "Filters & Budget",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Filter products by price and category in Uganda Shillings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }

                        // Price Range Slider Filter Panel
                        item {
                            BudgetRangeFilterCard(
                                priceRange = priceRange,
                                onRangeChange = { viewModel.setPriceRange(it) },
                                onReset = { viewModel.resetPriceFilter() }
                            )
                        }

                        // Category List for Desktop
                        item {
                            Text(
                                "Product Categories",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                nonFoodCategories.forEach { cat ->
                                    val isSelected = selectedCategory == cat.name
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) BrandPrimaryLight else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.setSelectedCategory(cat.name) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                getCategoryIcon(cat),
                                                contentDescription = null,
                                                tint = if (isSelected) BrandPrimary else Slate600,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                cat.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) BrandPrimary else Slate800
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Desktop Right Main Content (Search, Banner & Multi-Column Grid)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SearchAndBuyHeader(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onOpenPostAd = onOpenPostAd,
                            focusManager = focusManager
                        )
                    }

                    item {
                        PromotionalProcurementBanner(onOpenPostAd = onOpenPostAd)
                    }

                    item {
                        ProductListControlBar(
                            selectedCategory = selectedCategory,
                            nonFoodCategories = nonFoodCategories,
                            productCount = products.size,
                            sortOrder = sortOrder,
                            showSortMenu = showSortMenu,
                            onToggleSortMenu = { showSortMenu = it },
                            onSelectSortOrder = { viewModel.setSortOrder(it) }
                        )
                    }

                    if (products.isEmpty()) {
                        item {
                            EmptyProductState(onResetFilter = {
                                viewModel.resetPriceFilter()
                                viewModel.setSelectedCategory(ProductCategory.ALL.name)
                                viewModel.setSearchQuery("")
                            })
                        }
                    } else {
                        val desktopChunks = products.chunked(3)
                        items(desktopChunks) { rowProducts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowProducts.forEach { prod ->
                                    ProductCard(
                                        product = prod,
                                        onProductClick = { viewModel.selectProduct(prod) },
                                        onBuyClick = {
                                            viewModel.addToCart(prod, 1)
                                            viewModel.selectProduct(prod)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                for (i in rowProducts.size until 3) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MOBILE COMPACT VIEW WITH ACCESSIBLE BUDGET RANGE SLIDER
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate50),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header & Search
                item {
                    SearchAndBuyHeader(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onOpenPostAd = onOpenPostAd,
                        focusManager = focusManager
                    )
                }

                // Procurement Banner
                item {
                    PromotionalProcurementBanner(onOpenPostAd = onOpenPostAd)
                }

                // Category Chips (Non-Food Only)
                item {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Categories",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                            )
                            Text(
                                "Non-Food Catalog",
                                style = MaterialTheme.typography.labelSmall.copy(color = BrandPrimary, fontWeight = FontWeight.Bold)
                            )
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(nonFoodCategories) { cat ->
                                val isSelected = selectedCategory == cat.name
                                CategoryRoundChip(
                                    category = cat,
                                    isSelected = isSelected,
                                    onClick = { viewModel.setSelectedCategory(cat.name) }
                                )
                            }
                        }
                    }
                }

                // Interactive Budget Filter & Range Slider (Mobile)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .animateContentSize()
                            .testTag("budget_filter_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isBudgetFilterExpanded = !isBudgetFilterExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = BrandAccent.copy(alpha = 0.2f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.MonetizationOn,
                                                contentDescription = null,
                                                tint = BrandAccentDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            "Budget & Price Filter",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Slate900
                                        )
                                        Text(
                                            if (priceRange.start == 0f && priceRange.endInclusive >= 5000000f)
                                                "Showing all prices"
                                            else
                                                "${UgxFormatter.format(priceRange.start.toDouble())} - ${if (priceRange.endInclusive >= 5000000f) "5M+ UGX" else UgxFormatter.format(priceRange.endInclusive.toDouble())}",
                                            fontSize = 11.sp,
                                            color = BrandPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (priceRange.start > 0f || priceRange.endInclusive < 5000000f) {
                                        TextButton(
                                            onClick = { viewModel.resetPriceFilter() },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("clear_budget_btn")
                                        ) {
                                            Text("Clear", fontSize = 11.sp, color = UgandaRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Icon(
                                        if (isBudgetFilterExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Budget Slider",
                                        tint = Slate600
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isBudgetFilterExpanded) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    BudgetRangeFilterContent(
                                        priceRange = priceRange,
                                        onRangeChange = { viewModel.setPriceRange(it) },
                                        onReset = { viewModel.resetPriceFilter() }
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Shopping Assistant Quick Helper Banner
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandTealLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onOpenChatBot() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandTeal, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Looking for specific item within your budget?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandTealDark)
                                Text("Ask Crane Shopping Assistant for quotes & MoMo payments.", fontSize = 11.sp, color = Slate600)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandTealDark)
                        }
                    }
                }

                // Product List Control Bar (Count + Sort)
                item {
                    ProductListControlBar(
                        selectedCategory = selectedCategory,
                        nonFoodCategories = nonFoodCategories,
                        productCount = products.size,
                        sortOrder = sortOrder,
                        showSortMenu = showSortMenu,
                        onToggleSortMenu = { showSortMenu = it },
                        onSelectSortOrder = { viewModel.setSortOrder(it) }
                    )
                }

                // Product 2-Column Grid
                if (products.isEmpty()) {
                    item {
                        EmptyProductState(onResetFilter = {
                            viewModel.resetPriceFilter()
                            viewModel.setSelectedCategory(ProductCategory.ALL.name)
                            viewModel.setSearchQuery("")
                        })
                    }
                } else {
                    val chunkedProducts = products.chunked(2)
                    items(chunkedProducts) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProductCard(
                                product = pair[0],
                                onProductClick = { viewModel.selectProduct(pair[0]) },
                                onBuyClick = {
                                    viewModel.addToCart(pair[0], 1)
                                    viewModel.selectProduct(pair[0])
                                },
                                modifier = Modifier.weight(1f)
                            )

                            if (pair.size > 1) {
                                ProductCard(
                                    product = pair[1],
                                    onProductClick = { viewModel.selectProduct(pair[1]) },
                                    onBuyClick = {
                                        viewModel.addToCart(pair[1], 1)
                                        viewModel.selectProduct(pair[1])
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetRangeFilterCard(
    priceRange: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onReset: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate50),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            BudgetRangeFilterContent(
                priceRange = priceRange,
                onRangeChange = onRangeChange,
                onReset = onReset
            )
        }
    }
}

@Composable
fun BudgetRangeFilterContent(
    priceRange: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Price Range Display Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Min Price", style = MaterialTheme.typography.labelSmall, color = Slate500)
                Text(
                    UgxFormatter.format(priceRange.start.toDouble()),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandPrimary
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Max Price", style = MaterialTheme.typography.labelSmall, color = Slate500)
                Text(
                    if (priceRange.endInclusive >= 5000000f) "5,000,000+ UGX" else UgxFormatter.format(priceRange.endInclusive.toDouble()),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandPrimary
                )
            }
        }

        // Range Slider Material 3 Component
        RangeSlider(
            value = priceRange,
            onValueChange = { onRangeChange(it) },
            valueRange = 0f..5000000f,
            steps = 50,
            colors = SliderDefaults.colors(
                thumbColor = BrandPrimary,
                activeTrackColor = BrandPrimary,
                inactiveTrackColor = Slate200
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("price_range_slider")
        )

        // Quick Budget Preset Chips
        Text(
            "Quick Budget Presets",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Slate600
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                BudgetPresetChip(
                    label = "All",
                    isSelected = priceRange.start == 0f && priceRange.endInclusive >= 5000000f,
                    onClick = onReset
                )
            }
            item {
                BudgetPresetChip(
                    label = "Under 50k",
                    isSelected = priceRange.start == 0f && priceRange.endInclusive == 50000f,
                    onClick = { onRangeChange(0f..50000f) }
                )
            }
            item {
                BudgetPresetChip(
                    label = "50k - 200k",
                    isSelected = priceRange.start == 50000f && priceRange.endInclusive == 200000f,
                    onClick = { onRangeChange(50000f..200000f) }
                )
            }
            item {
                BudgetPresetChip(
                    label = "200k - 1M",
                    isSelected = priceRange.start == 200000f && priceRange.endInclusive == 1000000f,
                    onClick = { onRangeChange(200000f..1000000f) }
                )
            }
            item {
                BudgetPresetChip(
                    label = "1M - 3M",
                    isSelected = priceRange.start == 1000000f && priceRange.endInclusive == 3000000f,
                    onClick = { onRangeChange(1000000f..3000000f) }
                )
            }
            item {
                BudgetPresetChip(
                    label = "3M+",
                    isSelected = priceRange.start == 3000000f,
                    onClick = { onRangeChange(3000000f..5000000f) }
                )
            }
        }
    }
}

@Composable
fun BudgetPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) BrandPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BrandPrimary else Slate300),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("budget_chip_$label")
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Slate800,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SearchAndBuyHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenPostAd: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Surface(
        color = BrandPrimary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(16.dp))
                    Text("Kampala & All Uganda", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenPostAd,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("header_buy_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                        Text("+ BUY ITEM", color = Slate950, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search phones, laptops, cars, fashion, crafts...", color = Slate400, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = BrandPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandAccent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Slate900,
                    unfocusedTextColor = Slate900
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("search_text_field")
            )
        }
    }
}

@Composable
fun PromotionalProcurementBanner(
    onOpenPostAd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BrandPrimaryDark, BrandPrimary, Color(0xFF1E40AF))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandAccent
                    ) {
                        Text(
                            "UGANDA DIRECT PROCUREMENT",
                            color = Slate950,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "Looking to buy something?",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Instant quotes from top verified Ugandan dealers with same-day Boda delivery.",
                        color = Color(0xFFBFDBFE),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onOpenPostAd,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("banner_buy_now_btn")
                    ) {
                        Text("Request / Buy Now", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListControlBar(
    selectedCategory: String,
    nonFoodCategories: List<ProductCategory>,
    productCount: Int,
    sortOrder: ProductSortOrder,
    showSortMenu: Boolean,
    onToggleSortMenu: (Boolean) -> Unit,
    onSelectSortOrder: (ProductSortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                if (selectedCategory == ProductCategory.ALL.name) "Verified Catalog" else nonFoodCategories.find { it.name == selectedCategory }?.displayName ?: selectedCategory,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
            )
            Text("$productCount items in budget range", style = MaterialTheme.typography.labelSmall, color = Slate500)
        }

        Box {
            OutlinedButton(
                onClick = { onToggleSortMenu(true) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("sort_filter_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(sortOrder.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onToggleSortMenu(false) }
            ) {
                ProductSortOrder.values().forEach { order ->
                    DropdownMenuItem(
                        text = { Text(order.title, fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            onSelectSortOrder(order)
                            onToggleSortMenu(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyProductState(onResetFilter: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
            Text("No products match your budget & filter", fontWeight = FontWeight.Bold, color = Slate700)
            Text("Try expanding your price range or clearing category filters.", fontSize = 12.sp, color = Slate500)
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onResetFilter,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reset Filters & Budget")
            }
        }
    }
}

fun getCategoryIcon(category: ProductCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        ProductCategory.ALL -> Icons.Default.Apps
        ProductCategory.VEHICLES -> Icons.Default.DirectionsCar
        ProductCategory.PROPERTY -> Icons.Default.Apartment
        ProductCategory.PHONES -> Icons.Default.Smartphone
        ProductCategory.ELECTRONICS -> Icons.Default.Tv
        ProductCategory.FASHION -> Icons.Default.Checkroom
        ProductCategory.HOME_FURNITURE -> Icons.Default.Chair
        ProductCategory.UGANDAN_CRAFTS -> Icons.Default.Palette
        ProductCategory.HEALTH_BEAUTY -> Icons.Default.Spa
        ProductCategory.HARDWARE -> Icons.Default.Handyman
        ProductCategory.SPORTS -> Icons.Default.FitnessCenter
        ProductCategory.SERVICES -> Icons.Default.Work
    }
}

@Composable
fun CategoryRoundChip(
    category: ProductCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = getCategoryIcon(category)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isSelected) BrandPrimary else Color.White)
                .border(1.dp, if (isSelected) BrandPrimary else Slate200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.displayName,
                tint = if (isSelected) Color.White else BrandPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (category == ProductCategory.ALL) "All" else category.displayName.split(" ")[0],
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BrandPrimary else Slate700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onProductClick: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}")
    ) {
        Column {
            // Product Image & Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                ProductImageView(
                    imageUri = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BrandPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(6.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.condition,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                if (product.discountPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = UgandaRed,
                        modifier = Modifier.padding(6.dp).align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Product Details
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = UgxFormatter.format(product.finalPriceUgx),
                    color = BrandPrimaryDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )

                if (product.discountPercent > 0) {
                    Text(
                        text = UgxFormatter.format(product.priceUgx),
                        color = Slate400,
                        fontSize = 10.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(11.dp))
                    Text(
                        text = product.originRegion,
                        color = Slate500,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // DIRECT BUY BUTTON (Touch target >= 48dp container)
                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("buy_button_${product.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BUY", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
