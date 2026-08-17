package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CraneViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<String> = emptyList(),
    val isAgent: Boolean = false
)

enum class MessageSender {
    USER, BOT, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    viewModel: CraneViewModel,
    onNavigateToShop: () -> Unit = {},
    onNavigateToPostAd: () -> Unit = {}, // triggers + BUY
    onNavigateToTracking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var isBotTyping by remember { mutableStateOf(false) }

    val initialMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.BOT,
                text = "Oli otya! 👋 Welcome to Crane Stores Uganda Customer Support. I'm your 24/7 AI shopping & procurement assistant.",
                suggestedActions = listOf(
                    "🛵 Delivery rates & Boda times",
                    "📱 How to pay with MTN MoMo",
                    "📲 Airtel Money payment steps",
                    "🛍️ How to request/buy items (+ BUY)",
                    "🔒 Buyer protection & Escrow"
                )
            ),
            ChatMessage(
                sender = MessageSender.BOT,
                text = "How can I help you today? You can ask about item prices, express boda deliveries across Kampala, tech gadgets, fashion fabrics, or submit a custom buy request!"
            )
        )
    }

    val quickQuestions = listOf(
        "🛵 Delivery rates to Kampala",
        "📱 MTN MoMo pay steps",
        "📲 Airtel Money pay",
        "🛍️ Request an item (+ BUY)",
        "📱 Smartphones & Gadgets",
        "👗 Kitenge Fabric details",
        "🔒 Buyer Escrow & Safety",
        "🎁 Loyalty points & rewards"
    )

    fun getBotResponse(userPrompt: String): Pair<String, List<String>> {
        val q = userPrompt.lowercase(Locale.ROOT)
        return when {
            q.contains("delivery") || q.contains("boda") || q.contains("rate") || q.contains("kampala") || q.contains("shipping") -> {
                "🛵 **Delivery in Uganda:**\n\n• **Boda Boda Express (Kampala & Wakiso):** Delivered in 25–45 minutes for UGX 5,000.\n• **Same-Day Van (Central Region):** UGX 10,000 (Jinja, Entebbe, Mukono).\n• **Upcountry Courier (Mbarara, Gulu, Mbale, Fort Portal):** 24–48 hours for UGX 15,000.\n\nAll orders include real-time live GPS tracking!" to listOf("Track my current order", "Browse marketplace", "Submit a Buy Request")
            }
            q.contains("momo") || q.contains("mtn") || q.contains("165") -> {
                "📱 **Paying with MTN Mobile Money (*165#):**\n\n1. Select MTN MoMo at checkout.\n2. Enter your Ugandan MTN phone number (+256 77...).\n3. You will receive an instant USSD authorization prompt on your phone.\n4. Enter your 4-5 digit MoMo PIN to authorize payment.\n5. Transaction is verified instantly with zero extra charge!" to listOf("Go to Marketplace", "Track order", "Contact support")
            }
            q.contains("airtel") || q.contains("185") -> {
                "📲 **Paying with Airtel Money (*185#):**\n\n1. Select Airtel Money on the payment options screen.\n2. Provide your Airtel phone number (+256 70... / +256 75...).\n3. Approve the pop-up push request with your Airtel Money PIN.\n4. You will receive an instant SMS confirmation and your order is immediately dispatched!" to listOf("Go to Marketplace", "Explore products")
            }
            q.contains("buy") || q.contains("request") || q.contains("order") || q.contains("item") || q.contains("sourcing") -> {
                "🛍️ **How to Direct Buy or Request Sourcing (+ BUY):**\n\n1. Tap the **'+ BUY'** button on the bottom bar.\n2. Enter the item name (e.g. iPhone 15, Kitenge Fabric, Furniture, Tools).\n3. Set your target budget in UGX and delivery location.\n4. Submit your order — our verified merchant network matches you instantly with same-day Boda delivery!" to listOf("Submit a Buy Request Now", "View All Categories")
            }
            q.contains("safe") || q.contains("scam") || q.contains("fraud") || q.contains("trust") || q.contains("escrow") -> {
                "🔒 **Crane Stores Uganda Buyer Protection & Escrow:**\n\n• **Direct Verification**: All merchants are identity-verified in Uganda.\n• **Inspect on Delivery**: Inspect your order physically upon Boda arrival.\n• **Escrow Protected**: Funds are held securely and only released when you confirm satisfactory delivery.\n• **24/7 Support**: Report any issues to our Kampala support hotline." to listOf("View verified products", "Submit a Buy Request")
            }
            q.contains("phone") || q.contains("samsung") || q.contains("gadget") || q.contains("electronic") -> {
                "📱 **Phones & Electronics Catalog:**\n\nWe feature certified Samsung, iPhone, Smart TVs, Sound systems, and accessories from vetted Ugandan suppliers with local warranty coverage." to listOf("View Phones & Tablets", "View Electronics & TV")
            }
            q.contains("kitenge") || q.contains("fabric") || q.contains("fashion") -> {
                "👗 **Authentic Ugandan & East African Kitenge Fabrics:**\n\n100% premium wax cotton prints sourced directly from Owino Market, Kampala. Available in 6-yard bolts with vibrant geometric motifs." to listOf("View Fashion Category", "Buy Kitenge Fabric")
            }
            q.contains("points") || q.contains("reward") || q.contains("loyalty") || q.contains("spin") -> {
                "🎁 **Crane Club Loyalty & Rewards:**\n\n• Earn **1 Point for every UGX 1,000** spent on orders.\n• Spin the Daily Lucky Wheel to win up to **500 free bonus points**!\n• Redeem points for instant discount vouchers (e.g. CRANE5K, FREEDEL, CRANE30K) at checkout." to listOf("Browse marketplace", "View loyalty vouchers")
            }
            q.contains("agent") || q.contains("human") || q.contains("call") || q.contains("help") -> {
                "👤 **Connecting to Ugandan Customer Representative:**\n\nOur support desk at Plot 14 Acacia Avenue, Kampala is online from 7:00 AM to 10:00 PM EAT.\n\n📞 Direct Hotline: **+256 770 123 456**\n💬 WhatsApp Support: **+256 772 884 192**\n📧 Email: **support@cranestores.ug**" to listOf("Call Hotline", "Back to Shopping")
            }
            else -> {
                "Thank you for your question! 😊\n\nI can help you browse non-food products in Uganda, calculate Boda delivery costs to your town, guide you through MTN MoMo / Airtel Money payments, or submit a custom + BUY sourcing request." to listOf("🛵 Delivery rates", "📱 MoMo payment", "🛍️ Request Item (+ BUY)", "👗 Kitenge Fabric")
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        initialMessages.add(userMsg)
        inputMessage = ""

        coroutineScope.launch {
            listState.animateScrollToItem(initialMessages.size - 1)
            isBotTyping = true
            delay(1000)
            val (botReply, actions) = getBotResponse(text)
            isBotTyping = false
            initialMessages.add(
                ChatMessage(
                    sender = MessageSender.BOT,
                    text = botReply,
                    suggestedActions = actions
                )
            )
            listState.animateScrollToItem(initialMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Chatbot Header Bar
        Surface(
            color = BrandPrimary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.White, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = "Crane Bot",
                                tint = BrandPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Crane AI Assistant",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4ADE80),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                            Text(
                                "Uganda Shopping & Sourcing Guide",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable {
                            initialMessages.clear()
                            initialMessages.add(
                                ChatMessage(
                                    sender = MessageSender.BOT,
                                    text = "Chat cleared! How may I help you with shopping, Boda delivery, or sourcing items in Uganda?",
                                    suggestedActions = listOf("🛵 Delivery rates", "📱 MoMo Pay", "🛍️ Request Item (+ BUY)")
                                )
                            )
                        }
                    ) {
                        Text(
                            "Clear",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips Bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickQuestions.forEach { question ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandPrimaryLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable { sendMessage(question) }
                    ) {
                        Text(
                            text = question,
                            color = BrandPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(initialMessages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onActionClick = { action ->
                        when {
                            action.contains("Buy") || action.contains("Request") || action.contains("Sourcing") -> onNavigateToPostAd()
                            action.contains("Track") -> onNavigateToTracking()
                            action.contains("Cart") || action.contains("Order") || action.contains("Shop") || action.contains("Market") || action.contains("Phone") || action.contains("Fashion") -> onNavigateToShop()
                            else -> sendMessage(action)
                        }
                    }
                )
            }

            if (isBotTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(BrandPrimary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandPrimary
                                )
                                Text("Crane Bot is typing...", fontSize = 12.sp, color = Slate600)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Chat Input Bar
        Surface(
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Ask about Boda delivery, MoMo, buying items...", fontSize = 13.sp) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("chat_input_field")
                )

                IconButton(
                    onClick = { sendMessage(inputMessage) },
                    enabled = inputMessage.isNotBlank(),
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (inputMessage.isNotBlank()) BrandPrimary else Slate200,
                            shape = CircleShape
                        )
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputMessage.isNotBlank()) Color.White else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onActionClick: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(BrandPrimary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    color = if (isUser) BrandPrimary else Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = message.text,
                            color = if (isUser) Color.White else Slate900,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedTime,
                            color = if (isUser) Color.White.copy(alpha = 0.7f) else Slate400,
                            fontSize = 10.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Suggested Action Chips below Bot message
                if (!isUser && message.suggestedActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.suggestedActions.forEach { action ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BrandAccentLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandAccent.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onActionClick(action) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = action,
                                        color = BrandAccentDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = BrandAccentDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
