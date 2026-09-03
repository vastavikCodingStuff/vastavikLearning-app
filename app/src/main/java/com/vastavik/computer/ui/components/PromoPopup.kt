package com.vastavik.computer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.vastavik.computer.ui.theme.brutalBorderColor
import com.vastavik.computer.ui.theme.brutalShadowColor

data class PromoData(
    val id: String = "promo_50",
    val title: String = "50% OFF Premium!",
    val body: String = "Get full access to all courses, AI chat & papers. Limited time.",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val ctaText: String = "Grab Now",
    val ctaLink: String = "payment"
)

@Composable
fun PromoPopup(
    promo: PromoData,
    onDismiss: () -> Unit,
    onCta: (String) -> Unit
) {
    val bb = brutalBorderColor()
    val bs = brutalShadowColor()
    val PrimaryIndigo = Color(0xFF2563EB)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bs)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, bb), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (promo.imageUrl != null) {
                            AsyncImage(
                                model = promo.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                    .background(PrimaryIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Celebration,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.White
                                )
                            }
                        }
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                promo.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(promo.body, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))

                            Box(modifier = Modifier.padding(end = 5.dp, bottom = 5.dp)) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .offset(x = 5.dp, y = 5.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bs)
                                )
                                Button(
                                    onClick = { onCta(promo.ctaLink) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .border(BorderStroke(2.dp, bb), RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(promo.ctaText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(1.5.dp, bb), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
