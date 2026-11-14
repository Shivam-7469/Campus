package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState
import com.example.viewmodel.VerificationOutcome

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VerifierScreen(
    state: TruTicketUiState,
    initialTicketRef: String?,
    onBack: () -> Unit,
    onVerify: (String) -> Unit,
    onReportScam: (String) -> Unit,
    onLogoLongPress: () -> Unit
) {
    var refInput by remember { mutableStateOf(initialTicketRef ?: "TT-BLR-8841") }
    var showReportSheet by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("Seller asked above 110% cap") }

    LaunchedEffect(initialTicketRef) {
        if (!initialTicketRef.isNullOrBlank()) {
            refInput = initialTicketRef
            onVerify(initialTicketRef)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .navigationBarsPadding()
    ) {
        TruTopBar(
            title = "Public verifier",
            canNavigateBack = true,
            onBack = onBack,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Check authenticity before paying any seller. Owner identity is never exposed.",
                style = MaterialTheme.typography.bodyMedium,
                color = TruTextMuted
            )

            OutlinedTextField(
                value = refInput,
                onValueChange = { refInput = it.uppercase() },
                label = { Text("Ticket reference ID") },
                placeholder = { Text("e.g. TT-BLR-8841") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("verifier_input"),
                shape = RoundedCornerShape(16.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TT-BLR-8841", "TT-DEL-3910", "TT-FAKE-999").forEach { sample ->
                    AssistChip(
                        onClick = {
                            refInput = sample
                            onVerify(sample)
                        },
                        label = { Text(sample, style = MonoSmallStyle) }
                    )
                }
            }

            TruPrimaryButton(
                text = "Verify ticket",
                onClick = { onVerify(refInput) },
                testTag = "verify_submit_button",
                leadingIcon = Icons.Filled.VerifiedUser
            )

            when (state.verifierState.outcome) {
                VerificationOutcome.GENUINE -> {
                    val res = state.verifierState
                    TicketStubSurface(cornerRadius = 24.dp, borderColor = TruSuccessMint.copy(alpha = 0.6f)) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TruStatusBadge(text = "Genuine ticket", tone = TruStatusTone.SUCCESS)
                                Text(res.ticketRef, style = MonoSmallStyle, color = TruLime)
                            }
                            Text(res.eventTitle, style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                            Text("No personal owner details shown for privacy.", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                        }
                        PerforatedDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("FACE VALUE", style = MonoSmallStyle, color = TruTextMuted)
                                Text(TruTicketFormatters.formatInr(res.faceValueInr), style = MonoMediumStyle, color = TruLime)
                            }
                            Column {
                                Text("TRANSFERS", style = MonoSmallStyle, color = TruTextMuted)
                                Text("${res.transferCount}", style = MonoMediumStyle, color = TruTextPrimary)
                            }
                            Column {
                                Text("VALIDITY", style = MonoSmallStyle, color = TruTextMuted)
                                Text(res.statusLabel, style = MonoMediumStyle, color = TruSuccessMint)
                            }
                        }
                    }
                }
                VerificationOutcome.NOT_FOUND -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TruDangerCoral.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, TruDangerCoral),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TruStatusBadge(text = "Unrecognized reference", tone = TruStatusTone.DANGER)
                            Text(
                                "No genuine Ticket DNA matches ${state.verifierState.ticketRef}. Do not send money off-platform.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TruTextPrimary
                            )
                        }
                    }
                }
                VerificationOutcome.IDLE -> Unit
            }

            TruSecondaryButton(
                text = "Report a scam",
                onClick = { showReportSheet = true },
                testTag = "open_report_scam_button",
                leadingIcon = Icons.Filled.Flag
            )
        }
    }

    if (showReportSheet) {
        val reasons = listOf(
            "Seller asked above 110% cap",
            "Seller shared a static QR screenshot",
            "Demanded UPI transfer outside TruResale",
            "Duplicate ticket reference suspected"
        )
        ModalBottomSheet(
            onDismissRequest = { showReportSheet = false },
            containerColor = TruSurface,
            contentColor = TruTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Report a scam listing", style = MaterialTheme.typography.headlineMedium)
                Text("Help TruTicket freeze fraudulent references immediately.", style = MaterialTheme.typography.bodyMedium, color = TruTextMuted)
                reasons.forEach { reason ->
                    val selected = selectedReason == reason
                    Surface(
                        onClick = { selectedReason = reason },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) TruDangerCoral.copy(alpha = 0.14f) else TruSurfaceElevated,
                        border = BorderStroke(1.dp, if (selected) TruDangerCoral else TruBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(reason, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            RadioButton(selected = selected, onClick = { selectedReason = reason })
                        }
                    }
                }
                TruPrimaryButton(
                    text = "Submit report",
                    onClick = {
                        onReportScam(selectedReason)
                        showReportSheet = false
                    },
                    testTag = "submit_scam_report_button"
                )
            }
        }
    }
}
