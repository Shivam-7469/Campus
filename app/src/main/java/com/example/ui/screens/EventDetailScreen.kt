package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.data.TruEvent
import com.example.data.TruTicketFormatters
import com.example.ui.components.TruPrimaryButton
import com.example.ui.components.TruStatusBadge
import com.example.ui.components.TruStatusTone
import com.example.ui.theme.*

@Composable
fun EventDetailScreen(
    event: TruEvent,
    onBack: () -> Unit,
    onJoinFairQueue: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = TruSurfaceElevated,
                    contentColor = TruTextPrimary
                )
            }
        },
        bottomBar = {
            Surface(
                color = TruSurface,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text("FROM", style = MonoSmallStyle, color = TruTextMuted)
                        Text(
                            text = TruTicketFormatters.formatInr(event.tiers.minOf { it.priceInr }),
                            style = MonoLargeStyle,
                            color = TruLime,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                    TruPrimaryButton(
                        text = "Join FairQueue",
                        onClick = onJoinFairQueue,
                        modifier = Modifier.weight(1f),
                        testTag = "join_fairqueue_button",
                        leadingIcon = Icons.Filled.Bolt
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
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                Image(
                    painter = painterResource(id = event.imageResId),
                    contentDescription = "${event.title} hero banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(TruBg.copy(alpha = 0.25f), TruBg)))
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TruBg.copy(alpha = 0.78f))
                        .testTag("event_detail_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TruTextPrimary)
                }
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.isVerifiedOrganizer) {
                        TruStatusBadge(
                            text = "Verified organizer · ${event.organizer}",
                            tone = TruStatusTone.SUCCESS,
                            icon = Icons.Filled.Verified
                        )
                    }
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TruTextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CalendarToday, null, tint = TruLime, modifier = Modifier.size(16.dp))
                            Text("${event.dateLabel} · ${event.timeLabel}", style = MaterialTheme.typography.bodyLarge, color = TruTextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.LocationOn, null, tint = TruViolet, modifier = Modifier.size(16.dp))
                            Text(event.venue, style = MaterialTheme.typography.bodyMedium, color = TruTextMuted)
                        }
                    }
                }

                Text("Ticket tiers", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)

                event.tiers.forEach { tier ->
                    val ratio = (tier.remaining.toFloat() / tier.total.toFloat()).coerceIn(0f, 1f)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = TruSurface,
                        border = BorderStroke(1.dp, TruBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = tier.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TruTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tier.perks,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TruTextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    text = TruTicketFormatters.formatInr(tier.priceInr),
                                    style = MonoLargeStyle,
                                    color = TruLime,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (tier.isGroupTier) "Min ${tier.minQty} – Max ${tier.maxQty} passes" else "Max ${tier.maxQty} per order",
                                    style = MonoSmallStyle,
                                    color = TruTextMuted
                                )
                                Text(
                                    text = "${tier.remaining}/${tier.total} left",
                                    style = MonoSmallStyle,
                                    color = if (tier.remaining < 40) TruWarningAmber else TruSuccessMint
                                )
                            }
                            LinearProgressIndicator(
                                progress = { 1f - ratio },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                                color = if (tier.remaining < 40) TruWarningAmber else TruLime,
                                trackColor = TruSurfaceElevated
                            )
                        }
                    }
                }

                // Live Lineup Schedule & Venue Guidelines ticket stub card
                EventScheduleProtocolCard(event = event)

                // Venue & Entry Gates map card with tap for directions
                VenueGatesMapCard(
                    event = event,
                    onOpenDirections = {
                        val encoded = Uri.encode(event.venue)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded"))
                        runCatching { context.startActivity(intent) }.onFailure {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Directions: ${event.venue}")
                            }
                        }
                    }
                )

                // Resale cap & group policy summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruSurfaceElevated,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Gavel, null, tint = TruLime, modifier = Modifier.size(18.dp))
                            Text("Resale cap & group policy", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        }
                        Text(event.resalePolicySummary, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                        Text(event.groupPolicySummary, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                    }
                }
            }
        }
    }
}
