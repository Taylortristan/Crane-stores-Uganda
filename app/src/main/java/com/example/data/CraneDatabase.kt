package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CartItemEntity::class,
        LoyaltyProfileEntity::class,
        LoyaltyVoucherEntity::class,
        NotificationItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CraneDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: CraneDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CraneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CraneDatabase::class.java,
                    "crane_stores_uganda.db"
                ).fallbackToDestructiveMigration()
                .addCallback(CraneDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class CraneDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: CraneDatabase) {
            val productDao = database.productDao()
            val loyaltyDao = database.loyaltyDao()
            val notificationDao = database.notificationDao()
            val orderDao = database.orderDao()

            if (productDao.getProductCount() == 0) {
                val initialProducts = listOf(
                    ProductEntity(
                        id = 1,
                        name = "Kampala Vibrant Kitenge Wax Fabric (6 Yards)",
                        category = ProductCategory.FASHION.name,
                        description = "Authentic East African 100% premium cotton wax print fabric with bright geometric motifs. Premium texture for custom tailoring, suits, and dresses.",
                        priceUgx = 65000.0,
                        discountPercent = 10,
                        rating = 4.9,
                        reviewCount = 58,
                        imageUri = "hero_uganda_market",
                        stockQuantity = 45,
                        isFeatured = true,
                        originRegion = "Owino Market, Kampala",
                        unitLabel = "6 Yards Bolt",
                        condition = "Brand New",
                        sellerName = "Owino Textiles Hub"
                    ),
                    ProductEntity(
                        id = 2,
                        name = "Handcrafted Ankole Horn Serving Bowl",
                        category = ProductCategory.UGANDAN_CRAFTS.name,
                        description = "Artisanal hand-carved and polished bowl crafted from natural ethical Ankole longhorn cattle horn. Unique marble grain pattern.",
                        priceUgx = 52000.0,
                        discountPercent = 0,
                        rating = 4.9,
                        reviewCount = 112,
                        imageUri = "ic_crane_logo",
                        stockQuantity = 60,
                        isFeatured = true,
                        originRegion = "Mbarara Artisans Hub",
                        unitLabel = "Handmade piece",
                        condition = "Handmade",
                        sellerName = "Western Crafts Collective"
                    ),
                    ProductEntity(
                        id = 3,
                        name = "Samsung Galaxy A54 5G (128GB, Dual SIM)",
                        category = ProductCategory.PHONES.name,
                        description = "Super AMOLED 120Hz display with 50MP OIS camera and 5000mAh battery. Official warranty included with free screen protector.",
                        priceUgx = 1150000.0,
                        discountPercent = 8,
                        rating = 4.8,
                        reviewCount = 74,
                        imageUri = "ic_crane_logo",
                        stockQuantity = 20,
                        isFeatured = true,
                        originRegion = "Kampala Road, Central",
                        unitLabel = "Complete Box Set",
                        condition = "Brand New",
                        sellerName = "Kampala Smart Gadgets"
                    ),
                    ProductEntity(
                        id = 4,
                        name = "Uganda Boda Boda Certified Smart Helmet",
                        category = ProductCategory.VEHICLES.name,
                        description = "High impact ABS shell helmet with ventilation ducts and UNBS safety certification. Essential for commuter & rider safety across Kampala.",
                        priceUgx = 75000.0,
                        discountPercent = 10,
                        rating = 4.8,
                        reviewCount = 63,
                        imageUri = "ic_crane_logo",
                        stockQuantity = 35,
                        isFeatured = false,
                        originRegion = "Kampala Motors Hub",
                        unitLabel = "1 Helmet with Visor",
                        condition = "Brand New",
                        sellerName = "Apex Moto Uganda"
                    ),
                    ProductEntity(
                        id = 5,
                        name = "Organic Pure Nilotica Shea Butter (250g)",
                        category = ProductCategory.HEALTH_BEAUTY.name,
                        description = "Cold-pressed virgin East African Nilotica Shea Butter from Northern Uganda. Naturally soft, creamy texture ideal for skin moisture & hair care.",
                        priceUgx = 22000.0,
                        discountPercent = 5,
                        rating = 4.9,
                        reviewCount = 89,
                        imageUri = "ic_crane_logo",
                        stockQuantity = 80,
                        isFeatured = false,
                        originRegion = "Gulu District",
                        unitLabel = "250g Jar",
                        condition = "Brand New",
                        sellerName = "Nilotica Organics Uganda"
                    ),
                    ProductEntity(
                        id = 6,
                        name = "Smart LED TV 43-inch 4K UHD with HDR",
                        category = ProductCategory.ELECTRONICS.name,
                        description = "Vibrant 4K resolution with built-in YouTube, Netflix, digital DVB-T2 tuner for local Ugandan channels, and HDMI/USB inputs.",
                        priceUgx = 890000.0,
                        discountPercent = 12,
                        rating = 4.7,
                        reviewCount = 47,
                        imageUri = "hero_uganda_market",
                        stockQuantity = 15,
                        isFeatured = true,
                        originRegion = "Lugogo Mall, Kampala",
                        unitLabel = "Unit in Box",
                        condition = "Brand New",
                        sellerName = "City Electronics Ltd"
                    ),
                    ProductEntity(
                        id = 7,
                        name = "Modern Solid Mahogany Coffee Table",
                        category = ProductCategory.HOME_FURNITURE.name,
                        description = "Custom seasoned Ugandan mahogany wood with protective lacquer finish and lower storage shelf. Handcrafted by master carpenters in Nsambya.",
                        priceUgx = 280000.0,
                        discountPercent = 0,
                        rating = 4.9,
                        reviewCount = 38,
                        imageUri = "hero_uganda_market",
                        stockQuantity = 10,
                        isFeatured = true,
                        originRegion = "Nsambya Carpentry Hub",
                        unitLabel = "Finished Unit",
                        condition = "Brand New",
                        sellerName = "Nsambya Woodworks"
                    ),
                    ProductEntity(
                        id = 8,
                        name = "Heavy Duty 20V Cordless Drill & Tool Set",
                        category = ProductCategory.HARDWARE.name,
                        description = "Powerful brushless motor drill with 2 Lithium batteries, charger, and 45-piece drill and screwdriver bit accessory kit in sturdy toolbox.",
                        priceUgx = 185000.0,
                        discountPercent = 15,
                        rating = 4.8,
                        reviewCount = 31,
                        imageUri = "ic_crane_logo",
                        stockQuantity = 25,
                        isFeatured = false,
                        originRegion = "Nakivubo Road, Kampala",
                        unitLabel = "Full Kit in Case",
                        condition = "Brand New",
                        sellerName = "Builders Warehouse UG"
                    ),
                    ProductEntity(
                        id = 9,
                        name = "Traditional Handwoven Raffia Wall Art Plate",
                        category = ProductCategory.UGANDAN_CRAFTS.name,
                        description = "Intricately coiled sweetgrass and sisal wall basket. Made by women artisans in Western Uganda with natural vegetable dyes.",
                        priceUgx = 35000.0,
                        discountPercent = 0,
                        rating = 4.9,
                        reviewCount = 29,
                        imageUri = "ic_crane_logo",
                        stockQuantity = 40,
                        isFeatured = false,
                        originRegion = "Fort Portal Artisans",
                        unitLabel = "12-inch Plate",
                        condition = "Handmade",
                        sellerName = "Empower Uganda Crafts"
                    ),
                    ProductEntity(
                        id = 10,
                        name = "Heavyweight Boxing & Fitness Punching Bag (120cm)",
                        category = ProductCategory.SPORTS.name,
                        description = "Durable synthetic leather with heavy chain mount and reinforced stitching. Ideal for home gym fitness and martial arts training.",
                        priceUgx = 140000.0,
                        discountPercent = 10,
                        rating = 4.7,
                        reviewCount = 22,
                        imageUri = "hero_uganda_market",
                        stockQuantity = 18,
                        isFeatured = false,
                        originRegion = "Equatorial Mall, Kampala",
                        unitLabel = "Filled 30kg Bag",
                        condition = "Brand New",
                        sellerName = "Kampala Fitness Pro"
                    )
                )
                productDao.insertAll(initialProducts)
            }

            // Populate Loyalty profile and vouchers
            loyaltyDao.insertOrUpdateProfile(
                LoyaltyProfileEntity(
                    id = 1,
                    customerName = "Taylor Tristan",
                    phoneNumber = "+256 770 123 456",
                    pointsBalance = 850,
                    lifetimePoints = 1420,
                    currentTier = LoyaltyTier.SILVER.name
                )
            )

            val vouchers = listOf(
                LoyaltyVoucherEntity(
                    code = "CRANE5K",
                    title = "UGX 5,000 Discount",
                    discountAmountUgx = 5000.0,
                    minSpendUgx = 25000.0,
                    pointsRequired = 200,
                    description = "Get UGX 5,000 off any order over UGX 25,000."
                ),
                LoyaltyVoucherEntity(
                    code = "FREEDEL",
                    title = "Free Express Boda Delivery",
                    discountAmountUgx = 5000.0,
                    minSpendUgx = 30000.0,
                    pointsRequired = 250,
                    description = "Enjoy free Boda delivery within Kampala & Wakiso."
                ),
                LoyaltyVoucherEntity(
                    code = "CRANE15K",
                    title = "UGX 15,000 VIP Voucher",
                    discountAmountUgx = 15000.0,
                    minSpendUgx = 75000.0,
                    pointsRequired = 500,
                    description = "Get UGX 15,000 discount on electronics, phones, or fashion shopping."
                ),
                LoyaltyVoucherEntity(
                    code = "CRANE30K",
                    title = "UGX 30,000 Mega Saver",
                    discountAmountUgx = 30000.0,
                    minSpendUgx = 150000.0,
                    pointsRequired = 1000,
                    description = "Redeem UGX 30,000 voucher on large equipment or furniture purchases."
                )
            )
            loyaltyDao.insertAllVouchers(vouchers)

            // Initial active sample order for tracking demonstration
            val sampleOrderId = "CRN-8942"
            val sampleOrder = OrderEntity(
                id = sampleOrderId,
                customerName = "Taylor Tristan",
                customerPhone = "+256 770 123 456",
                deliveryAddress = "Plot 14 Acacia Avenue, Kololo",
                deliveryCity = "Kampala",
                deliveryMethod = DeliveryMethod.BODA_EXPRESS.displayName,
                paymentMethod = PaymentMethod.MTN_MOMO.displayName,
                paymentStatus = "PAID",
                transactionRef = "MTN-UG-993821048",
                status = OrderStatus.OUT_FOR_DELIVERY,
                subtotalUgx = 87000.0,
                deliveryFeeUgx = 5000.0,
                discountUgx = 5000.0,
                totalUgx = 87000.0,
                pointsEarned = 87,
                pointsRedeemed = 200,
                createdAt = System.currentTimeMillis() - (18 * 60 * 1000),
                estimatedDeliveryTime = System.currentTimeMillis() + (15 * 60 * 1000),
                riderName = "Kato Sulaiman",
                riderPhone = "+256 772 884 192",
                riderPlate = "UFF 842K (Bajaj Boxer)",
                riderRating = 4.9,
                driverProgress = 0.65f,
                customerNotes = "Call upon arrival at the gate.",
                itemsSummary = "Kampala Kitenge Fabric (1x), Nilotica Shea Butter (1x)"
            )
            orderDao.insertOrder(sampleOrder)

            // Initial notifications
            val sampleNotifications = listOf(
                NotificationItemEntity(
                    title = "Order En Route! 🛵",
                    message = "Your order #CRN-8942 is out for delivery with Kato Sulaiman (UFF 842K). Estimated arrival in 15 mins.",
                    type = NotificationType.ORDER_STATUS,
                    relatedOrderId = sampleOrderId,
                    isRead = false,
                    timestamp = System.currentTimeMillis() - (5 * 60 * 1000)
                ),
                NotificationItemEntity(
                    title = "Silver Tier Unlocked! 🌟",
                    message = "Congratulations! You earned 850 Crane points and unlocked Silver Explorer status.",
                    type = NotificationType.LOYALTY_REWARD,
                    isRead = false,
                    timestamp = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
                ),
                NotificationItemEntity(
                    title = "Mega Tech & Fashion Week ✨",
                    message = "Up to 15% discount on Smartphones, Kitenge Fabrics, and Handcrafted Ugandan goods this week.",
                    type = NotificationType.PROMOTION,
                    isRead = true,
                    timestamp = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000)
                )
            )
            notificationDao.insertAll(sampleNotifications)
        }
    }
}
