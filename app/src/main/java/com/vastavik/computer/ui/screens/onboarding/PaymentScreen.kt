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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onNavigate: (String) -> Unit) {
    var selectedPlan by remember { mutableStateOf("monthly") }
    var gateway by remember { mutableStateOf("Razorpay") }
    val promoActive = true
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPaySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Plans", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("profile") }) {
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
                                    .background(Color.White).border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(16.dp)),
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
                Text("Payment Gateway", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.Black, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Razorpay", "PhonePe").forEach { g ->
                        val sel = gateway == g
                        BrutalBoxCard(
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = if (sel) NeoBrutalistColors.Yellow else Color.White,
                            onClick = { gateway = g }
                        ) {
                            Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(if (g == "Razorpay") Icons.Filled.CreditCard else Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text(g, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black)
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
                        backgroundColor = Color(0xFFFFF7ED)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(NeoBrutalistColors.Yellow).border(BorderStroke(1.5.dp, Color.Black), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("50% OFF applied!", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.Black)
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

                // Features — brutal white card
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(BrutalDefaults.Radius),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("What you get:", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.Black)
                        Spacer(Modifier.height(10.dp))
                        listOf("Unlimited video lessons", "All coding challenges", "PYQ access", "AI chat assistant", "Priority support").forEach { feature ->
                            Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(NeoBrutalistColors.Lime).border(BorderStroke(1.5.dp, Color.Black), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(feature, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "Monthly UPI AutoPay mandate. Access revoked if not paid after 3-day grace. Cancel anytime.",
                    fontSize = 11.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            // Sticky pay bar — always visible, brutal black-bordered pill button
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.96f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .padding(bottom = 4.dp)
            ) {
                BrutalBoxCard(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                    backgroundColor = Color.Black,
                    onClick = { showPaySheet = true }
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = NeoBrutalistColors.Yellow, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (promoActive) "Pay with $gateway — UPI AutoPay (50% OFF)" else "Pay with $gateway — UPI AutoPay",
                            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (showPaySheet) {
                com.vastavik.computer.ui.theme.BrutalCard(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(BrutalDefaults.RadiusLarge),
                    backgroundColor = Color.White
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Confirm payment", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black, modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White).border(BorderStroke(1.5.dp, Color.Black), CircleShape).clickable { showPaySheet = false }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(NeoBrutalistColors.Yellow).border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(if (gateway == "Razorpay") Icons.Filled.CreditCard else Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(gateway, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black)
                                Text("UPI AutoPay • " + if (selectedPlan == "monthly") "Monthly ₹${if (promoActive) "149" else "299"}" else "Yearly ₹${if (promoActive) "999" else "1,999"}", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        BrutalBoxCard(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(BrutalDefaults.RadiusPill),
                            backgroundColor = NeoBrutalistColors.Yellow,
                            onClick = {
                                showPaySheet = false
                                scope.launch { snackbarHostState.showSnackbar("Starting $gateway UPI AutoPay…") }
                                // createMandate via gateway backend, then webhook; for now record locally and open history
                                onNavigate("payment_history")
                            }
                        ) {
                            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Pay securely with $gateway", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showPaySheet = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold) }
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
    BrutalBoxCard(
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(BrutalDefaults.Radius),
        backgroundColor = if (isSelected) NeoBrutalistColors.Yellow.copy(alpha = 0.35f) else Color.White,
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape)
                    .background(if (isSelected) Color.Black else Color.White)
                    .border(BorderStroke(1.5.dp, Color.Black), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.Black, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (originalPrice != null) Text(originalPrice, fontSize = 12.sp, color = Color(0xFF64748B), textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(end = 6.dp))
                    Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                }
                Text(period, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
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
