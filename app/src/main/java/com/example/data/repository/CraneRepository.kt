package com.example.data.repository

import com.example.data.CraneDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class CraneRepository(private val db: CraneDatabase) {

    private val productDao = db.productDao()
    private val orderDao = db.orderDao()
    private val cartDao = db.cartDao()
    private val loyaltyDao = db.loyaltyDao()
    private val notificationDao = db.notificationDao()

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val featuredProducts: Flow<List<ProductEntity>> = productDao.getFeaturedProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return if (category == ProductCategory.ALL.name) {
            productDao.getAllProducts()
        } else {
            productDao.getProductsByCategory(category)
        }
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return if (query.isBlank()) {
            productDao.getAllProducts()
        } else {
            productDao.searchProducts(query)
        }
    }

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    fun getProductFlowById(id: Long): Flow<ProductEntity?> = productDao.getProductFlowById(id)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(id: Long) = productDao.deleteProductById(id)

    suspend fun updateStock(id: Long, stock: Int) = productDao.updateStock(id, stock)

    // Cart with hydrated products
    val cartWithProducts: Flow<List<CartItemWithProduct>> =
        combine(cartDao.getAllCartItems(), productDao.getAllProducts()) { cartItems, products ->
            val productMap = products.associateBy { it.id }
            cartItems.mapNotNull { item ->
                productMap[item.productId]?.let { prod ->
                    CartItemWithProduct(cartItem = item, product = prod)
                }
            }
        }

    val totalCartCount: Flow<Int> = cartDao.getTotalCartCount().map { it ?: 0 }

    suspend fun addToCart(productId: Long, quantity: Int = 1) {
        val existing = cartDao.getCartItem(productId)
        if (existing != null) {
            cartDao.updateQuantity(productId, existing.quantity + quantity)
        } else {
            cartDao.insertOrUpdate(CartItemEntity(productId = productId, quantity = quantity))
        }
    }

    suspend fun updateCartQuantity(productId: Long, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: Long) = cartDao.deleteCartItem(productId)

    suspend fun clearCart() = cartDao.clearCart()

    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val activeOrders: Flow<List<OrderEntity>> = orderDao.getActiveOrders()

    suspend fun getOrderById(orderId: String): OrderEntity? = orderDao.getOrderById(orderId)

    fun getOrderFlowById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderFlowById(orderId)

    fun getOrderItemsFlow(orderId: String): Flow<List<OrderItemEntity>> = orderDao.getOrderItemsFlow(orderId)

    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        city: String,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        cartItems: List<CartItemWithProduct>,
        appliedVoucher: LoyaltyVoucherEntity?,
        customerNotes: String
    ): OrderEntity {
        val orderNum = (1000..9999).random()
        val orderId = "CRN-$orderNum"

        val subtotal = cartItems.sumOf { it.totalPriceUgx }
        val deliveryFee = deliveryMethod.baseFeeUgx
        val discount = appliedVoucher?.discountAmountUgx ?: 0.0
        val total = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)

        // Points earned: 1 point for every 1,000 UGX spent
        val pointsEarned = (total / 1000).toInt()
        val pointsUsed = appliedVoucher?.pointsRequired ?: 0

        val itemsSummary = cartItems.joinToString(", ") { "${it.product.name} (${it.cartItem.quantity}x)" }

        val order = OrderEntity(
            id = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryCity = city,
            deliveryMethod = deliveryMethod.displayName,
            paymentMethod = paymentMethod.displayName,
            paymentStatus = "PAID",
            transactionRef = when (paymentMethod) {
                PaymentMethod.MTN_MOMO -> "MTN-UG-${System.currentTimeMillis() % 100000000}"
                PaymentMethod.AIRTEL_MONEY -> "AIRTEL-UG-${System.currentTimeMillis() % 100000000}"
                PaymentMethod.CARD -> "CARD-AUTH-${(100000..999999).random()}"
                PaymentMethod.CASH_ON_DELIVERY -> "COD-PENDING"
            },
            status = OrderStatus.PLACED,
            subtotalUgx = subtotal,
            deliveryFeeUgx = deliveryFee,
            discountUgx = discount,
            totalUgx = total,
            pointsEarned = pointsEarned,
            pointsRedeemed = pointsUsed,
            riderName = "Kato Sulaiman",
            riderPhone = "+256 772 884 192",
            riderPlate = "UFF 842K (Bajaj Boxer)",
            driverProgress = 0.05f,
            customerNotes = customerNotes,
            itemsSummary = itemsSummary
        )

        orderDao.insertOrder(order)

        val orderItemEntities = cartItems.map {
            OrderItemEntity(
                orderId = orderId,
                productId = it.product.id,
                productName = it.product.name,
                productImage = it.product.imageUri,
                unitPriceUgx = it.product.finalPriceUgx,
                quantity = it.cartItem.quantity,
                subtotalUgx = it.totalPriceUgx
            )
        }
        orderDao.insertOrderItems(orderItemEntities)

        // Clear cart
        cartDao.clearCart()

        // Update loyalty points
        loyaltyDao.addPoints(pointsEarned)
        if (pointsUsed > 0 && appliedVoucher != null) {
            loyaltyDao.markVoucherRedeemed(appliedVoucher.id)
            loyaltyDao.deductPoints(pointsUsed)
        }

        // Add Push Notification
        notificationDao.insertNotification(
            NotificationItemEntity(
                title = "Order Confirmed ($orderId) 🛍️",
                message = "Your order for $itemsSummary totaling UGX ${String.format("%,.0f", total)} is being processed.",
                type = NotificationType.ORDER_STATUS,
                relatedOrderId = orderId
            )
        )

        return order
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status)
        notificationDao.insertNotification(
            NotificationItemEntity(
                title = "Order Status Update: ${status.title} 📦",
                message = "Order #$orderId is now: ${status.description}",
                type = NotificationType.ORDER_STATUS,
                relatedOrderId = orderId
            )
        )
    }

    suspend fun updateDriverProgress(orderId: String, progress: Float) {
        orderDao.updateDriverProgress(orderId, progress)
    }

    suspend fun assignDriver(orderId: String, riderName: String, riderPhone: String, riderPlate: String) {
        orderDao.assignRider(orderId, riderName, riderPhone, riderPlate)
        notificationDao.insertNotification(
            NotificationItemEntity(
                title = "Driver Assigned! 🛵",
                message = "$riderName ($riderPlate) has been dispatched for order #$orderId.",
                type = NotificationType.ORDER_STATUS,
                relatedOrderId = orderId
            )
        )
    }

    // Loyalty
    val loyaltyProfile: Flow<LoyaltyProfileEntity?> = loyaltyDao.getProfileFlow()
    val loyaltyVouchers: Flow<List<LoyaltyVoucherEntity>> = loyaltyDao.getAllVouchers()

    suspend fun redeemVoucher(voucher: LoyaltyVoucherEntity): Boolean {
        val profile = loyaltyDao.getProfile() ?: return false
        if (profile.pointsBalance >= voucher.pointsRequired) {
            loyaltyDao.deductPoints(voucher.pointsRequired)
            loyaltyDao.markVoucherRedeemed(voucher.id)
            notificationDao.insertNotification(
                NotificationItemEntity(
                    title = "Voucher Redeemed! 🎟️",
                    message = "You redeemed voucher code '${voucher.code}' for UGX ${String.format("%,.0f", voucher.discountAmountUgx)} off!",
                    type = NotificationType.LOYALTY_REWARD
                )
            )
            return true
        }
        return false
    }

    suspend fun claimSpinReward(pointsWon: Int) {
        loyaltyDao.addPoints(pointsWon)
        loyaltyDao.updateLastSpin(System.currentTimeMillis())
        notificationDao.insertNotification(
            NotificationItemEntity(
                title = "Lucky Wheel Winner! 🎡",
                message = "You spun the Crane Wheel and won +$pointsWon Crane Points!",
                type = NotificationType.LOYALTY_REWARD
            )
        )
    }

    suspend fun applyVoucherCode(code: String): LoyaltyVoucherEntity? {
        return loyaltyDao.getVoucherByCode(code.trim().uppercase())
    }

    // Notifications
    val allNotifications: Flow<List<NotificationItemEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun postNotification(notification: NotificationItemEntity) = notificationDao.insertNotification(notification)

    // Analytics Helper for Admin Dashboard
    suspend fun getAnalyticsSummary(): AnalyticsSummary {
        val totalCount = orderDao.getTotalOrderCount()
        val totalRevenue = orderDao.getTotalRevenue() ?: 0.0
        val avgOrderValue = if (totalCount > 0) totalRevenue / totalCount else 0.0

        return AnalyticsSummary(
            totalOrders = totalCount,
            totalRevenueUgx = totalRevenue,
            averageOrderValueUgx = avgOrderValue,
            activeShoppers = 142,
            growthRate = "+24.5%",
            topSellingCategory = "Coffee & Beverages",
            bodaDeliveryRate = "94.2%"
        )
    }
}

data class AnalyticsSummary(
    val totalOrders: Int,
    val totalRevenueUgx: Double,
    val averageOrderValueUgx: Double,
    val activeShoppers: Int,
    val growthRate: String,
    val topSellingCategory: String,
    val bodaDeliveryRate: String
)
