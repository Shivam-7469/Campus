package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.TruTicketFormatters
import com.example.data.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@Composable
fun ProfileScreen(
    state: TruTicketUiState,
    onSwitchRole: (UserRole) -> Unit,
    onSignOutSession: (String) -> Unit,
    onToggleTwoStep: (Boolean) -> Unit,
    onToggleDropNotif: (Boolean) -> Unit,
    onToggleQueueNotif: (Boolean) -> Unit,
    onToggleResaleNotif: (Boolean) -> Unit,
    onClaimWaitlist: (String) -> Unit,
    onOpenVerifier: () -> Unit,
    onOpenGroupManager: () -> Unit,
    onLogout: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val topPadding = with(LocalDensity.current) { topBarHeightPx.toDp() } + 12.dp
    val topBackdropLayer = rememberGraphicsLayer()
    var scrollContainerOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { scrollContainerOffset = it.positionInRoot() }
                .drawWithContent {
                    topBackdropLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(topBackdropLayer)
                },
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity & role switcher
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aarav Sharma", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                                Text("+91 98450 21834 · Bengaluru", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                            }
                            Spacer(Modifier.width(8.dp))
                            TruStatusBadge(text = "KYC Verified", tone = TruStatusTone.SUCCESS)
                        }
                        Text(
                            text = "Active prototype role",
                            style = MaterialTheme.typography.labelLarge,
                            color = TruTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(UserRole.entries) { role ->
                                val active = state.userRole == role
                                TruGlassFilterChip(
                                    label = role.label,
                                    selected = active,
                                    onClick = { onSwitchRole(role) },
                                    modifier = Modifier.testTag("profile_role_${role.name}")
                                )
                            }
                        }
                    }
                }
            }

            // Waitlist claims with expiry timers
            item {
                Text("Waitlist claims", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
            }
            if (state.waitlistClaims.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = TruSurface, border = BorderStroke(1.dp, TruBorder), modifier = Modifier.fillMaxWidth()) {
                        Text("No active waitlist claims.", style = MaterialTheme.typography.bodyMedium, color = TruTextMuted, modifier = Modifier.padding(16.dp))
                    }
                }
            } else {
                items(state.waitlistClaims.size) { idx ->
                    val claim = state.waitlistClaims[idx]
                    val mins = (claim.secondsRemaining / 60).toString().padStart(2, '0')
                    val secs = (claim.secondsRemaining % 60).toString().padStart(2, '0')
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TruSurface,
                        border = BorderStroke(1.dp, TruWarningAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TruStatusBadge(text = "Claim window", tone = TruStatusTone.WARNING, icon = Icons.Filled.Timer)
                                Text("$mins:$secs", style = MonoLargeStyle, color = TruWarningAmber)
                            }
                            Text(claim.eventTitle, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                            Text("${claim.tierName} · ${TruTicketFormatters.formatInr(claim.priceInr)}", style = MonoSmallStyle, color = TruLime)
                            TruPrimaryButton(text = "Claim pass now", onClick = { onClaimWaitlist(claim.eventId) })
                        }
                    }
                }
            }

            // Security & notifications
            item {
                Text("Security & alerts", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ToggleRow("2-step verification", "Require OTP + device key for transfers", state.twoStepEnabled, onToggleTwoStep)
                        HorizontalDivider(color = TruBorder)
                        ToggleRow("Drop reminders", "15-min alert before FairQueue opens", state.notifDropAlerts, onToggleDropNotif)
                        HorizontalDivider(color = TruBorder)
                        ToggleRow("Queue turn alerts", "Haptic + push when your turn unlocks", state.notifQueueAlerts, onToggleQueueNotif)
                        HorizontalDivider(color = TruBorder)
                        ToggleRow("Capped resale alerts", "Notify when wishlist tiers list", state.notifResaleAlerts, onToggleResaleNotif)
                    }
                }
            }

            // Active sessions
            item {
                Text("Active sessions", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.sessions.forEach { ses ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = TruSurface,
                            border = BorderStroke(1.dp, TruBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ses.deviceName, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                                    Text("${ses.city} · ${ses.lastActive}", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                                }
                                if (ses.isCurrent) {
                                    TruStatusBadge(text = "This device", tone = TruStatusTone.SUCCESS)
                                } else {
                                    TextButton(onClick = { onSignOutSession(ses.id) }) {
                                        Text("Sign out", color = TruDangerCoral)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Shortcuts & Privacy & Logout
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TruSecondaryButton(text = "Public ticket verifier", onClick = onOpenVerifier, leadingIcon = Icons.Filled.VerifiedUser)
                    TruSecondaryButton(text = "Corporate group manager", onClick = onOpenGroupManager, leadingIcon = Icons.Filled.Groups)
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, TruDangerCoral),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("profile_logout_button")
                    ) {
                        Icon(Icons.Filled.Logout, null, tint = TruDangerCoral)
                        Spacer(Modifier.width(8.dp))
                        Text("Log out", color = TruDangerCoral, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        TruTopBar(
            title = "Profile & security",
            role = state.userRole,
            onLogoLongPress = onLogoLongPress,
            backdropLayer = topBackdropLayer,
            backdropContainerOffset = scrollContainerOffset,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height }
        )
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
        }
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
