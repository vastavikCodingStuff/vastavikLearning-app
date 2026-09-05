package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.ui.theme.BrutalBoxCard
import com.vastavik.computer.ui.theme.BrutalCard
import com.vastavik.computer.ui.theme.BrutalDefaults
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.utils.PaymentReceiptData
import com.vastavik.computer.utils.PaymentReceiptPdf
import kotlinx.coroutines.launch

data class PaymentHistoryItem(
    val id: String,
    val plan: String,
    val period: String,
    val amount: String,
    val baseAmount: String,
    val discount: String,
    val gst: String,
    val method: String,
    val date: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(onNavigate: (String) -> Unit, onBack: () -> Unit = {}) {
    val bb = brutalBorderColor()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val transactions = remember {
        listOf(
            PaymentHistoryItem(
                id = "pay_rzp_9842109842",
                plan = "Vastavik Monthly Pro",
                period = "30 Days",
                amount = "149",
                baseAmount = "₹299",
                discount = "₹150",
                gst = "₹22.73",
                method = "UPI AutoPay",
                date = "Active • Renews Next Month",
                status = "Completed"
            ),
            PaymentHistoryItem(
                id = "pay_rzp_1102948271",
                plan = "Vastavik Yearly Pro",
                period = "365 Days",
                amount = "999",
                baseAmount = "₹1,999",
                discount = "₹1,000",
                gst = "₹152.39",
                method = "Credit Card",
                date = "Completed",
                status = "Completed"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History & Invoices", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No transactions yet",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { item ->
                    BrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(BrutalDefaults.Radius),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0C2340)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.plan, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                                    Text("Razorpay • ${item.method}", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                                    Text(item.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${item.amount}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PAID", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = bb.copy(alpha = 0.2f))
                            Spacer(Modifier.height(10.dp))

                            // Action: Download / View PDF Invoice
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ID: ${item.id}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                OutlinedButton(
                                    onClick = {
                                        val user = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
                                        val data = PaymentReceiptData(
                                            invoiceNumber = "INV-2026-RZP-" + item.id.takeLast(6),
                                            transactionId = item.id,
                                            orderId = "order_rzp_" + item.id.takeLast(8),
                                            planName = item.plan,
                                            planPeriod = item.period,
                                            amount = item.amount,
                                            baseAmount = item.baseAmount,
                                            discountAmount = item.discount,
                                            gstAmount = item.gst,
                                            paymentMethod = item.method,
                                            gateway = "Razorpay",
                                            customerName = user?.displayName ?: "Vastavik Student",
                                            customerEmail = user?.email ?: "student@vastaviklearning.com"
                                        )
                                        val file = PaymentReceiptPdf.generateReceipt(context, data)
                                        if (file != null) {
                                            PaymentReceiptPdf.openPdf(context, file)
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to generate PDF")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2563EB))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Download PDF", fontSize = 11.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
