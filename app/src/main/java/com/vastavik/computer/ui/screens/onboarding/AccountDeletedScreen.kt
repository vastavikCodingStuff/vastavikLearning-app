package com.vastavik.computer.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

@Composable
fun AccountDeletedScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.DeleteForever,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Account Deleted",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Your account has been permanently deleted.\nAll your data has been removed from our servers.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onNavigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = neoShape(12.dp)
            ) {
                Text("Back to Login")
            }
        }
    }
}
