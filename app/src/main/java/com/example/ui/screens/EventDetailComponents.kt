package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TruEvent
import com.example.ui.components.PerforatedDivider
import com.example.ui.components.TicketStubSurface
import com.example.ui.theme.*

@Composable
fun EventScheduleProtocolCard(event: TruEvent) {
    TicketStubSurface(
        cornerRadius = 24.dp,
        modifier = Modifier.testTag("event_schedule_ticket_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = TruViolet, modifier = Modifier.size(18.dp))
                Text(event.scheduleTitle, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
            }
            Text(event.scheduleSubtitle, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                event.scheduleSlots.forEach { slot ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TruSurfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (slot.isHighlight) TruViolet.copy(alpha = 0.55f) else TruBorder.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = slot.time,
                                    style = MonoSmallStyle,
                                    color = if (slot.isHighlight) TruViolet else TruSuccessMint,
                                    softWrap = false
                                )
                                Text(
                                    text = slot.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (slot.isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                                    color = TruTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            if (slot.isHighlight) {
                                Surface(
                                    shape = CircleShape,
                                    color = TruViolet.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = slot.stageTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TruTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        softWrap = false
                                    )
                                }
                            } else {
                                Text(
                                    text = slot.stageTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TruTextMuted,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        PerforatedDivider()

        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = TruSuccessMint, modifier = Modifier.size(18.dp))
                Text("Venue Guidelines & Protocol", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
            }
            event.venueGuidelines.forEach { guideline ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircleOutline,
                        contentDescription = null,
                        tint = TruSuccessMint,
                        modifier = Modifier.padding(top = 2.dp).size(15.dp)
                    )
                    Text(
                        text = guideline,
                        style = MaterialTheme.typography.bodySmall,
                        color = TruTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun VenueGatesMapCard(
    event: TruEvent,
    onOpenDirections: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = TruSurface,
        border = BorderStroke(1.dp, TruBorder),
        modifier = Modifier.fillMaxWidth().testTag("venue_gates_map_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Venue & Entry Gates", style = MaterialTheme.typography.titleSmall, color = TruTextPrimary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = event.venueLocality.ifEmpty { event.city.label },
                    style = MaterialTheme.typography.labelSmall,
                    color = TruSuccessMint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenDirections)
                    .testTag("venue_map_directions_button")
            ) {
                // Stylized dark-mode cartographic vector map preview
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xFF141B22))

                    // Green park / campus blocks
                    drawRoundRect(
                        color = Color(0xFF172B22),
                        topLeft = Offset(size.width * 0.06f, size.height * 0.14f),
                        size = Size(size.width * 0.24f, size.height * 0.34f),
                        cornerRadius = CornerRadius(18f, 18f)
                    )
                    drawRoundRect(
                        color = Color(0xFF172B22),
                        topLeft = Offset(size.width * 0.68f, size.height * 0.56f),
                        size = Size(size.width * 0.25f, size.height * 0.30f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                    // Water body accent
                    drawRoundRect(
                        color = Color(0xFF162A3E),
                        topLeft = Offset(size.width * 0.64f, size.height * 0.08f),
                        size = Size(size.width * 0.30f, size.height * 0.22f),
                        cornerRadius = CornerRadius(22f, 22f)
                    )

                    // Secondary grid streets
                    val streetColor = Color(0xFF24303C)
                    for (i in 1..4) {
                        val y = size.height * (i * 0.21f)
                        drawLine(streetColor, Offset(0f, y), Offset(size.width, y - 16f), strokeWidth = 3.dp.toPx())
                    }
                    for (i in 1..4) {
                        val x = size.width * (i * 0.22f)
                        drawLine(streetColor, Offset(x, 0f), Offset(x + 20f, size.height), strokeWidth = 2.5.dp.toPx())
                    }

                    // Main arterial highway curve
                    val arterialPath = Path().apply {
                        moveTo(0f, size.height * 0.72f)
                        quadraticTo(size.width * 0.45f, size.height * 0.48f, size.width, size.height * 0.28f)
                    }
                    drawPath(
                        path = arterialPath,
                        color = Color(0xFF4B4729),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Metro / shuttle route line
                    val transitPath = Path().apply {
                        moveTo(size.width * 0.16f, 0f)
                        lineTo(size.width * 0.50f, size.height * 0.50f)
                        lineTo(size.width * 0.84f, size.height)
                    }
                    drawPath(
                        path = transitPath,
                        color = TruViolet.copy(alpha = 0.45f),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Gate pins
                    drawCircle(TruSuccessMint, radius = 6.dp.toPx(), center = Offset(size.width * 0.28f, size.height * 0.46f))
                    drawCircle(TruLime, radius = 6.dp.toPx(), center = Offset(size.width * 0.74f, size.height * 0.44f))
                    drawCircle(TruViolet, radius = 14.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.50f))
                }

                // Centered floating venue pill
                Surface(
                    shape = CircleShape,
                    color = TruBg.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Venue pin",
                            tint = TruSuccessMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = event.venuePinLabel.ifEmpty { event.title },
                            style = MaterialTheme.typography.labelLarge,
                            color = TruTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom-right directions cue
                Surface(
                    shape = CircleShape,
                    color = TruSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, TruLime.copy(alpha = 0.5f)),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Directions, contentDescription = "Get directions", tint = TruLime, modifier = Modifier.size(14.dp))
                        Text("Directions", style = MonoSmallStyle, color = TruLime)
                    }
                }
            }
        }
    }
}
