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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PaymentMethod
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.CheckoutPhase
import com.example.viewmodel.TruTicketUiState

@Composable
fun CheckoutScreen(
    state: TruTicketUiState,
    totalInr: Int,
    onBack: () -> Unit,
    onSelectMethod: (PaymentMethod) -> Unit,
    onToggleSimulateFailure: (Boolean) -> Unit,
    onPayNow: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    LaunchedEffect(state.checkoutPhase) {
        if (state.checkoutPhase == CheckoutPhase.SUCCESS) {
            onPaymentSuccess()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TruTopBar(
                title = "Checkout",
                canNavigateBack = state.checkoutPhase != CheckoutPhase.PROCESSING,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    val isFailed = state.checkoutPhase == CheckoutPhase.FAILED
                    val isProcessing = state.checkoutPhase == CheckoutPhase.PROCESSING
                    TruPrimaryButton(
                        text = when {
                            isProcessing -> "Authorizing payment…"
                            isFailed -> "Retry ${TruTicketFormatters.formatInr(totalInr)}"
                            else -> "Pay ${TruTicketFormatters.formatInr(totalInr)}"
                        },
                        enabled = !isProcessing,
                        onClick = onPayNow,
                        testTag = "pay_now_button",
                        leadingIcon = if (isFailed) Icons.Filled.Refresh else Icons.Filled.Lock
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TruStatusBadge(text = "Razorpay test mode", tone = TruStatusTone.WARNING, icon = Icons.Filled.Science)
                Text(TruTicketFormatters.formatInr(totalInr), style = MonoLargeStyle, color = TruLime)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(state.selectedEvent.title, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                    Text("${state.selectedQty} × ${state.selectedTier.name} · ${state.selectedEvent.dateLabel}", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                }
            }

            Text("Payment method", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)

            PaymentMethod.entries.forEach { method ->
                val selected = state.paymentMethod == method
                Surface(
                    onClick = { onSelectMethod(method) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) TruSurfaceElevated else TruSurface,
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) TruLime else TruBorder),
                    modifier = Modifier.fillMaxWidth().testTag("pay_method_${method.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(method.label, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                            Text(method.subtitle, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                        }
                        RadioButton(
                            selected = selected,
                            onClick = { onSelectMethod(method) },
                            colors = RadioButtonDefaults.colors(selectedColor = TruLime)
                        )
                    }
                }
            }

            // Prototype test toggle for failure state
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurfaceElevated,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulate gateway failure", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        Text("Test declined bank state & retry recovery", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                    }
                    Switch(
                        checked = state.simulatePaymentFailure,
                        onCheckedChange = onToggleSimulateFailure,
                        modifier = Modifier.testTag("simulate_failure_switch")
                    )
                }
            }

            if (state.checkoutPhase == CheckoutPhase.PROCESSING) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruViolet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TruLime, strokeWidth = 2.5.dp)
                        Text("Verifying payment intent with Razorpay sandbox…", style = MaterialTheme.typography.bodyMedium, color = TruTextPrimary)
                    }
                }
            }

            if (state.checkoutPhase == CheckoutPhase.FAILED && state.checkoutErrorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruDangerCoral.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, TruDangerCoral),
                    modifier = Modifier.fillMaxWidth().testTag("checkout_error_banner")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TruStatusBadge(text = "Payment failed", tone = TruStatusTone.DANGER)
                        Text(state.checkoutErrorMessage, style = MaterialTheme.typography.bodyMedium, color = TruTextPrimary)
                    }
                }
            }
        }
    }
}
