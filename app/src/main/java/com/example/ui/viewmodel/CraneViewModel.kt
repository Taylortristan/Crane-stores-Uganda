package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CraneDatabase
import com.example.data.model.*
import com.example.data.repository.AnalyticsSummary
import com.example.data.repository.CraneRepository
import com.example.util.ImageStorageHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ProductSortOrder(val title: String) {
    FEATURED("Featured & Popular"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    RATING("Highest Rated")
}

sealed interface PaymentUiState {
    object Idle : PaymentUiState
    data class Processing(val method: PaymentMethod, val stepText: String) : PaymentUiState
    data class UssdPinPrompt(
        val method: PaymentMethod,
        val amountUgx: Double,
        val phone: String,
        val customerName: String,
        val deliveryAddress: String,
        val city: String,
        val deliveryMethod: DeliveryMethod,
        val customerNotes: String
    ) : PaymentUiState
    data class Success(val order: OrderEntity) : PaymentUiState
    data class Error(val message: String) : PaymentUiState
}


class CraneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CraneDatabase.getDatabase(application, viewModelScope)
    private val repository = CraneRepository(db)

    init {
        NotificationHelper.initNotificationChannel(application)
        startTrackingSimulationLoop()
    }

    // Navigation / Current Screen state
    private val _currentTab = MutableStateFlow(0) // 0: Shop, 1: AI Assistant, 2: Tracking, 3: Rewards/Loyalty, 4: Admin, 5: Notifications
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    // Admin Mode & Access Control (Hidden from customers)
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    fun verifyAdminPin(pin: String): Boolean {
        // Master Admin PIN for Crane Stores Management
        if (pin == "2560" || pin == "1234") {
            _isAdminLoggedIn.value = true
            _currentTab.value = 4 // Switch to Admin Dashboard
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentTab.value = 0 // Switch back to Shop
    }

    // Category & Search
    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL.name)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(ProductSortOrder.FEATURED)
    val sortOrder: StateFlow<ProductSortOrder> = _sortOrder.asStateFlow()

    // Price Range Budget Filter (UGX 0 to 5,000,000+)
    private val _priceRange = MutableStateFlow<ClosedFloatingPointRange<Float>>(0f..5000000f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange.asStateFlow()

    fun setPriceRange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun resetPriceFilter() {
        _priceRange.value = 0f..5000000f
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: ProductSortOrder) {
        _sortOrder.value = order
    }

    // Filtered Products Flow
    val products: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _selectedCategory,
        _searchQuery,
        _sortOrder,
        _priceRange
    ) { allProds, cat, query, sort, range ->
        var list = allProds

        if (cat != ProductCategory.ALL.name) {
            list = list.filter { it.category == cat }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.originRegion.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }

        // Apply Budget / Price Range Filter
        list = list.filter { prod ->
            val price = prod.finalPriceUgx.toFloat()
            val minBound = range.start
            val maxBound = range.endInclusive
            if (maxBound >= 5000000f) {
                price >= minBound
            } else {
                price in minBound..maxBound
            }
        }

        when (sort) {
            ProductSortOrder.FEATURED -> list.sortedWith(compareByDescending<ProductEntity> { it.isFeatured }.thenByDescending { it.rating })
            ProductSortOrder.PRICE_LOW_TO_HIGH -> list.sortedBy { it.finalPriceUgx }
            ProductSortOrder.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.finalPriceUgx }
            ProductSortOrder.RATING -> list.sortedByDescending { it.rating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<ProductEntity>> = repository.featuredProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected product for detailed view
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    // Cart
    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartWithProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCartCount: StateFlow<Int> = repository.totalCartCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotalUgx: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.totalPriceUgx }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product.id, quantity)
        }
    }

    fun updateCartQuantity(productId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Checkout & Applied Voucher
    private val _appliedVoucher = MutableStateFlow<LoyaltyVoucherEntity?>(null)
    val appliedVoucher: StateFlow<LoyaltyVoucherEntity?> = _appliedVoucher.asStateFlow()

    private val _voucherError = MutableStateFlow<String?>(null)
    val voucherError: StateFlow<String?> = _voucherError.asStateFlow()

    fun applyVoucherCode(code: String) {
        viewModelScope.launch {
            _voucherError.value = null
            val voucher = repository.applyVoucherCode(code)
            if (voucher != null) {
                if (voucher.isRedeemed) {
                    _voucherError.value = "Voucher '$code' has already been redeemed."
                } else {
                    val currentSubtotal = cartSubtotalUgx.value
                    if (currentSubtotal < voucher.minSpendUgx) {
                        _voucherError.value = "Minimum spend of UGX ${String.format("%,.0f", voucher.minSpendUgx)} required."
                    } else {
                        _appliedVoucher.value = voucher
                    }
                }
            } else {
                _voucherError.value = "Invalid voucher code '$code'."
            }
        }
    }

    fun removeAppliedVoucher() {
        _appliedVoucher.value = null
        _voucherError.value = null
    }

    // Payment & Order Placement Flow
    private val _paymentState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val paymentState: StateFlow<PaymentUiState> = _paymentState.asStateFlow()

    fun initiateCheckout(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        city: String,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        customerNotes: String
    ) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.totalPriceUgx }
            val discount = appliedVoucher.value?.discountAmountUgx ?: 0.0
            val total = (subtotal + deliveryMethod.baseFeeUgx - discount).coerceAtLeast(0.0)

            when (paymentMethod) {
                PaymentMethod.MTN_MOMO, PaymentMethod.AIRTEL_MONEY -> {
                    _paymentState.value = PaymentUiState.UssdPinPrompt(
                        method = paymentMethod,
                        amountUgx = total,
                        phone = customerPhone,
                        customerName = customerName,
                        deliveryAddress = deliveryAddress,
                        city = city,
                        deliveryMethod = deliveryMethod,
                        customerNotes = customerNotes
                    )
                }
                PaymentMethod.CARD, PaymentMethod.CASH_ON_DELIVERY -> {
                    processOrderFinalization(
                        customerName, customerPhone, deliveryAddress, city, deliveryMethod, paymentMethod, customerNotes
                    )
                }
            }
        }
    }

    fun confirmUssdPinAndPay(
        pin: String,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        city: String,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        customerNotes: String
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentUiState.Processing(paymentMethod, "Authorizing mobile money transfer with ${paymentMethod.displayName}...")
            delay(1800)
            _paymentState.value = PaymentUiState.Processing(paymentMethod, "Verifying transaction reference & escrow...")
            delay(1200)

            processOrderFinalization(
                customerName, customerPhone, deliveryAddress, city, deliveryMethod, paymentMethod, customerNotes
            )
        }
    }

    private suspend fun processOrderFinalization(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        city: String,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        customerNotes: String
    ) {
        val items = cartItems.value
        val order = repository.createOrder(
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            city = city,
            deliveryMethod = deliveryMethod,
            paymentMethod = paymentMethod,
            cartItems = items,
            appliedVoucher = _appliedVoucher.value,
            customerNotes = customerNotes
        )

        _appliedVoucher.value = null
        _paymentState.value = PaymentUiState.Success(order)
        _selectedOrderId.value = order.id

        // Show push notification
        NotificationHelper.showNotification(
            getApplication(),
            "Order Confirmed (${order.id}) 🛍️",
            "Thank you ${order.customerName}! Your order of UGX ${String.format("%,.0f", order.totalUgx)} has been placed."
        )
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentUiState.Idle
    }

    // Orders & Tracking
    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrders: StateFlow<List<OrderEntity>> = repository.activeOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedOrderId = MutableStateFlow<String?>("CRN-8942")
    val selectedOrderId: StateFlow<String?> = _selectedOrderId.asStateFlow()

    fun selectOrderForTracking(orderId: String) {
        _selectedOrderId.value = orderId
        _currentTab.value = 1 // Switch to Tracking screen
    }

    val selectedOrder: StateFlow<OrderEntity?> = _selectedOrderId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getOrderFlowById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedOrderItems: StateFlow<List<OrderItemEntity>> = _selectedOrderId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getOrderItemsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loyalty Program
    val loyaltyProfile: StateFlow<LoyaltyProfileEntity?> = repository.loyaltyProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val loyaltyVouchers: StateFlow<List<LoyaltyVoucherEntity>> = repository.loyaltyVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSpinningWheel = MutableStateFlow(false)
    val isSpinningWheel: StateFlow<Boolean> = _isSpinningWheel.asStateFlow()

    private val _spinRewardWon = MutableStateFlow<Int?>(null)
    val spinRewardWon: StateFlow<Int?> = _spinRewardWon.asStateFlow()

    fun spinLoyaltyWheel() {
        if (_isSpinningWheel.value) return
        viewModelScope.launch {
            _isSpinningWheel.value = true
            _spinRewardWon.value = null

            // Spin animation duration
            delay(2800)

            val prizes = listOf(50, 100, 150, 200, 300, 500)
            val wonPoints = prizes.random()
            repository.claimSpinReward(wonPoints)

            _spinRewardWon.value = wonPoints
            _isSpinningWheel.value = false

            NotificationHelper.showNotification(
                getApplication(),
                "Lucky Spin Winner! 🎡",
                "You won +$wonPoints Crane Points! Check your rewards balance."
            )
        }
    }

    fun dismissSpinReward() {
        _spinRewardWon.value = null
    }

    fun redeemLoyaltyVoucher(voucher: LoyaltyVoucherEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.redeemVoucher(voucher)
            onResult(success)
        }
    }

    // Notifications
    val notifications: StateFlow<List<NotificationItemEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    // Admin Dashboard
    private val _analyticsSummary = MutableStateFlow<AnalyticsSummary?>(null)
    val analyticsSummary: StateFlow<AnalyticsSummary?> = _analyticsSummary.asStateFlow()

    fun refreshAnalytics() {
        viewModelScope.launch {
            _analyticsSummary.value = repository.getAnalyticsSummary()
        }
    }

    fun updateOrderStatusByAdmin(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            NotificationHelper.showNotification(
                getApplication(),
                "Order Status: ${newStatus.title} 📦",
                "Order #$orderId has been updated to ${newStatus.description}"
            )
        }
    }

    fun assignDriverByAdmin(orderId: String, riderName: String, phone: String, plate: String) {
        viewModelScope.launch {
            repository.assignDriver(orderId, riderName, phone, plate)
        }
    }

    fun addNewProductByAdmin(
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
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                name = name,
                category = category,
                description = description,
                priceUgx = priceUgx,
                discountPercent = discountPercent,
                stockQuantity = stock,
                originRegion = originRegion,
                unitLabel = unitLabel,
                isFeatured = isFeatured,
                imageUri = imageUri.ifBlank { "hero_uganda_market" }
            )
            repository.insertProduct(product)

            repository.postNotification(
                NotificationItemEntity(
                    title = "New Arrival: $name ✨",
                    message = "Fresh in stock from $originRegion at UGX ${String.format("%,.0f", priceUgx)}.",
                    type = NotificationType.PROMOTION
                )
            )
        }
    }

    fun updateProductByAdmin(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProductByAdmin(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun saveUploadedImage(context: Context, uri: Uri): String? {
        return ImageStorageHelper.saveUriToInternalStorage(context, uri)
    }

    // Real-Time Order Driver Movement Simulation Loop
    private var simulationJob: Job? = null
    private fun startTrackingSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(3500)
                val activeList = repository.activeOrders.first()
                for (order in activeList) {
                    if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                        val newProgress = (order.driverProgress + 0.08f).coerceAtMost(1.0f)
                        repository.updateDriverProgress(order.id, newProgress)

                        if (newProgress >= 1.0f) {
                            repository.updateOrderStatus(order.id, OrderStatus.DELIVERED)
                            NotificationHelper.showNotification(
                                getApplication(),
                                "Order Delivered! 📦🎉",
                                "Your order #${order.id} has arrived at ${order.deliveryAddress}. Enjoy!"
                            )
                        }
                    }
                }
            }
        }
    }
}
