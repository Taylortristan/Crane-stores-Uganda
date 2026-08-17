package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LoyaltyTier(
    val tierName: String,
    val minPoints: Int,
    val cashbackRate: Double, // percentage
    val perks: List<String>,
    val badgeColorHex: Long
) {
    BRONZE(
        "Bronze Shopper",
        0,
        0.02,
        listOf("Earn 1 pt per 1,000 UGX", "Standard Promotions", "Birthday Reward"),
        0xFFCD7F32
    ),
    SILVER(
        "Silver Explorer",
        500,
        0.05,
        listOf("5% Bonus Points", "Exclusive Weekend Flash Deals", "Priority Customer Support"),
        0xFF94A3B8
    ),
    GOLD(
        "Gold VIP",
        1500,
        0.08,
        listOf("8% Bonus Points", "Free Boda Delivery on orders > 50k", "Early Access to New Products"),
        0xFFEAB308
    ),
    PLATINUM(
        "Platinum Ambassador",
        3000,
        0.12,
        listOf("12% Cash Back Points", "Free Delivery on All Orders", "Dedicated Crane Personal Shopper", "Exclusive Seasonal Gifts"),
        0xFF38BDF8
    );

    companion object {
        fun fromPoints(points: Int): LoyaltyTier {
            return when {
                points >= PLATINUM.minPoints -> PLATINUM
                points >= GOLD.minPoints -> GOLD
                points >= SILVER.minPoints -> SILVER
                else -> BRONZE
            }
        }
    }
}

@Entity(tableName = "loyalty_profile")
data class LoyaltyProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val customerName: String = "Taylor Tristan",
    val phoneNumber: String = "+256 770 123 456",
    val pointsBalance: Int = 850,
    val lifetimePoints: Int = 1420,
    val currentTier: String = LoyaltyTier.SILVER.name,
    val lastSpinTimestamp: Long = 0L,
    val memberSince: Long = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
)

@Entity(tableName = "loyalty_vouchers")
data class LoyaltyVoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val title: String,
    val discountAmountUgx: Double,
    val minSpendUgx: Double,
    val pointsRequired: Int,
    val isRedeemed: Boolean = false,
    val expiryDate: String = "30 Sep 2026",
    val description: String
)
