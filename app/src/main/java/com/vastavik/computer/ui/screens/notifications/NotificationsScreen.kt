package com.vastavik.computer.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.ui.theme.VastavikColors
import com.vastavik.computer.ui.theme.neoShape
import com.vastavik.computer.ui.theme.neoCircleShape

data class AppNotification(val id:String, val title:String, val body:String, val time:String, val unread:Boolean=true, val type:String="general")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigate:(String)->Unit){
    var items by remember { mutableStateOf(listOf(
        AppNotification("1","New Lesson: OOP in Java","VS Code + Whiteboard videos are live","2h ago",true,"new_lesson"),
        AppNotification("2","50% OFF ends soon","Premium at Rs 149/mo. Tap to grab.","5h ago",true,"promo"),
        AppNotification("3","Practice Reminder","You haven't practiced today. 3 MCQs waiting.","1d ago",false,"reminder"),
        AppNotification("4","App Update Available","v1.1.0 with OCR exercise is here.","2d ago",true,"update"),
        AppNotification("5","Payment due in 3 days","Renew to keep Pro access.","3d ago",true,"expiry")
    ))}
    Scaffold(
        topBar = { TopAppBar(title={Text("Notifications", fontWeight=FontWeight.Bold)}, navigationIcon={IconButton(onClick={onNavigate("home")}){Icon(Icons.Filled.ArrowBack,contentDescription=null)}} , actions={ TextButton(onClick={ items = items.map{ it.copy(unread=false)}}){Text("Mark all read")}})},
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (items.isEmpty()){
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment=Alignment.Center){ Text("No notifications", color=MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
                items(items){ n ->
                    Card(
                        onClick = { items = items.map{ if(it.id==n.id) it.copy(unread=false) else it }; when(n.type){ "promo"-> onNavigate("payment"); "update"-> onNavigate("app_update"); "new_lesson"-> onNavigate("video_lesson/1/1/1/1"); else->{}} },
                        shape=neoShape(16.dp),
                        colors=CardDefaults.cardColors(containerColor = if(n.unread) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                        border = if(n.unread) CardDefaults.outlinedCardBorder().copy(width=1.dp) else null
                    ){
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment=Alignment.Top){
                            Box(Modifier.size(40.dp).clip(neoCircleShape()).background(if(n.unread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline), contentAlignment=Alignment.Center){ Icon(Icons.Filled.Notifications, contentDescription=null, tint=Color.White, modifier=Modifier.size(20.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)){
                                Row(verticalAlignment=Alignment.CenterVertically){
                                    Text(n.title, fontWeight=FontWeight.Bold, fontSize=14.sp, modifier=Modifier.weight(1f), color=MaterialTheme.colorScheme.onSurface)
                                    if(n.unread) Box(Modifier.size(8.dp).clip(neoCircleShape()).background(MaterialTheme.colorScheme.primary))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(n.body, fontSize=13.sp, color=MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(n.time, fontSize=11.sp, color=MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}
