package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState
import kotlinx.coroutines.delay

@Composable
fun OrderConfirmationScreen(
    state: TruTicketUiState,
    onViewInWallet: () -> Unit,
    onAllocateToTeam: () -> Unit
) {
    var burst by remember { mutableStateOf(false) }
    var mintStage by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        burst = true
        delay(450L)
        mintStage = 2
        delay(550L)
        mintStage = 3
    }

    val scale by animateFloatAsState(
        targetValue = if (burst) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "confirm_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(TruSuccessMint.copy(alpha = 0.16f))
                .border(2.dp, TruSuccessMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Order confirmed", tint = TruSuccessMint, modifier = Modifier.size(44.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TruStatusBadge(text = "Issued & secured", tone = TruStatusTone.SUCCESS)
            Text("Passes issued!", style = MaterialTheme.typography.headlineLarge, color = TruTextPrimary)
            Text("Order ${state.lastOrderRef}", style = MonoMediumStyle, color = TruLime)
        }

        TicketStubSurface(cornerRadius = 28.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.selectedEvent.title, style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                Text("${state.selectedQty} × ${state.selectedTier.name} · ${TruTicketFormatters.formatInr(state.lastOrderTotalInr)}", style = MonoMediumStyle, color = TruTextMuted)
            }
            PerforatedDivider()
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Issuance timeline", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                TimelineStep("Payment confirmed", "Settled via ${state.paymentMethod.label}", mintStage >= 1)
                TimelineStep("Minting Ticket DNA", "Generating rotating QR secret", mintStage >= 2)
                TimelineStep("Ticket issued", "Locked to your TruTicket vault", mintStage >= 3)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            TruPrimaryButton(
                text = "View in wallet",
                onClick = onViewInWallet,
                testTag = "view_in_wallet_button",
                leadingIcon = Icons.Filled.ConfirmationNumber
            )
            if (state.lastOrderIsGroup) {
                TruSecondaryButton(
                    text = "Allocate to team",
                    onClick = onAllocateToTeam,
                    testTag = "allocate_to_team_button",
                    leadingIcon = Icons.Filled.Groups
                )
            }
        }
        }

        CelebratoryConfettiOverlay(
            triggerKey = state.lastOrderRef
        )
    }
}

@Composable
private fun TimelineStep(title: String, subtitle: String, completed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = title,
            tint = if (completed) TruSuccessMint else TruBorder,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (completed) TruTextPrimary else TruTextMuted)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
        }
    }
}
