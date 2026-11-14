package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.data.TruTicketFormatters
import com.example.ui.components.TruSecondaryButton
import com.example.ui.components.TruStatusBadge
import com.example.ui.components.TruStatusTone
import com.example.ui.components.TruTopBar
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@Composable
fun FairQueueScreen(
    state: TruTicketUiState,
    onBack: () -> Unit,
    onQueueComplete: () -> Unit,
    onFastForward: () -> Unit,
    onCopyHash: (String) -> Unit,
    onLogoLongPress: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var fairExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.queueCompleted) {
        if (state.queueCompleted) {
            onQueueComplete()
        }
    }

    val progress by animateFloatAsState(
        targetValue = (8 - state.queueSecondsLeft) / 8f,
        label = "queue_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .navigationBarsPadding()
    ) {
        TruTopBar(
            title = "FairQueue waiting room",
            canNavigateBack = true,
            onBack = onBack,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TruStatusBadge(
                text = "Verified human · Eligible",
                tone = TruStatusTone.SUCCESS,
                icon = Icons.Filled.VerifiedUser
            )

            Text(
                text = state.selectedEvent.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TruTextPrimary
            )

            // Progress ring + mono countdown
            Box(
                modifier = Modifier.size(196.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    drawArc(
                        color = TruSurfaceElevated,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = TruLime,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR TURN IN", style = MonoSmallStyle, color = TruTextMuted)
                    Text(
                        text = "00:${state.queueSecondsLeft.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = TruLime
                    )
                    Text("Auto-advances", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                }
            }

            // Seed commitment hash with copy button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SEED COMMITMENT", style = MonoSmallStyle, color = TruTextMuted)
                        Text(
                            text = TruTicketFormatters.shortHash(state.queueSeedHash),
                            style = MonoLargeStyle,
                            color = TruTextPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            val short = TruTicketFormatters.shortHash(state.queueSeedHash)
                            clipboard.setText(AnnotatedString(short))
                            onCopyHash("Copied commitment hash $short")
                        },
                        modifier = Modifier.size(48.dp).testTag("copy_seed_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy commitment hash", tint = TruLime)
                    }
                }
            }

            // "How we keep it fair" expander
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { fairExpanded = !fairExpanded }
                    .testTag("fairqueue_expander")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Shield, null, tint = TruViolet, modifier = Modifier.size(18.dp))
                            Text("How we keep it fair", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        }
                        Icon(
                            imageVector = if (fairExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Toggle details",
                            tint = TruTextMuted
                        )
                    }
                    AnimatedVisibility(visible = fairExpanded) {
                        Text(
                            text = "1. Queue seeds are committed before drop time so organizers cannot reorder fans.\n" +
                                "2. Device attestation limits each verified phone to one active queue slot.\n" +
                                "3. When your timer hits 00:00, your 10-minute seat lock opens automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TruTextMuted
                        )
                    }
                }
            }

            TruSecondaryButton(
                text = "Skip wait (demo)",
                onClick = onFastForward,
                testTag = "fast_forward_queue_button"
            )
        }
    }
}
