package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val productId: Long,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val product: ProductEntity
) {
    val totalPriceUgx: Double
        get() = product.finalPriceUgx * cartItem.quantity
}
