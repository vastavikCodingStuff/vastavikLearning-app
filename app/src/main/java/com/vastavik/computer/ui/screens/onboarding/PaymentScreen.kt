package com.vastavik.computer.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.ui.components.NinjaCelebrationOverlay
import com.vastavik.computer.ui.theme.BrutalBoxCard
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.NeoBrutalistColors
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.utils.AdminSession
import com.vastavik.computer.utils.PaymentReceiptData
import com.vastavik.computer.utils.PaymentReceiptPdf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class RazorpayPaymentMethod(val label: String, val subtitle: String) {
    UPI_AUTOPAY("UPI AutoPay", "Mandate Authorization • 0% Fee • Cancel Anytime"),
    UPI_STANDARD("Standard UPI", "GPay, Paytm, BHIM, CRED UPI"),
    CARD("Credit or Debit Card", "Visa, MasterCard, RuPay, Maestro"),
    NETBANKING("NetBanking", "HDFC, SBI, ICICI, Axis, Kotak & 50+ Banks")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val bb = brutalBorderColor()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isAdmin by AdminSession.isAdmin.collectAsState()

    var selectedPlan by remember { mutableStateOf("monthly") }
    // UPI AutoPay is the primary and default selection
    var selectedMethod by remember { mutableStateOf(RazorpayPaymentMethod.UPI_AUTOPAY) }

    // Card Details State
    var cardType by remember { mutableStateOf("Credit") }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    // UPI Details State
    var upiId by remember { mutableStateOf("") }

    // Netbanking State
    val popularBanks = listOf("HDFC Bank", "SBI", "ICICI Bank", "Axis Bank", "Kotak Bank", "PNB")
    var selectedBank by remember { mutableStateOf("HDFC Bank") }

    var showPaySheet by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var generatedReceiptFile by remember { mutableStateOf<File?>(null) }

    val promoActive = true
    val currentAmount = if (selectedPlan == "monthly") (if (promoActive) "149" else "299") else (if (promoActive) "999" else "1999")
    val planDesc = if (selectedPlan == "monthly") "Vastavik Monthly Pro" else "Vastavik Yearly Pro"
    val planPeriod = if (selectedPlan == "monthly") "30 Days" else "365 Days"
    val baseAmount = if (selectedPlan == "monthly") "₹299" else "₹1,999"
    val discountAmount = if (selectedPlan == "monthly") "₹150" else "₹1,000"
    val gstAmount = if (selectedPlan == "monthly") "₹22.73" else "₹152.39"

    // Razorpay Payment Execution Flow
    fun processRazorpayPayment() {
        scope.launch {
            isProcessing = true
            delay(1000) // Razorpay secure transaction simulation

            val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
            val customerEmail = user?.email ?: "student@vastaviklearning.com"
            val customerName = user?.displayName ?: "Vastavik Student"

            val txId = "pay_rzp_" + UUID.randomUUID().toString().replace("-", "").take(14)
            val orderId = "order_rzp_" + UUID.randomUUID().toString().replace("-", "").take(14)
            val invNumber = "INV-2026-RZP-" + (100000..999999).random()

            val methodLabel = when (selectedMethod) {
                RazorpayPaymentMethod.UPI_AUTOPAY -> "UPI AutoPay (Mandate Active)"
                RazorpayPaymentMethod.UPI_STANDARD -> "Standard UPI (${upiId.ifEmpty { "UPI Instant" }})"
                RazorpayPaymentMethod.CARD -> "$cardType Card (•••• ${cardNumber.takeLast(4).ifEmpty { "4242" }})"
                RazorpayPaymentMethod.NETBANKING -> "NetBanking ($selectedBank)"
            }

            val receiptData = PaymentReceiptData(
                invoiceNumber = invNumber,
                transactionId = txId,
                orderId = orderId,
                planName = planDesc,
                planPeriod = planPeriod,
                amount = currentAmount,
                baseAmount = baseAmount,
                discountAmount = discountAmount,
                gstAmount = gstAmount,
                paymentMethod = methodLabel,
                gateway = "Razorpay",
                customerName = customerName,
                customerEmail = customerEmail
            )

            val receiptFile = PaymentReceiptPdf.generateReceipt(context, receiptData)
            generatedReceiptFile = receiptFile

            isProcessing = false
            showPaySheet = false
            showCelebration = true
        }
    }

    // Fullscreen Ninja Samurai Celebration Overlay after payment success
    if (showCelebration) {
        NinjaCelebrationOverlay(
            planName = planDesc,
            amount = currentAmount,
            paymentMethod = selectedMethod.label,
            receiptFile = generatedReceiptFile,
            onDownloadReceipt = {
                generatedReceiptFile?.let { file ->
                    PaymentReceiptPdf.openPdf(context, file)
                } ?: scope.launch {
                    snackbarHostState.showSnackbar("Generating PDF receipt...")
                }
            },
            onEnterApp = {
                showCelebration = false
                onNavigate("payment_history")
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to Pro", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Admin Status Banner (Always-on Admin Access)
                if (isAdmin) {
                    BrutalCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(BrutalDefaults.Radius),
                        backgroundColor = Color(0xFFFEF3C7)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Admin Access Active", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF78350F))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "You are logged in as Administrator. All content is 100% unlocked. You may test the Razorpay flow or bypass directly.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF92400E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onBack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                                shape = RoundedCornerShape(50.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Admin Bypass (Return to App)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Premium Header Banner
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
                    backgroundColor = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0284C7))
                                )
                            )
                            .padding(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(BorderStroke(2.dp, bb), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Vastavik Premium Pro", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("FastAPI Backend • AI Tutor • Coding Dojo • PYQs", fontSize = 12.sp, color = Color.White.copy(0.85f))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (promoActive) {
                    BrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                                    .border(BorderStroke(1.5.dp, Color(0xFF2563EB)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("50% Special Discount Applied!", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color.Black).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("LIMITED TIME", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Subscription Plan Selection
                BrutalPlanCard(
                    title = "Monthly Pro",
                    price = if (promoActive) "₹149" else "₹299",
                    period = "/month",
                    originalPrice = if (promoActive) "₹299" else null,
                    isSelected = selectedPlan == "monthly",
                    onClick = { selectedPlan = "monthly" }
                )
                Spacer(Modifier.height(10.dp))
                BrutalPlanCard(
                    title = "Yearly Pro",
                    price = if (promoActive) "₹999" else "₹1,999",
                    period = "/year",
                    originalPrice = if (promoActive) "₹1,999" else null,
                    badge = "SAVE 50%",
                    isSelected = selectedPlan == "yearly",
                    onClick = { selectedPlan = "yearly" }
                )

                Spacer(Modifier.height(18.dp))

                // Razorpay Payment Options Container
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Select Payment Method (Razorpay)", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Spacer(Modifier.height(12.dp))

                        // OPTION 1: UPI AutoPay (MAIN & PRIMARY SELECTION)
                        val isAutoPay = selectedMethod == RazorpayPaymentMethod.UPI_AUTOPAY
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAutoPay) Color(0xFF2563EB).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    BorderStroke(if (isAutoPay) 2.dp else 1.5.dp, if (isAutoPay) Color(0xFF2563EB) else bb),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = RazorpayPaymentMethod.UPI_AUTOPAY }
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2563EB)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Autorenew, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("UPI AutoPay", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF10B981))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("PRIMARY • ZERO FEE", fontSize = 8.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            }
                                        }
                                        Text("Mandate Authorization • 1-Tap Setup • Cancel Anytime", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    RadioButton(
                                        selected = isAutoPay,
                                        onClick = { selectedMethod = RazorpayPaymentMethod.UPI_AUTOPAY },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                                    )
                                }

                                AnimatedVisibility(visible = isAutoPay) {
                                    Column(Modifier.padding(top = 10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFEFF6FF))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    "⚡ AutoPay Mandate Details:",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E40AF)
                                                )
                                                Text(
                                                    "• Debit Amount: ₹$currentAmount / ${if (selectedPlan == "monthly") "month" else "year"}\n" +
                                                            "• Mandate VPA: vastavik.autopay@razorpay\n" +
                                                            "• Zero gateway surcharge. Pause or revoke mandate anytime with 1 tap.",
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF1E3A8A)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // OPTION 2: Standard 1-Time UPI
                        val isStandardUpi = selectedMethod == RazorpayPaymentMethod.UPI_STANDARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isStandardUpi) Color(0xFF059669).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    BorderStroke(if (isStandardUpi) 2.dp else 1.5.dp, if (isStandardUpi) Color(0xFF059669) else bb),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = RazorpayPaymentMethod.UPI_STANDARD }
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF059669)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Standard UPI", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("GPay, Paytm, BHIM, CRED or Custom VPA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    RadioButton(
                                        selected = isStandardUpi,
                                        onClick = { selectedMethod = RazorpayPaymentMethod.UPI_STANDARD },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF059669))
                                    )
                                }

                                AnimatedVisibility(visible = isStandardUpi) {
                                    Column(Modifier.padding(top = 10.dp)) {
                                        OutlinedTextField(
                                            value = upiId,
                                            onValueChange = { upiId = it },
                                            label = { Text("Enter UPI ID (e.g. name@okhdfcbank)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // OPTION 3: Credit or Debit Card
                        val isCard = selectedMethod == RazorpayPaymentMethod.CARD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCard) Color(0xFF7C3AED).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    BorderStroke(if (isCard) 2.dp else 1.5.dp, if (isCard) Color(0xFF7C3AED) else bb),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = RazorpayPaymentMethod.CARD }
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF7C3AED)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Credit / Debit Card", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Visa, MasterCard, RuPay, Maestro", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    RadioButton(
                                        selected = isCard,
                                        onClick = { selectedMethod = RazorpayPaymentMethod.CARD },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7C3AED))
                                    )
                                }

                                AnimatedVisibility(visible = isCard) {
                                    Column(Modifier.padding(top = 12.dp)) {
                                        // Card Type Toggle: Credit vs Debit
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(3.dp)
                                        ) {
                                            listOf("Credit", "Debit").forEach { type ->
                                                val isSelectedType = cardType == type
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelectedType) Color(0xFF7C3AED) else Color.Transparent)
                                                        .clickable { cardType = type }
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "$type Card",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelectedType) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = cardNumber,
                                            onValueChange = { if (it.length <= 19) cardNumber = it },
                                            label = { Text("Card Number (16 digits)") },
                                            placeholder = { Text("4123 4567 8901 2345") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = cardHolder,
                                            onValueChange = { cardHolder = it },
                                            label = { Text("Cardholder Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        Row(Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = cardExpiry,
                                                onValueChange = { if (it.length <= 5) cardExpiry = it },
                                                label = { Text("Expiry (MM/YY)") },
                                                placeholder = { Text("12/28") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = cardCvv,
                                                onValueChange = { if (it.length <= 4) cardCvv = it },
                                                label = { Text("CVV") },
                                                placeholder = { Text("123") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                visualTransformation = PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }

                                        Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("256-bit bank grade encryption powered by Razorpay", fontSize = 10.sp, color = Color(0xFF059669))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // OPTION 4: Netbanking
                        val isNetbanking = selectedMethod == RazorpayPaymentMethod.NETBANKING
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isNetbanking) Color(0xFFEA580C).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    BorderStroke(if (isNetbanking) 2.dp else 1.5.dp, if (isNetbanking) Color(0xFFEA580C) else bb),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = RazorpayPaymentMethod.NETBANKING }
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFEA580C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("NetBanking", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Direct Bank Transfer & Mandates", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    RadioButton(
                                        selected = isNetbanking,
                                        onClick = { selectedMethod = RazorpayPaymentMethod.NETBANKING },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEA580C))
                                    )
                                }

                                AnimatedVisibility(visible = isNetbanking) {
                                    Column(Modifier.padding(top = 10.dp)) {
                                        Text("Select Your Bank:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.height(6.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            popularBanks.take(3).forEach { bank ->
                                                val isSelectedBank = selectedBank == bank
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelectedBank) Color(0xFFEA580C) else MaterialTheme.colorScheme.surfaceVariant)
                                                        .clickable { selectedBank = bank }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        bank,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelectedBank) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            popularBanks.drop(3).forEach { bank ->
                                                val isSelectedBank = selectedBank == bank
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelectedBank) Color(0xFFEA580C) else MaterialTheme.colorScheme.surfaceVariant)
                                                        .clickable { selectedBank = bank }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        bank,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelectedBank) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
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

                Spacer(Modifier.height(16.dp))

                // Feature Highlights Card
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("What You Get With Vastavik Pro:", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(10.dp))
                        listOf(
                            "Full access to all ICSE/ISC/CBSE/WB board video lessons",
                            "Mistral AI chat tutor (GOD, Demi-God & Human AI engines)",
                            "Interactive coding dojo with syntax analyzer",
                            "Unlimited board PYQ papers with AI answers & marking schemes",
                            "Official downloadable PDF receipt & tax invoice"
                        ).forEach { feature ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(NeoBrutalistColors.Lime).border(BorderStroke(1.5.dp, bb), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(feature, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Payments are securely processed by Razorpay. UPI AutoPay mandate can be paused or cancelled at any time.",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            // Sticky Bottom Checkout Trigger Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
                    .border(BorderStroke(1.dp, bb.copy(alpha = 0.2f)))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .padding(bottom = 4.dp)
            ) {
                BrutalBoxCard(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                    backgroundColor = Color(0xFF0C2340), // Razorpay Deep Navy
                    onClick = { showPaySheet = true }
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pay ₹$currentAmount with Razorpay • ${selectedMethod.label}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Razorpay Confirmation Bottom Sheet
            if (showPaySheet) {
                BrutalCard(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(14.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0C2340)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Razorpay Secure Checkout", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                    Text("Vastavik Computers EdTech", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showPaySheet = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Amount Breakdown Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Plan:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$planDesc ($planPeriod)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Payment Method:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(selectedMethod.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("50% Festive Discount:", fontSize = 12.sp, color = Color(0xFF059669))
                                    Text("- $discountAmount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }
                                Spacer(Modifier.height(6.dp))
                                HorizontalDivider(color = bb.copy(alpha = 0.2f))
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total Amount (Incl. GST):", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                    Text("₹$currentAmount INR", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0C2340))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Confirm & Pay via Razorpay Button
                        BrutalBoxCard(
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                            backgroundColor = Color(0xFF0C2340),
                            onClick = {
                                if (!isProcessing) {
                                    processRazorpayPayment()
                                }
                            }
                        ) {
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                if (isProcessing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Connecting to Razorpay...", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Authorize & Pay ₹$currentAmount with Razorpay",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { showPaySheet = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            enabled = !isProcessing
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrutalPlanCard(
    title: String,
    price: String,
    period: String,
    originalPrice: String? = null,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bb = brutalBorderColor()
    val selectedBlue = Color(0xFF2563EB)
    BrutalBoxCard(
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(BrutalDefaults.Radius),
        backgroundColor = if (isSelected) selectedBlue else MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape)
                    .background(if (isSelected) Color.White else MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.5.dp, if (isSelected) Color.White else bb), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = selectedBlue, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (originalPrice != null) Text(originalPrice, fontSize = 12.sp, color = if (isSelected) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(end = 6.dp))
                    Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
                Text(period, fontSize = 11.sp, color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(if (isSelected) Color.White else Color.Black).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(badge, fontSize = 10.sp, color = if (isSelected) selectedBlue else Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
