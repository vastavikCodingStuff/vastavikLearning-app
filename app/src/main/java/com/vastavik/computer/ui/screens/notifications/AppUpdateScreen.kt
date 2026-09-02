package com.vastavik.computer.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(onNavigate:(String)->Unit){
    val current = "1.0.0"
    val latest = "1.1.0"
    val isUpdateAvailable = latest != current
    val force = false
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = { TopAppBar(title={Text("App Update", fontWeight=FontWeight.Bold)}, navigationIcon={IconButton(onClick={onNavigate("home")}){Icon(Icons.Filled.ArrowBack,contentDescription=null)}})},
        containerColor = MaterialTheme.colorScheme.background
    ){ padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally){
            Icon(Icons.Filled.SystemUpdate, contentDescription=null, modifier=Modifier.size(72.dp), tint=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Vastavik Computers", fontWeight=FontWeight.Bold, fontSize=20.sp)
            Text("Current: v", color=MaterialTheme.colorScheme.onSurfaceVariant, fontSize=13.sp)
            Spacer(Modifier.height(24.dp))
            if(isUpdateAvailable){
                Card(shape=neoShape(16.dp), colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)){
                    Column(Modifier.padding(16.dp)){
                        Text("Update available: v", fontWeight=FontWeight.Bold, color=MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("What's new:", fontWeight=FontWeight.W600, fontSize=13.sp)
                        Text("- OCR coding exercise\n- 50% promo banner\n- Full-screen code editor with line numbers\n- Bug fixes", fontSize=13.sp, color=MaterialTheme.colorScheme.onSurfaceVariant, modifier=Modifier.padding(top=4.dp))
                        if(force) { Spacer(Modifier.height(8.dp)); Text("Force update required to continue.", color=MaterialTheme.colorScheme.error, fontWeight=FontWeight.Bold, fontSize=12.sp) }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick={ try{ uriHandler.openUri("https://play.google.com/store/apps/details?id=com.vastavik.computer")} catch(_:Exception){}}, modifier=Modifier.fillMaxWidth().height(48.dp)){ Text("Update Now", fontWeight=FontWeight.Bold) }
                OutlinedButton(onClick={onNavigate("home")}, enabled=!force, modifier=Modifier.fillMaxWidth().padding(top=8.dp)){ Text("Later") }
            } else {
                Spacer(Modifier.height(12.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription=null, tint=MaterialTheme.colorScheme.tertiary, modifier=Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("You're up to date!", color=MaterialTheme.colorScheme.tertiary, fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick={onNavigate("home")}, modifier=Modifier.fillMaxWidth()){ Text("Back to Home") }
            }
        }
    }
}
