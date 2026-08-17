package com.example.data.dao

import androidx.room.*
import com.example.data.model.LoyaltyProfileEntity
import com.example.data.model.LoyaltyVoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM loyalty_profile WHERE id = 1")
    fun getProfileFlow(): Flow<LoyaltyProfileEntity?>

    @Query("SELECT * FROM loyalty_profile WHERE id = 1")
    suspend fun getProfile(): LoyaltyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: LoyaltyProfileEntity)

    @Query("UPDATE loyalty_profile SET pointsBalance = pointsBalance + :points, lifetimePoints = lifetimePoints + :points WHERE id = 1")
    suspend fun addPoints(points: Int)

    @Query("UPDATE loyalty_profile SET pointsBalance = pointsBalance - :points WHERE id = 1")
    suspend fun deductPoints(points: Int)

    @Query("UPDATE loyalty_profile SET lastSpinTimestamp = :timestamp WHERE id = 1")
    suspend fun updateLastSpin(timestamp: Long)

    @Query("SELECT * FROM loyalty_vouchers ORDER BY isRedeemed ASC, pointsRequired ASC")
    fun getAllVouchers(): Flow<List<LoyaltyVoucherEntity>>

    @Query("SELECT * FROM loyalty_vouchers WHERE code = :code LIMIT 1")
    suspend fun getVoucherByCode(code: String): LoyaltyVoucherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: LoyaltyVoucherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVouchers(vouchers: List<LoyaltyVoucherEntity>)

    @Query("UPDATE loyalty_vouchers SET isRedeemed = 1 WHERE id = :id")
    suspend fun markVoucherRedeemed(id: Long)
}
