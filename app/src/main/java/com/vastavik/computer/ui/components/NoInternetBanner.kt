package com.vastavik.computer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.utils.NetworkStatusObserver
import kotlinx.coroutines.launch

/**
 * Custom Cloud icon with a bold centered question mark '?' inside the cloud body.
 */
@Composable
fun CloudQuestionMarkIcon(
    modifier: Modifier = Modifier,
    cloudColor: Color = Color.Black,
    questionColor: Color = Color.White
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = "Cloud with question mark",
            tint = cloudColor,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "?",
            color = questionColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-1).dp)
        )
    }
}

/**
 * NeoBrutalistic offline banner displayed at the top of the app when device is disconnected from internet.
 * Features:
 * - Thick black border on the right and bottom (NeoBrutalist solid black offset shadow + 2.5dp border)
 * - Rounded vertices (14.dp shape)
 * - Cloud icon with '?' inside
 * - Informative warning copy stating the app cannot work without internet
 */
@Composable
fun NoInternetBannerHost(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val observer = remember { NetworkStatusObserver(context) }
    val isConnected by observer.isConnected.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = !isConnected,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // NeoBrutalistic Solid Black Shadow on the right and bottom
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 5.dp, y = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
            )

            // NeoBrutalistic Main Warning Card with rounded vertices and thick black border
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD600) // High-contrast NeoBrutalist Yellow
                ),
                border = BorderStroke(2.5.dp, Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // NeoBrutalistic Cloud with '?' Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CloudQuestionMarkIcon(
                            modifier = Modifier.size(32.dp),
                            cloudColor = Color(0xFF1E293B),
                            questionColor = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Banner Text
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "NO INTERNET CONNECTION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "App cannot work without internet. Please connect to Wi-Fi or mobile data.",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF262626)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Retry button with NeoBrutalistic outline
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .clickable {
                                coroutineScope.launch {
                                    observer.checkCurrentConnectivity()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Retry",
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RETRY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD600)
                            )
                        }
                    }
                }
            }
        }
    }
}
