package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ScanVerdict
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@Composable
fun GateActivityLogScreen(
    state: TruTicketUiState,
    onBack: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    val validCount = state.gateScanLogs.count { it.verdict == ScanVerdict.VALID }
    val rejectedCount = state.gateScanLogs.size - validCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .navigationBarsPadding()
    ) {
        TruTopBar(
            title = "Gate activity log",
            canNavigateBack = true,
            onBack = onBack,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSuccessMint.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, TruSuccessMint),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(validCount.toString(), style = MonoLargeStyle, color = TruSuccessMint)
                    Text("Valid", style = MaterialTheme.typography.labelMedium, color = TruTextPrimary)
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruDangerCoral.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, TruDangerCoral),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(rejectedCount.toString(), style = MonoLargeStyle, color = TruDangerCoral)
                    Text("Rejected", style = MaterialTheme.typography.labelMedium, color = TruTextPrimary)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.gateScanLogs, key = { it.id }) { log ->
                val tone = when (log.verdict) {
                    ScanVerdict.VALID -> TruStatusTone.SUCCESS
                    ScanVerdict.ALREADY_USED, ScanVerdict.EXPIRED, ScanVerdict.OFFLINE_UNVERIFIED -> TruStatusTone.WARNING
                    else -> TruStatusTone.DANGER
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${log.ticketRef} · ${log.tierName}", style = MonoMediumStyle, color = TruTextPrimary)
                            Text(
                                text = if (log.firstScanTimeLabel != null) "${log.timestampLabel} (1st: ${log.firstScanTimeLabel})" else log.timestampLabel,
                                style = MonoSmallStyle,
                                color = TruTextMuted
                            )
                        }
                        TruStatusBadge(text = log.verdict.title, tone = tone)
                    }
                }
            }
        }
    }
}

@Composable
fun GateManualLookupScreen(
    state: TruTicketUiState,
    onBack: () -> Unit,
    onRedeem: (String) -> Unit,
    onLogoLongPress: () -> Unit
) {
    var query by remember { mutableStateOf("TT-DEL-3910") }
    val matched = state.tickets.firstOrNull { it.ticketRef.equals(query.trim(), ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .navigationBarsPadding()
    ) {
        TruTopBar(
            title = "Manual lookup",
            canNavigateBack = true,
            onBack = onBack,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TruGlassSearchBar(
                value = query,
                onValueChange = { query = it.uppercase() },
                placeholder = "Enter ticket reference (e.g. TT-DEL-3910)",
                testTag = "gate_lookup_input",
                leadingContentDescription = "Search ticket reference"
            )

            if (matched != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruLime),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(matched.ticketRef, style = MonoMediumStyle, color = TruLime)
                        Text("Tier: ${matched.tierName}", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        Text("Status: ${matched.status.label}", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                    }
                }
            } else if (query.isNotBlank()) {
                TruStatusBadge(text = "No matching ticket in manifest", tone = TruStatusTone.DANGER)
            }

            TruPrimaryButton(
                text = "Redeem & verify",
                onClick = { onRedeem(query) },
                enabled = query.isNotBlank(),
                testTag = "gate_manual_redeem_button",
                leadingIcon = Icons.Filled.CheckCircle
            )
        }
    }
}
