package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCode2
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
import com.example.data.TicketItem
import com.example.data.TicketStatus
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

private enum class WalletTab(val label: String, val status: TicketStatus) {
    UPCOMING("Upcoming", TicketStatus.UPCOMING),
    PAST("Past", TicketStatus.PAST),
    LISTED("Listed", TicketStatus.LISTED),
    UNASSIGNED_GROUP("Unassigned group", TicketStatus.UNASSIGNED_GROUP)
}

@Composable
fun MyTicketsScreen(
    state: TruTicketUiState,
    onOpenTicketDna: (String) -> Unit,
    onOpenGroupManager: () -> Unit,
    onExploreEvents: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(WalletTab.UPCOMING) }
    val filtered = remember(state.tickets, selectedTab) {
        state.tickets.filter { it.status == selectedTab.status }
    }
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
            contentPadding = PaddingValues(top = topPadding, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WalletTab.entries) { tab ->
                        val active = selectedTab == tab
                        val count = state.tickets.count { it.status == tab.status }
                        TruGlassFilterChip(
                            label = "${tab.label} ($count)",
                            selected = active,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.testTag("wallet_tab_${tab.name}")
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = TruSurface,
                        border = BorderStroke(1.dp, TruBorder),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.ConfirmationNumber, null, tint = TruTextMuted, modifier = Modifier.size(44.dp))
                            Text("No ${selectedTab.label.lowercase()} passes", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                            Text(
                                text = "Verified Ticket DNA passes and group blocks appear here automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TruTextMuted
                            )
                            TruPrimaryButton(text = "Browse drops", onClick = onExploreEvents)
                        }
                    }
                }
            } else {
                items(filtered, key = { it.ticketRef }) { ticket ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        WalletTicketStubCard(
                            ticket = ticket,
                            onClick = {
                                if (ticket.status == TicketStatus.UNASSIGNED_GROUP) onOpenGroupManager()
                                else onOpenTicketDna(ticket.ticketRef)
                            }
                        )
                    }
                }
            }
        }

        TruTopBar(
            title = "My Tickets",
            role = state.userRole,
            onLogoLongPress = onLogoLongPress,
            backdropLayer = topBackdropLayer,
            backdropContainerOffset = scrollContainerOffset,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height },
            trailingAction = {
                TextButton(
                    onClick = onOpenGroupManager,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = TruLime, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Group", color = TruLime, maxLines = 1, softWrap = false)
                }
            }
        )
    }
}

@Composable
private fun WalletTicketStubCard(ticket: TicketItem, onClick: () -> Unit) {
    TicketStubSurface(
        cornerRadius = 28.dp,
        onClick = onClick,
        modifier = Modifier.testTag("ticket_stub_${ticket.ticketRef}")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ticket.ticketRef, style = MonoSmallStyle, color = TruLime)
                val tone = when (ticket.status) {
                    TicketStatus.UPCOMING -> TruStatusTone.SUCCESS
                    TicketStatus.LISTED -> TruStatusTone.WARNING
                    TicketStatus.UNASSIGNED_GROUP -> TruStatusTone.INFO
                    else -> TruStatusTone.DANGER
                }
                TruStatusBadge(text = ticket.status.label, tone = tone)
            }
            Text(ticket.eventTitle, style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
            Text("${ticket.dateLabel} · ${ticket.timeLabel} · ${ticket.city}", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
        }
        PerforatedDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(ticket.tierName.uppercase(), style = MonoSmallStyle, color = TruViolet)
                Text(
                    text = TruTicketFormatters.formatInr(ticket.listedAskInr ?: ticket.faceValueInr),
                    style = MonoMediumStyle,
                    color = TruTextPrimary
                )
            }
            Surface(shape = CircleShape, color = TruSurfaceElevated, border = BorderStroke(1.dp, TruBorder)) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.QrCode2, null, tint = TruLime, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (ticket.status == TicketStatus.UNASSIGNED_GROUP) "Allocate" else "Ticket DNA",
                        style = MaterialTheme.typography.labelLarge,
                        color = TruTextPrimary
                    )
                }
            }
        }
    }
}
