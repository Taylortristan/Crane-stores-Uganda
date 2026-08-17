package com.example.data.dao

import androidx.room.*
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderFlowById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY createdAt DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsFlow(orderId: String): Flow<List<OrderItemEntity>>

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)

    @Query("UPDATE orders SET driverProgress = :progress WHERE id = :orderId")
    suspend fun updateDriverProgress(orderId: String, progress: Float)

    @Query("UPDATE orders SET riderName = :riderName, riderPhone = :riderPhone, riderPlate = :riderPlate WHERE id = :orderId")
    suspend fun assignRider(orderId: String, riderName: String, riderPhone: String, riderPlate: String)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getTotalOrderCount(): Int

    @Query("SELECT SUM(totalUgx) FROM orders WHERE paymentStatus = 'PAID'")
    suspend fun getTotalRevenue(): Double?
}
