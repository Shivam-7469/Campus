package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.TruStatusBadge
import com.example.ui.components.TruStatusTone
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GateScannerScreen(
    state: TruTicketUiState,
    onToggleOnline: () -> Unit,
    onSimulateScan: (String) -> Unit,
    onOpenManualLookup: () -> Unit,
    onOpenActivityLog: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "scan_line")
    val scanY by infinite.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line_y"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-contrast Gate header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(onClick = {}, onLongClick = onLogoLongPress)
            ) {
                Text("GATE STAFF · DOOR 2", style = MonoSmallStyle, color = TruLime)
                Text(state.gateEventTitle, style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
            }
            Surface(
                onClick = onToggleOnline,
                shape = CircleShape,
                color = if (state.gateIsOnline) TruSuccessMint.copy(alpha = 0.16f) else TruWarningAmber.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, if (state.gateIsOnline) TruSuccessMint else TruWarningAmber),
                modifier = Modifier.heightIn(min = 48.dp).testTag("gate_online_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (state.gateIsOnline) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = null,
                        tint = if (state.gateIsOnline) TruSuccessMint else TruWarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (state.gateIsOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (state.gateIsOnline) TruSuccessMint else TruWarningAmber
                    )
                }
            }
        }

        // Camera-style viewfinder with animated scan line
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0E0E14),
            border = BorderStroke(2.dp, TruLime),
            modifier = Modifier.fillMaxWidth().height(250.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = size.height * scanY
                    drawLine(
                        color = TruLime,
                        start = Offset(20.dp.toPx(), y),
                        end = Offset(size.width - 20.dp.toPx(), y),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Viewfinder", tint = TruLime, modifier = Modifier.size(56.dp))
                    Text("Align Ticket DNA QR inside frame", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                    Text("Double-scan automatically returns ALREADY USED", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                }
            }
        }

        // Manual lookup and log buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onOpenManualLookup,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TruSurfaceElevated, contentColor = TruTextPrimary),
                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("gate_manual_lookup_btn")
            ) {
                Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Manual lookup", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onOpenActivityLog,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TruSurfaceElevated, contentColor = TruTextPrimary),
                modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("gate_activity_log_btn")
            ) {
                Icon(Icons.Filled.History, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Log (${state.gateScanLogs.size})", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
            }
        }

        // Simulate scans section
        Text("Simulate gate scans", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SimulateScanButton("Scan valid VIP (TT-BLR-8841)", "Tap 1st time → VALID · 2nd time → ALREADY USED", TruSuccessMint) {
                onSimulateScan("TT-BLR-8841")
            }
            SimulateScanButton("Scan already used (TT-BLR-4998)", "Returns ALREADY USED with first-scan timestamp", TruWarningAmber) {
                onSimulateScan("TT-BLR-4998")
            }
            SimulateScanButton("Scan expired QR (TT-EXP-9001)", "Returns EXPIRED", TruWarningAmber) {
                onSimulateScan("TT-EXP-9001")
            }
            SimulateScanButton("Scan revoked seat (TT-REV-4410)", "Returns REVOKED", TruDangerCoral) {
                onSimulateScan("TT-REV-4410")
            }
            SimulateScanButton("Scan wrong event (TT-WRG-5520)", "Returns WRONG EVENT", TruDangerCoral) {
                onSimulateScan("TT-WRG-5520")
            }
            SimulateScanButton("Scan fake / invalid QR (TT-FAKE-000)", "Returns INVALID", TruDangerCoral) {
                onSimulateScan("TT-FAKE-000")
            }
        }
    }
}

@Composable
private fun SimulateScanButton(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = TruSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = TruTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
            }
            Icon(Icons.Filled.PlayArrow, null, tint = accent)
        }
    }
}
