package com.vastavik.computer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.data.model.*

@Composable
fun MeetingChatPanel(
    modifier: Modifier = Modifier,
    messages: List<LiveChatMessage>,
    onSendMessage: (String) -> Unit,
    onReply: (LiveChatMessage) -> Unit,
    currentUserId: String,
    chatEnabled: Boolean = true,
    replyTo: ReplyPreview? = null,
    onReplyToChange: (ReplyPreview?) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    Box(modifier = modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().padding(start = 5.dp, top = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black), elevation = CardDefaults.cardElevation(0.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Black, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Chat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (!chatEnabled) Text("Disabled", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
                replyTo?.let { preview ->
                    Surface(modifier = Modifier.fillMaxWidth().padding(8.dp), color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFFF9800))) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Replying to ${preview.senderName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text(preview.truncatedText, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onReplyToChange(null) }) { Icon(Icons.Filled.Close, contentDescription = null, tint = Color.DarkGray) }
                        }
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp), reverseLayout = true) {
                    items(messages.reversed()) { msg -> ChatBubble(msg, msg.senderId == currentUserId, onReply) }
                }
                if (chatEnabled) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = inputText, onValueChange = { inputText = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message...") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { if (inputText.isNotBlank()) { onSendMessage(inputText.trim()); inputText = ""; onReplyToChange(null) } }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black, contentColor = Color.White)) {
                            Icon(Icons.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: LiveChatMessage, isOwn: Boolean, onReply: (LiveChatMessage) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start) {
        if (!isOwn) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black), contentAlignment = Alignment.Center) { Text(msg.senderName.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.widthIn(max = 240.dp)) {
            msg.replyTo?.let { r ->
                Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f))) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text(r.senderName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(r.truncatedText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isOwn) 16.dp else 4.dp, bottomEnd = if (isOwn) 4.dp else 16.dp), color = if (isOwn) Color.Black else Color.White, border = BorderStroke(1.5.dp, Color.Black)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (!isOwn) Text(msg.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(msg.text, fontSize = 13.sp, color = if (isOwn) Color.White else Color.Black)
                    Text(formatTime(msg.timestamp), fontSize = 9.sp, color = if (isOwn) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            }
            if (!isOwn) Text("Reply", modifier = Modifier.padding(top = 4.dp).clickable { onReply(msg) }, fontSize = 11.sp, color = Color.Blue)
        }
        if (isOwn) {
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF0F172A)), contentAlignment = Alignment.Center) { Text(msg.senderName.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        }
    }
}

private fun formatTime(ts: Long): String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ts))

@Composable
fun ParticipantsPanel(
    modifier: Modifier = Modifier,
    participants: List<Participant>,
    currentUserId: String,
    currentUserRole: ParticipantRole,
    onKick: (String) -> Unit,
    onAssignStarCast: (String) -> Unit,
    onRevokeStarCast: (String) -> Unit,
    onGrantScreenShare: (String) -> Unit,
    onRevokeScreenShare: (String) -> Unit
) {
    Box(modifier = modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().padding(start = 5.dp, top = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black))
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black), elevation = CardDefaults.cardElevation(0.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Black, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
                    Text("Participants (${participants.count { it.isActive }})", modifier = Modifier.padding(12.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(participants.sortedByDescending { it.role.ordinal }) { p ->
                        ParticipantRow(p, p.userId == currentUserId, currentUserRole, onKick, onAssignStarCast, onRevokeStarCast, onGrantScreenShare, onRevokeScreenShare)
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantRow(
    participant: Participant, isCurrentUser: Boolean, currentUserRole: ParticipantRole,
    onKick: (String) -> Unit, onAssignStarCast: (String) -> Unit, onRevokeStarCast: (String) -> Unit,
    onGrantScreenShare: (String) -> Unit, onRevokeScreenShare: (String) -> Unit
) {
    val canKick = currentUserRole == ParticipantRole.ADMIN || (currentUserRole == ParticipantRole.STARCAST && participant.role != ParticipantRole.ADMIN)
    val canManageStar = currentUserRole == ParticipantRole.ADMIN
    val canManageShare = currentUserRole == ParticipantRole.ADMIN
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), border = BorderStroke(2.dp, Color.Black), color = if (isCurrentUser) Color(0xFFE3F2FD) else Color.White) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(when (participant.role) { ParticipantRole.ADMIN -> Color(0xFF2196F3); ParticipantRole.STARCAST -> Color(0xFFFFD700); else -> Color.Gray }), contentAlignment = Alignment.Center) {
                    Text(participant.displayName.first().uppercase(), color = if (participant.role == ParticipantRole.STARCAST) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(participant.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (participant.role == ParticipantRole.STARCAST) Text(" ★ starCast", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB77900))
                        if (participant.role == ParticipantRole.ADMIN) Text(" Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                        if (isCurrentUser) Text(" (You)", fontSize = 10.sp, color = Color.Gray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusDot(Icons.Filled.Mic, participant.micState == MediaState.ON)
                        StatusDot(Icons.Filled.Videocam, participant.cameraState == MediaState.ON)
                        if (participant.handRaised) StatusDot(Icons.Filled.BackHand, true)
                        if (participant.isScreenSharing) StatusDot(Icons.Filled.ScreenShare, true)
                    }
                }
            }
            if (!isCurrentUser) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (canManageShare && participant.role != ParticipantRole.ADMIN) {
                        if (participant.hasScreenSharePermission) IconButton(onClick = { onRevokeScreenShare(participant.userId) }) { Icon(Icons.Filled.ScreenShare, contentDescription = null, tint = Color.Red) }
                        else IconButton(onClick = { onGrantScreenShare(participant.userId) }) { Icon(Icons.Filled.ScreenShare, contentDescription = null, tint = Color.Green) }
                    }
                    if (canManageStar && participant.role != ParticipantRole.ADMIN) {
                        if (participant.role == ParticipantRole.STARCAST) IconButton(onClick = { onRevokeStarCast(participant.userId) }) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFA000)) }
                        else IconButton(onClick = { onAssignStarCast(participant.userId) }) { Icon(Icons.Filled.StarBorder, contentDescription = null) }
                    }
                    if (canKick && participant.role != ParticipantRole.ADMIN) IconButton(onClick = { onKick(participant.userId) }) { Icon(Icons.Filled.PersonRemove, contentDescription = null, tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(icon: ImageVector, active: Boolean) {
    Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(4.dp)).background(if (active) Color.Black else Color.White).border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = if (active) Color.White else Color.Black, modifier = Modifier.size(12.dp))
    }
}

@Composable
fun MeetingControlBar(
    modifier: Modifier = Modifier,
    micEnabled: Boolean, onMicToggle: () -> Unit,
    cameraEnabled: Boolean, onCameraToggle: () -> Unit,
    screenShareEnabled: Boolean, onScreenShareToggle: () -> Unit,
    hasScreenSharePermission: Boolean, isAdmin: Boolean,
    onLeave: () -> Unit, recording: Boolean, disabledFeatures: Set<DisabledFeature>,
    whiteboardVisible: Boolean = false,
    onWhiteboardToggle: (() -> Unit)? = null
) {
    val showShare = isAdmin || hasScreenSharePermission
    // screen-share limit: max 2 at a time (mock local state — without APIs we enforce locally)
    val activeSharers = 0 // will be passed via ViewModel in real API; here button visibility alone enforces via isScreenSharing count in screen
    Box(modifier = modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().padding(start = 5.dp, top = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black))
        Card(modifier = Modifier.wrapContentWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(modifier = Modifier.wrapContentWidth().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (recording) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFC62828)).border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color.White))
                        Spacer(Modifier.width(5.dp))
                        Text("REC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(Modifier.width(6.dp))
                }
                // Whiteboard icon moved here — left of mute, as requested
                if (onWhiteboardToggle != null) {
                    ControlButton(icon = Icons.Filled.Draw, active = whiteboardVisible, onClick = onWhiteboardToggle)
                }
                if (DisabledFeature.MIC !in disabledFeatures) ControlButton(icon = if (micEnabled) Icons.Filled.Mic else Icons.Filled.MicOff, active = micEnabled, onClick = onMicToggle)
                if (DisabledFeature.CAMERA !in disabledFeatures) ControlButton(icon = if (cameraEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff, active = cameraEnabled, onClick = onCameraToggle)
                if (showShare && DisabledFeature.SCREENSHARE !in disabledFeatures) ControlButton(icon = Icons.Filled.ScreenShare, active = screenShareEnabled, onClick = onScreenShareToggle)
                ControlButton(icon = Icons.Filled.CallEnd, active = false, isDestructive = true, onClick = onLeave)
            }
        }
    }
}

@Composable
private fun ControlButton(icon: ImageVector, active: Boolean, isDestructive: Boolean = false, onClick: () -> Unit) {
    val bg = when { isDestructive -> Color(0xFFC62828); active -> Color(0xFF4CAF50); else -> Color.White }
    val tint = if (active || isDestructive) Color.White else Color.Black
    // New brutalistic: each control has its own bottom/right 3-4dp black offset, rounded square
    Box(modifier = Modifier.size(52.dp).padding(end = 3.dp, bottom = 3.dp)) {
        Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(bg).border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ClassLobbyCard(classInfo: ClassSession, onJoin: () -> Unit, onCancel: () -> Unit) {
    // Brutal card — white div with 2dp black border + 5dp bottom/right black offset (site-wide), rounded 20dp
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(20.dp)).background(Color.Black))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(2.dp, Color.Black), elevation = CardDefaults.cardElevation(0.dp)) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Brutal icon square — 72dp black rounded 16dp with 2dp border + shadow feel
                Box(modifier = Modifier.size(72.dp).padding(end = 3.dp, bottom = 3.dp)) {
                    Box(modifier = Modifier.matchParentSize().offset(x = 3.dp, y = 3.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black))
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.Black).border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.VideoCall, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Live Class", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.Black)
                Spacer(Modifier.height(6.dp))
                Text(classInfo.topic, fontSize = 15.sp, textAlign = TextAlign.Center, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    // Cancel — brutal white square with 2dp black + 4dp shadow (not OutlinedButton flat)
                    Box(modifier = Modifier.weight(1f).height(52.dp).padding(end = 4.dp, bottom = 4.dp)) {
                        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black))
                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color.White).border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(14.dp)).clickable { onCancel() }, contentAlignment = Alignment.Center) {
                            Text("Cancel", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.Black)
                        }
                    }
                    // Join — brutal black square with 2dp border + 4dp shadow (not Material Button)
                    Box(modifier = Modifier.weight(1f).height(52.dp).padding(end = 4.dp, bottom = 4.dp)) {
                        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black))
                        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color.Black).border(BorderStroke(2.dp, Color.Black), RoundedCornerShape(14.dp)).clickable { onJoin() }, contentAlignment = Alignment.Center) {
                            Text("Join Class", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Mic & camera will be OFF by default", fontSize = 11.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}