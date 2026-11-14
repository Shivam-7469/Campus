package com.example.ui.screens

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.data.TicketHistoryEntry
import com.example.data.TicketItem
import com.example.data.TicketStatus
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@Composable
fun TicketDnaScreen(
    state: TruTicketUiState,
    ticket: TicketItem,
    onBack: () -> Unit,
    onForceRotateQr: () -> Unit,
    onListForResale: () -> Unit,
    onShareVerifier: (String) -> Unit,
    onLogoLongPress: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showPayload by remember { mutableStateOf(false) }
    val qrPayload = """{"ticket_ref":"${ticket.ticketRef}","expires_at":${state.qrExpiresAtEpoch},"nonce":"${state.qrNonce}"}"""
    val ringProgress by animateFloatAsState(
        targetValue = state.qrSecondsLeft / 10f,
        animationSpec = tween(durationMillis = 350),
        label = "qr_countdown_ring"
    )
    val qrPulseScale = remember { Animatable(1f) }
    val haloPulseScale = remember { Animatable(0.92f) }
    val haloPulseAlpha = remember { Animatable(0f) }
    var isFirstNonceRender by remember { mutableStateOf(true) }

    LaunchedEffect(state.qrNonce) {
        if (isFirstNonceRender) {
            isFirstNonceRender = false
            return@LaunchedEffect
        }
        val animScale = runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)
        if (animScale <= 0f) return@LaunchedEffect

        launch {
            qrPulseScale.snapTo(1f)
            qrPulseScale.animateTo(
                targetValue = 1.055f,
                animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
            )
            qrPulseScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
        launch {
            haloPulseScale.snapTo(0.92f)
            haloPulseAlpha.snapTo(0.55f)
            launch {
                haloPulseScale.animateTo(
                    targetValue = 1.14f,
                    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
                )
            }
            haloPulseAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
        }
    }

    val ringColor = if (state.qrSecondsLeft <= 3) TruWarningAmber else TruLime
    val ticketStatusTone = when (ticket.status) {
        TicketStatus.UPCOMING -> TruStatusTone.SUCCESS
        TicketStatus.LISTED -> TruStatusTone.WARNING
        TicketStatus.UNASSIGNED_GROUP -> TruStatusTone.INFO
        TicketStatus.PAST, TicketStatus.REVOKED, TicketStatus.EXPIRED -> TruStatusTone.DANGER
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .navigationBarsPadding()
    ) {
        TruTopBar(
            title = "Ticket DNA",
            canNavigateBack = true,
            onBack = onBack,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glowing Ticket DNA hero stub
            TicketStubSurface(
                cornerRadius = 28.dp,
                borderColor = ringColor.copy(alpha = 0.55f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    TruLime.copy(alpha = 0.12f),
                                    TruViolet.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Top status badges row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TruStatusBadge(text = "VERIFIED", tone = TruStatusTone.SUCCESS, icon = Icons.Filled.Verified)
                                TruStatusBadge(text = ticket.status.label, tone = ticketStatusTone)
                            }
                            Text(ticket.ticketRef, style = MonoMediumStyle, color = TruLime)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = ticket.eventTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TruTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${ticket.dateLabel} · ${ticket.timeLabel} · ${ticket.venue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TruTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Seat & resale cap badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = TruSurfaceElevated, border = BorderStroke(1.dp, TruBorder)) {
                                Text(
                                    text = ticket.seatLabel,
                                    style = MonoSmallStyle,
                                    color = TruTextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            TruStatusBadge(
                                text = "110% cap",
                                tone = TruStatusTone.INFO,
                                icon = Icons.Filled.Shield
                            )
                        }

                        // Dynamic 10-second QR inside circular countdown ring with hash-regeneration pulse
                        Box(
                            modifier = Modifier
                                .size(242.dp)
                                .testTag("dna_qr_wrapper"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (haloPulseAlpha.value > 0.01f) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = haloPulseScale.value
                                            scaleY = haloPulseScale.value
                                        }
                                ) {
                                    drawCircle(
                                        color = TruLime.copy(alpha = haloPulseAlpha.value),
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 5.5.dp.toPx()
                                drawArc(
                                    color = TruSurfaceElevated,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * ringProgress,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    scaleX = qrPulseScale.value
                                    scaleY = qrPulseScale.value
                                }
                            ) {
                                AnimatedContent(
                                    targetState = qrPayload,
                                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                                    label = "qr_nonce_transition"
                                ) { livePayload ->
                                    QrCodeMatrix(payload = livePayload, sizeDp = 186.dp)
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = TruBg,
                                border = BorderStroke(1.dp, ringColor),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                Text(
                                    text = "${state.qrSecondsLeft}s",
                                    style = MonoSmallStyle,
                                    color = ringColor,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                        .testTag("dna_countdown_badge")
                                )
                            }
                        }

                        // Live refresh pill + payload toggle
                        Surface(
                            onClick = onForceRotateQr,
                            shape = CircleShape,
                            color = TruSurfaceElevated,
                            border = BorderStroke(1.dp, TruBorder),
                            modifier = Modifier.heightIn(min = 44.dp).testTag("dna_rotate_now_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Rotate QR now", tint = ringColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Nonce ${state.qrNonce} · Tap to rotate",
                                    style = MonoSmallStyle,
                                    color = TruTextPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TruWarningAmber.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, TruWarningAmber.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = TruWarningAmber, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Screenshots stop working within seconds.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TruWarningAmber
                                )
                            }
                        }
                    }
                }

                PerforatedDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DnaMetric("TIER", ticket.tierName)
                    DnaMetric("ISSUE PRICE", TruTicketFormatters.formatInr(ticket.faceValueInr))
                    DnaMetric("TRANSFERS", ticket.transferCount.toString())
                    DnaMetric("MAX CAP", TruTicketFormatters.formatInr(ticket.maxResaleInr()))
                }
            }

            // Expandable live QR payload inspector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPayload = !showPayload }
                    .testTag("dna_payload_expander")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Code, contentDescription = null, tint = TruViolet, modifier = Modifier.size(16.dp))
                            Text("Signed QR payload", style = MaterialTheme.typography.labelLarge, color = TruTextPrimary)
                        }
                        Icon(
                            imageVector = if (showPayload) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Toggle payload",
                            tint = TruTextMuted
                        )
                    }
                    AnimatedVisibility(visible = showPayload) {
                        Text(
                            text = qrPayload,
                            style = MonoSmallStyle,
                            color = TruLime
                        )
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TruPrimaryButton(
                    text = if (ticket.status == TicketStatus.LISTED) "Manage listing" else "List for resale",
                    onClick = onListForResale,
                    enabled = ticket.status == TicketStatus.UPCOMING || ticket.status == TicketStatus.LISTED,
                    modifier = Modifier.weight(1f),
                    testTag = "dna_list_resale_button",
                    leadingIcon = Icons.Filled.SwapHoriz
                )
                TruSecondaryButton(
                    text = "Share verify link",
                    onClick = {
                        val link = "https://truticket.in/verify/${ticket.ticketRef}"
                        clipboard.setText(AnnotatedString(link))
                        onShareVerifier(ticket.ticketRef)
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "dna_share_verify_button",
                    leadingIcon = Icons.Filled.Share
                )
            }

            // Privacy-safe transaction history timeline
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transaction history", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        TruStatusBadge(text = "Privacy-safe", tone = TruStatusTone.INFO, icon = Icons.Filled.Security)
                    }
                    if (ticket.history.isEmpty()) {
                        Text("No ledger events recorded yet.", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                    } else {
                        ticket.history.forEachIndexed { index, entry ->
                            HistoryTimelineRow(
                                entry = entry,
                                isFirst = index == 0,
                                isLast = index == ticket.history.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTimelineRow(
    entry: TicketHistoryEntry,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) TruLime else TruViolet)
                    .border(1.5.dp, TruBg, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(TruBorder)
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.title, style = MaterialTheme.typography.bodyMedium, color = TruTextPrimary)
                Text(entry.timestamp, style = MonoSmallStyle, color = TruTextMuted)
            }
            Text(entry.detail, style = MonoSmallStyle, color = TruTextMuted)
        }
    }
}

@Composable
private fun DnaMetric(label: String, value: String) {
    Column {
        Text(label, style = MonoSmallStyle, color = TruTextMuted)
        Text(value, style = MonoMediumStyle, color = TruTextPrimary)
    }
}
