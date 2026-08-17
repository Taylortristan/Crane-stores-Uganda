package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus(val title: String, val description: String, val stepIndex: Int) {
    PLACED("Order Placed", "Order received and queued at Crane Hub", 0),
    CONFIRMED("Confirmed", "Merchant confirmed product availability", 1),
    PACKING("Packing Order", "Quality checked and safely packaged", 2),
    OUT_FOR_DELIVERY("Out for Delivery", "Express Boda rider is en route", 3),
    DELIVERED("Delivered", "Delivered to your doorstep", 4),
    CANCELLED("Cancelled", "Order has been cancelled", -1)
}

enum class PaymentMethod(val displayName: String, val subtitle: String, val ussdCode: String) {
    MTN_MOMO("MTN Mobile Money", "Instant MoMo Pay (*165#)", "*165*3#"),
    AIRTEL_MONEY("Airtel Money", "Airtel Pay Instant (*185#)", "*185*9#"),
    CARD("Visa / Mastercard", "Secure card checkout", ""),
    CASH_ON_DELIVERY("Cash on Delivery", "Pay rider upon delivery", "")
}

enum class DeliveryMethod(val displayName: String, val durationText: String, val baseFeeUgx: Double) {
    BODA_EXPRESS("Boda Boda Express", "25 - 45 mins", 5000.0),
    STANDARD_VAN("Standard Van Delivery", "2 - 4 hours", 8000.0),
    ECO_SAVER("Eco Saver (Next Day)", "Tomorrow morning", 3000.0)
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String, // e.g. "CRN-8291"
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryCity: String, // "Kampala", "Entebbe", "Jinja", "Wakiso", "Mbarara", "Gulu"
    val deliveryMethod: String,
    val paymentMethod: String,
    val paymentStatus: String, // "PAID", "PENDING", "FAILED"
    val transactionRef: String,
    val status: OrderStatus = OrderStatus.PLACED,
    val subtotalUgx: Double,
    val deliveryFeeUgx: Double,
    val discountUgx: Double = 0.0,
    val totalUgx: Double,
    val pointsEarned: Int = 0,
    val pointsRedeemed: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val estimatedDeliveryTime: Long = System.currentTimeMillis() + (35 * 60 * 1000),
    val riderName: String = "Kato Sulaiman",
    val riderPhone: String = "+256 772 884 192",
    val riderPlate: String = "UFF 842K (Bajaj Boxer)",
    val riderRating: Double = 4.9,
    val driverProgress: Float = 0.0f, // 0.0 to 1.0 along the simulated route
    val customerNotes: String = "",
    val itemsSummary: String = "" // JSON or text summary e.g. "Mount Elgon Coffee (2x), Matooke (1x)"
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val productId: Long,
    val productName: String,
    val productImage: String,
    val unitPriceUgx: Double,
    val quantity: Int,
    val subtotalUgx: Double
)
