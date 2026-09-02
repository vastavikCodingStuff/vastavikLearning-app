package com.vastavik.computer.ui.screens.meeting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.data.model.*
import com.vastavik.computer.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    onNavigate: (String) -> Unit,
    classInfo: ClassSession,
    viewModel: MeetingViewModel,
    userId: String,
    displayName: String
) {
    LaunchedEffect(Unit) { viewModel.joinClass(classInfo.classId, userId, displayName) }
    Scaffold(containerColor = Color(0xFFF5F5F5)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            ClassLobbyCard(classInfo = classInfo, onJoin = { onNavigate("meeting_inclass/${classInfo.classId}") }, onCancel = { onNavigate("profile") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InClassScreen(
    onNavigate: (String) -> Unit,
    viewModel: MeetingViewModel,
    userId: String
) {
    val participantsMap by viewModel.participants.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val whiteboardState by viewModel.whiteboardState.collectAsState()
    val session by viewModel.currentSession.collectAsState()
    val showParticipants by viewModel.showParticipants.collectAsState()
    val showChat by viewModel.showChat.collectAsState()
    val currentTool by viewModel.whiteboardTool.collectAsState()
    val replyTo by viewModel.replyTo.collectAsState()
    val micEnabled by viewModel.localMicEnabled.collectAsState()
    val camEnabled by viewModel.localCameraEnabled.collectAsState()
    val handRaised by viewModel.localHandRaised.collectAsState()
    val screenSharing by viewModel.localScreenSharing.collectAsState()
    var showWhiteboard by remember { mutableStateOf(false) }

    val participants = participantsMap.values.filter { it.isActive }
    val currentUser = viewModel.currentUser
    val isAdmin = currentUser?.role == ParticipantRole.ADMIN
    val hasSharePerm = currentUser?.hasScreenSharePermission == true || isAdmin
    val disabled = session?.disabledFeatures ?: emptySet()
    val recording = session?.recording == true
    val screenSharers = participants.filter { it.isScreenSharing }.take(2)
    var selectedSharerId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(screenSharers.map { it.userId }) {
        if (screenSharers.isNotEmpty() && selectedSharerId !in screenSharers.map { it.userId }) selectedSharerId = screenSharers.first().userId
        if (screenSharers.isEmpty()) selectedSharerId = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8EAF6))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Main area: students grid (default) or whiteboard (toggle)
        if (showWhiteboard) {
            NeoBrutalistWhiteboard(
                modifier = Modifier.fillMaxSize().padding(8.dp).padding(bottom = 80.dp),
                elements = whiteboardState.elements,
                onElementsChange = { viewModel.updateWhiteboard(it) },
                viewport = whiteboardState.viewport,
                onViewportChange = {},
                currentTool = currentTool,
                onToolChange = { viewModel.setWhiteboardTool(it) }
            )
        } else {
            // Students — with Google-Meet style screen-share strip at top where "No Participants" was (max 2 sharers)
            Column(modifier = Modifier.fillMaxSize().padding(10.dp).padding(bottom = 80.dp)) {
                // Screen-share strip — shows at top of participants part, only 2 at a time, choose which to view
                if (screenSharers.isNotEmpty()) {
                    val isVertical = screenSharers.size == 2 // mock: 2 vertical => side by side, otherwise one after other (Column handles both via Row)
                    // Selected enlarged view
                    selectedSharerId?.let { selId ->
                        screenSharers.find { it.userId == selId }?.let { sel ->
                            Box(modifier = Modifier.fillMaxWidth().padding(end = 5.dp, bottom = 5.dp).padding(top = 48.dp)) {
                                Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black))
                                Box(modifier = Modifier.fillMaxWidth().aspectRatio(if (isVertical) 16f/9f else 16f/9f).clip(RoundedCornerShape(16.dp)).background(Color(0xFF0F172A)).border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.ScreenShare, contentDescription = null, tint = Color.White.copy(0.85f), modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.height(6.dp))
                                        Text("${sel.displayName} is sharing", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                        Text("Tap a tile below to switch view", color = Color.White.copy(0.6f), fontSize = 10.sp)
                                    }
                                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF00C853)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                        Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    // Two tiles to choose from — vertical => Row side-by-side, horizontal => Column one after other (we use Row for both, wraps correctly; vertical side-by-side as requested)
                    if (isVertical && screenSharers.size == 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            screenSharers.forEach { sharer ->
                                val selected = sharer.userId == selectedSharerId
                                Box(modifier = Modifier.weight(1f).aspectRatio(9f/16f).padding(end = 4.dp, bottom = 4.dp).clickable { selectedSharerId = sharer.userId }) {
                                    Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
                                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(if (selected) Color(0xFFFFE500) else Color.White).border(androidx.compose.foundation.BorderStroke(2.dp, if (selected) Color.Black else Color(0xFF1A1A1A)), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                            Icon(Icons.Filled.ScreenShare, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text(sharer.displayName, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.Black, maxLines = 1)
                                            Text(if (selected) "Viewing" else "Tap to view", fontSize = 9.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            screenSharers.forEach { sharer ->
                                val selected = sharer.userId == selectedSharerId
                                Box(modifier = Modifier.fillMaxWidth().height(72.dp).padding(end = 4.dp, bottom = 4.dp).clickable { selectedSharerId = sharer.userId }) {
                                    Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
                                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(if (selected) Color(0xFFFFE500) else Color.White).border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp)).padding(10.dp), contentAlignment = Alignment.CenterStart) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ScreenShare, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(sharer.displayName, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.Black)
                                                Text(if (selected) "Viewing • Tap to switch" else "Tap to view full screen", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                            Spacer(Modifier.weight(1f))
                                            if (selected) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (participants.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No participants yet", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 12.dp, top = 4.dp)
                    ) {
                        items(participants, key = { it.userId }) { p ->
                            BrutalStudentTile(participant = p, isMe = p.userId == userId)
                        }
                    }
                }
            }
        }

        if (showParticipants || showChat) {
            Box(modifier = Modifier.fillMaxSize().clickable { viewModel.closePanels() })
        }
        if (showParticipants) {
            Box(modifier = Modifier.fillMaxSize().clickable { viewModel.closePanels() }, contentAlignment = Alignment.CenterEnd) {
                Box(modifier = Modifier.clickable(enabled = false) {}) {
                    ParticipantsPanel(
                        modifier = Modifier.width(320.dp).fillMaxHeight().padding(end = 12.dp, top = 56.dp, bottom = 90.dp),
                        participants = participants,
                        currentUserId = userId,
                        currentUserRole = currentUser?.role ?: ParticipantRole.STUDENT,
                        onKick = { viewModel.kickParticipant(it) },
                        onAssignStarCast = { viewModel.assignStarCast(it) },
                        onRevokeStarCast = { viewModel.revokeStarCast(it) },
                        onGrantScreenShare = { viewModel.grantScreenShare(it) },
                        onRevokeScreenShare = { viewModel.revokeScreenShare(it) }
                    )
                }
            }
        }
        if (showChat) {
            Box(modifier = Modifier.fillMaxSize().clickable { viewModel.closePanels() }, contentAlignment = Alignment.CenterStart) {
                Box(modifier = Modifier.clickable(enabled = false) {}) {
                    MeetingChatPanel(
                        modifier = Modifier.width(320.dp).fillMaxHeight().padding(start = 12.dp, top = 56.dp, bottom = 90.dp),
                        messages = chatMessages,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onReply = { viewModel.onMessageReplyClick(it) },
                        currentUserId = userId,
                        chatEnabled = DisabledFeature.CHAT !in disabled,
                        replyTo = replyTo,
                        onReplyToChange = { viewModel.setReplyTo(it) }
                    )
                }
            }
        }

        // Call controls — centered bottom, brutal box: Whiteboard (left of mute) + Mute / Video / (Screenshare left of Decline) / End
        val effectiveSharePerm = hasSharePerm && (screenSharers.size < 2 || screenSharing)
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
            MeetingControlBar(
                micEnabled = micEnabled, onMicToggle = { viewModel.toggleMic() },
                cameraEnabled = camEnabled, onCameraToggle = { viewModel.toggleCamera() },
                screenShareEnabled = screenSharing, onScreenShareToggle = { viewModel.toggleScreenShare() },
                hasScreenSharePermission = effectiveSharePerm, isAdmin = isAdmin && (screenSharers.size < 2 || screenSharing),
                onLeave = { viewModel.leaveClass(); onNavigate("home") },
                recording = recording, disabledFeatures = disabled,
                whiteboardVisible = showWhiteboard,
                onWhiteboardToggle = { showWhiteboard = !showWhiteboard }
            )
        }

        // Top vertical stack — Participants / Messages / Raise Hand ( squares with 2dp black + shadow )
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SquareTopButton(icon = Icons.Filled.People, label = "${participants.size}", selected = showParticipants, onClick = { viewModel.toggleParticipantsPanel() })
            SquareTopButton(icon = Icons.Filled.ChatBubble, selected = showChat, onClick = { viewModel.toggleChatPanel() })
            SquareTopButton(icon = Icons.Filled.BackHand, selected = handRaised, onClick = { viewModel.toggleHandRaise() })
        }
    }
}

@Composable
private fun BrutalStudentTile(participant: Participant, isMe: Boolean) {
    val bg = when (participant.role) {
        ParticipantRole.ADMIN -> Color(0xFFE0E7FF)
        ParticipantRole.STARCAST -> Color(0xFFFEF3C7)
        else -> Color.White
    }
    Box(modifier = Modifier.aspectRatio(1f).padding(end = 5.dp, bottom = 5.dp)) {
        Box(modifier = Modifier.matchParentSize().offset(x = 5.dp, y = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black))
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(bg)
                .border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Black).border(androidx.compose.foundation.BorderStroke(2.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(participant.displayName.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(participant.displayName, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.Black, textAlign = TextAlign.Center, maxLines = 1)
                Text(
                    when (participant.role) { ParticipantRole.ADMIN -> "Admin"; ParticipantRole.STARCAST -> "★ starCast"; else -> if (isMe) "You" else "Student" },
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B)
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(if (participant.micState == MediaState.ON) Color.Black else Color.White).border(androidx.compose.foundation.BorderStroke(1.2.dp, Color.Black), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                        Icon(if (participant.micState == MediaState.ON) Icons.Filled.Mic else Icons.Filled.MicOff, contentDescription = null, tint = if (participant.micState == MediaState.ON) Color.White else Color.Black, modifier = Modifier.size(10.dp))
                    }
                    Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(if (participant.cameraState == MediaState.ON) Color.Black else Color.White).border(androidx.compose.foundation.BorderStroke(1.2.dp, Color.Black), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                        Icon(if (participant.cameraState == MediaState.ON) Icons.Filled.Videocam else Icons.Filled.VideocamOff, contentDescription = null, tint = if (participant.cameraState == MediaState.ON) Color.White else Color.Black, modifier = Modifier.size(10.dp))
                    }
                    if (participant.handRaised) {
                        Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFFE500)).border(androidx.compose.foundation.BorderStroke(1.2.dp, Color.Black), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.BackHand, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SquareTopButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String? = null, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(52.dp).padding(end = 4.dp, bottom = 4.dp)) {
        Box(modifier = Modifier.matchParentSize().offset(x = 4.dp, y = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black))
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                .background(if (selected) Color.Black else Color.White)
                .border(androidx.compose.foundation.BorderStroke(2.dp, Color.Black), RoundedCornerShape(12.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = if (selected) Color.White else Color.Black, modifier = Modifier.size(20.dp))
                if (label != null) Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color.Black)
            }
        }
    }
}
