package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TicketTier
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@Composable
fun TicketSelectionScreen(
    state: TruTicketUiState,
    onBack: () -> Unit,
    onSelectTier: (TicketTier) -> Unit,
    onAdjustQty: (Int) -> Unit,
    onProceedToCheckout: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    val event = state.selectedEvent
    val tier = state.selectedTier
    val base = tier.priceInr * state.selectedQty
    val protocolFee = (base * 3) / 100
    val taxes = (protocolFee * 18) / 100
    val total = base + protocolFee + taxes

    val mins = (state.reservationSecondsLeft / 60).toString().padStart(2, '0')
    val secs = (state.reservationSecondsLeft % 60).toString().padStart(2, '0')

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TruTopBar(
                title = "Select tickets",
                canNavigateBack = true,
                onBack = onBack,
                role = state.userRole,
                onLogoLongPress = onLogoLongPress
            )
        },
        bottomBar = {
            Surface(
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${state.selectedQty} × ${tier.name}", style = MaterialTheme.typography.titleMedium, color = TruTextMuted)
                        Text(TruTicketFormatters.formatInr(total), style = MonoLargeStyle, color = TruLime)
                    }
                    TruPrimaryButton(
                        text = "Continue to checkout",
                        onClick = onProceedToCheckout,
                        testTag = "continue_to_checkout_button"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 10-minute reservation timer
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruWarningAmber.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, TruWarningAmber.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.LockClock, contentDescription = null, tint = TruWarningAmber, modifier = Modifier.size(18.dp))
                        Text("Seats reserved for", style = MaterialTheme.typography.labelLarge, color = TruWarningAmber)
                    }
                    Text("$mins:$secs", style = MonoLargeStyle, color = TruWarningAmber)
                }
            }

            Text(event.title, style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)

            event.tiers.forEach { item ->
                val selected = item.id == tier.id
                Surface(
                    onClick = { onSelectTier(item) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) TruSurfaceElevated else TruSurface,
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) TruLime else TruBorder),
                    modifier = Modifier.fillMaxWidth().testTag("select_tier_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                                if (item.isGroupTier) {
                                    TruStatusBadge(text = "10–50 block", tone = TruStatusTone.INFO)
                                }
                            }
                            Text(item.perks, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(TruTicketFormatters.formatInr(item.priceInr), style = MonoMediumStyle, color = TruLime)
                    }
                }
            }

            // Quantity stepper card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Quantity", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        Text(
                            text = if (tier.isGroupTier) "Corporate group: ${tier.minQty} to ${tier.maxQty}" else "Retail limit: max ${tier.maxQty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TruTextMuted
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { onAdjustQty(if (tier.isGroupTier) -5 else -1) },
                            enabled = state.selectedQty > tier.minQty,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(TruSurfaceElevated)
                                .testTag("qty_minus")
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity", tint = TruTextPrimary)
                        }
                        Text(
                            text = state.selectedQty.toString(),
                            style = MonoLargeStyle,
                            color = TruLime,
                            modifier = Modifier.testTag("qty_value")
                        )
                        IconButton(
                            onClick = { onAdjustQty(if (tier.isGroupTier) 5 else 1) },
                            enabled = state.selectedQty < tier.maxQty,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(TruSurfaceElevated)
                                .testTag("qty_plus")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase quantity", tint = TruTextPrimary)
                        }
                    }
                }
            }

            // Live price breakdown stub
            TicketStubSurface(cornerRadius = 20.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Price breakdown", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                    PriceRow("${state.selectedQty} × ${tier.name}", TruTicketFormatters.formatInr(base))
                    PriceRow("3% TruTicket protocol fee", TruTicketFormatters.formatInr(protocolFee))
                    PriceRow("GST & statutory taxes (18% on fee)", TruTicketFormatters.formatInr(taxes))
                }
                PerforatedDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TruTextPrimary)
                    Text(TruTicketFormatters.formatInr(total), style = MonoLargeStyle, color = TruLime)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TruTextMuted)
        Text(value, style = MonoMediumStyle, color = TruTextPrimary)
    }
}
