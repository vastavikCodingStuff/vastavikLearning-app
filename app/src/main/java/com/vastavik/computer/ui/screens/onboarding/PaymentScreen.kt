package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.BrutalBoxCard
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.NeoBrutalistColors
import com.vastavik.computer.ui.theme.brutalBorderColor
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val bb = brutalBorderColor()
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("monthly") }
    var gateway by remember { mutableStateOf("PhonePe") }
    val promoActive = true
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPaySheet by remember { mutableStateOf(false) }

    val currentAmount = if (selectedPlan == "monthly") (if (promoActive) "149" else "299") else (if (promoActive) "999" else "1999")
    val planDesc = if (selectedPlan == "monthly") "Vastavik Monthly Pro" else "Vastavik Yearly Pro"

    fun initiatePhonePePayment() {
        val upiUri = Uri.parse("upi://pay?pa=vastavik@ybl&pn=Vastavik+Computers&am=$currentAmount&cu=INR&tn=$planDesc")
        val phonePeIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            setPackage("com.phonepe.app")
        }
        try {
            context.startActivity(phonePeIntent)
        } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, upiUri), "Pay with PhonePe or UPI")
            try {
                context.startActivity(chooser)
            } catch (_: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Direct UPI ID: vastavik@ybl copied to clipboard!")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Plans", fontWeight = FontWeight.ExtraBold) },
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
                // Premium header — brutal gradient card
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
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4))
                                )
                            )
                            .padding(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                                    .background(Color.White).border(BorderStroke(2.dp, bb), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp)) }
                            Spacer(Modifier.height(12.dp))
                            Text("Vastavik Premium", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Unlock your full potential", fontSize = 13.sp, color = Color.White.copy(0.85f))
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Gateway toggle — two brutal selectable cards
                Text("Payment Gateway", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Razorpay", "PhonePe").forEach { g ->
                        val sel = gateway == g
                        BrutalBoxCard(
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = if (sel) NeoBrutalistColors.Yellow else MaterialTheme.colorScheme.surface,
                            onClick = { gateway = g }
                        ) {
                            Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(if (g == "Razorpay") Icons.Filled.CreditCard else Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (sel) Color.Black else MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(8.dp))
                                Text(g, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = if (sel) Color.Black else MaterialTheme.colorScheme.onSurface)
                                if (sel) { Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (promoActive) {
                    BrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(NeoBrutalistColors.Yellow).border(BorderStroke(1.5.dp, bb), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("50% OFF applied!", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color.Black).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("Diwali Sale", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Plans
                BrutalPlanCard(
                    title = "Monthly",
                    price = if (promoActive) "₹149" else "₹299",
                    period = "/month",
                    originalPrice = if (promoActive) "₹299" else null,
                    isSelected = selectedPlan == "monthly",
                    onClick = { selectedPlan = "monthly" }
                )
                Spacer(Modifier.height(10.dp))
                BrutalPlanCard(
                    title = "Yearly",
                    price = if (promoActive) "₹999" else "₹1,999",
                    period = "/year",
                    originalPrice = if (promoActive) "₹1,999" else null,
                    badge = if (promoActive) "50% OFF" else "Save 44%",
                    isSelected = selectedPlan == "yearly",
                    onClick = { selectedPlan = "yearly" }
                )

                Spacer(Modifier.height(16.dp))

                Spacer(Modifier.height(16.dp))

                // Payment Gateway Selection — PhonePe Default
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Payment Method", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(10.dp))

                        // PhonePe Option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (gateway == "PhonePe") Color(0xFF5F259F).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    BorderStroke(if (gateway == "PhonePe") 2.dp else 1.5.dp, if (gateway == "PhonePe") Color(0xFF5F259F) else bb),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { gateway = "PhonePe" }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF5F259F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("पे", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("PhonePe UPI", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(Modifier.width(6.dp))
                                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF10B981)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                            Text("RECOMMENDED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                    Text("Instant UPI / AutoPay with 0% gateway fee", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(
                                    selected = gateway == "PhonePe",
                                    onClick = { gateway = "PhonePe" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5F259F))
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Other UPI / Cards Option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (gateway == "UPI") Color(0xFF2563EB).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    BorderStroke(if (gateway == "UPI") 2.dp else 1.5.dp, if (gateway == "UPI") Color(0xFF2563EB) else bb),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { gateway = "UPI" }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Other UPI / Cards", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("GPay, Paytm, NetBanking, Debit/Credit Card", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(
                                    selected = gateway == "UPI",
                                    onClick = { gateway = "UPI" },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Features — brutal white card
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("What you get:", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(10.dp))
                        listOf("Unlimited video lessons", "All coding challenges", "PYQ access", "AI chat assistant", "Priority support").forEach { feature ->
                            Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(NeoBrutalistColors.Lime).border(BorderStroke(1.5.dp, bb), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(feature, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "Monthly UPI AutoPay mandate. Access revoked if not paid after 3-day grace. Cancel anytime.",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            // Sticky pay bar — always visible, PhonePe styled pill button
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .padding(bottom = 4.dp)
            ) {
                BrutalBoxCard(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                    backgroundColor = if (gateway == "PhonePe") Color(0xFF5F259F) else Color.Black,
                    onClick = { showPaySheet = true }
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        if (gateway == "PhonePe") {
                            Text("पे", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        } else {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = NeoBrutalistColors.Yellow, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pay ₹$currentAmount with $gateway — UPI AutoPay",
                            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (showPaySheet) {
                com.vastavik.computer.ui.theme.BrutalCard(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Confirm Payment via $gateway", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).border(BorderStroke(1.5.dp, bb), CircleShape).clickable { showPaySheet = false }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (gateway == "PhonePe") Color(0xFF5F259F) else NeoBrutalistColors.Yellow)
                                    .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (gateway == "PhonePe") {
                                    Text("पे", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                } else {
                                    Icon(Icons.Filled.CreditCard, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(if (gateway == "PhonePe") "PhonePe UPI Gateway" else "UPI / NetBanking", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                Text("Amount: ₹$currentAmount • $planDesc", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        // Pay Button with PhonePe Intent trigger
                        BrutalBoxCard(
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                            backgroundColor = if (gateway == "PhonePe") Color(0xFF5F259F) else NeoBrutalistColors.Yellow,
                            onClick = {
                                showPaySheet = false
                                initiatePhonePePayment()
                                onNavigate("payment_history")
                            }
                        ) {
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (gateway == "PhonePe") Color.White else Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Pay ₹$currentAmount via $gateway",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (gateway == "PhonePe") Color.White else Color.Black
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showPaySheet = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }
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
    BrutalBoxCard(
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(BrutalDefaults.Radius),
        backgroundColor = if (isSelected) NeoBrutalistColors.Yellow else MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape)
                    .background(if (isSelected) Color.Black else MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.5.dp, bb), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (originalPrice != null) Text(originalPrice, fontSize = 12.sp, color = if (isSelected) Color.Black.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(end = 6.dp))
                    Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface)
                }
                Text(period, fontSize = 11.sp, color = if (isSelected) Color.Black.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(Color.Black).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(badge, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
