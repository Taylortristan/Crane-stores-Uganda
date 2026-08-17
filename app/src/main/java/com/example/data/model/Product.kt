package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductCategory(val displayName: String, val iconName: String) {
    ALL("All Categories", "category"),
    VEHICLES("Vehicles & Bodas", "directions_car"),
    PROPERTY("Property & Houses", "apartment"),
    PHONES("Phones & Tablets", "smartphone"),
    ELECTRONICS("Electronics & TV", "devices"),
    FASHION("Fashion & Clothes", "checkroom"),
    HOME_FURNITURE("Home & Furniture", "chair"),
    UGANDAN_CRAFTS("Ugandan Crafts", "palette"),
    HEALTH_BEAUTY("Health & Beauty", "spa"),
    HARDWARE("Hardware & Tools", "handyman"),
    SPORTS("Sports & Fitness", "fitness_center"),
    SERVICES("Jobs & Services", "work")
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // from ProductCategory name
    val description: String,
    val priceUgx: Double,
    val discountPercent: Int = 0,
    val rating: Double = 4.8,
    val reviewCount: Int = 24,
    val imageUri: String, // Can be res drawable name (e.g. "hero_uganda_market") or file:// path
    val stockQuantity: Int = 50,
    val isFeatured: Boolean = false,
    val originRegion: String = "Kampala, Uganda",
    val unitLabel: String = "per item",
    val weightKg: Double = 1.0,
    val condition: String = "Brand New", // Brand New, Used (Like New), Refurbished, Handmade
    val sellerName: String = "Verified Ugandan Merchant",
    val sellerPhone: String = "+256 700 123 456",
    val isVerifiedSeller: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val finalPriceUgx: Double
        get() = if (discountPercent > 0) priceUgx * (1.0 - discountPercent / 100.0) else priceUgx
}
