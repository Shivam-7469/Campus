package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@Composable
fun ExploreScreen(
    state: TruTicketUiState,
    onSelectCity: (CityOption) -> Unit,
    onSelectCategory: (EventCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onRefreshSkeleton: () -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenVerifier: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    val filteredEvents = remember(state.events, state.selectedCity, state.selectedCategory, state.searchQuery) {
        state.events.filter { evt ->
            val cityMatch = state.selectedCity == CityOption.ALL || evt.city == state.selectedCity
            val catMatch = state.selectedCategory == EventCategory.ALL || evt.category == state.selectedCategory
            val q = state.searchQuery.trim()
            val queryMatch = q.isEmpty() ||
                evt.title.contains(q, ignoreCase = true) ||
                evt.venue.contains(q, ignoreCase = true) ||
                evt.city.label.contains(q, ignoreCase = true)
            cityMatch && catMatch && queryMatch
        }
    }

    val featured = filteredEvents.firstOrNull { it.isFeatured } ?: filteredEvents.firstOrNull()
    val trending = filteredEvents.filter { it.isTrending }

    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val topPadding = with(density) { topBarHeightPx.toDp() } + 12.dp
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
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                                append("Good evening, ")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("Vihaan")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = TruTextPrimary
                    )
                    Text(
                        text = "Verified live drops across India · Zero scalper markups",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TruTextMuted
                    )
                }
            }

            // City selector glass chips (full-bleed horizontal scroll so chips never clip in gutter)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CityOption.entries) { city ->
                        val active = state.selectedCity == city
                        TruGlassFilterChip(
                            label = city.label,
                            selected = active,
                            onClick = { onSelectCity(city) },
                            leadingIcon = Icons.Filled.LocationOn,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("city_${city.name}")
                        )
                    }
                }
            }

            // Glassmorphism search bar
            item {
                TruGlassSearchBar(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Search concerts, cricket, comedy, venues…",
                    testTag = "explore_search_input",
                    boldText = true,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Working category glass filters
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(EventCategory.entries) { cat ->
                        val active = state.selectedCategory == cat
                        TruGlassFilterChip(
                            label = cat.label,
                            selected = active,
                            onClick = { onSelectCategory(cat) },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("cat_${cat.name}")
                        )
                    }
                }
            }

            if (state.isExploreLoading) {
                items(3) {
                    ShimmerSkeletonCard(
                        height = if (it == 0) 240.dp else 110.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else if (filteredEvents.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TruSurface,
                        border = BorderStroke(1.dp, TruBorder),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.SearchOff, contentDescription = null, tint = TruTextMuted, modifier = Modifier.size(40.dp))
                            Text(
                                text = "No matching events",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TruTextPrimary
                            )
                            Text(
                                "Try switching city to All cities or clearing your search filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TruTextMuted
                            )
                            TextButton(onClick = {
                                onSelectCity(CityOption.ALL)
                                onSelectCategory(EventCategory.ALL)
                                onSearchChange("")
                            }) {
                                Text("Reset filters", fontWeight = FontWeight.Bold, color = TruLime)
                            }
                        }
                    }
                }
            } else {
                // Featured poster
                if (featured != null) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            FeaturedPosterCard(event = featured, onClick = { onOpenEvent(featured.id) })
                        }
                    }
                }

                // Trending drops with sold-% bars
                if (trending.isNotEmpty()) {
                    item {
                        Text(
                            text = "Trending drops",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TruTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(trending, key = { it.id }) { evt ->
                                TrendingDropCard(event = evt, onClick = { onOpenEvent(evt.id) })
                            }
                        }
                    }
                }

                // Near-you list
                item {
                    Text(
                        text = "Near you",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TruTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(filteredEvents, key = { "near_${it.id}" }) { evt ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        NearYouEventRow(event = evt, onClick = { onOpenEvent(evt.id) })
                    }
                }
            }
        }

        TruTopBar(
            role = state.userRole,
            onLogoLongPress = onLogoLongPress,
            backdropLayer = topBackdropLayer,
            backdropContainerOffset = scrollContainerOffset,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height },
            trailingAction = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TruGlassSurface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        blurRadius = 12.dp,
                        shadowElevation = 8.dp,
                        topTint = Color(0xB8212131),
                        bottomTint = Color(0xA614141F),
                        rimHighlightColor = Color.White.copy(alpha = 0.28f),
                        rimGlowColor = TruLime.copy(alpha = 0.30f),
                        onClick = onRefreshSkeleton
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh drops", tint = TruTextPrimary)
                        }
                    }
                    TruGlassSurface(
                        modifier = Modifier.heightIn(min = 48.dp).testTag("explore_verify_button"),
                        shape = CircleShape,
                        blurRadius = 12.dp,
                        shadowElevation = 10.dp,
                        topTint = Color(0xCC1B2312),
                        bottomTint = Color(0xBF12170D),
                        rimHighlightColor = Color.White.copy(alpha = 0.38f),
                        rimGlowColor = TruLime.copy(alpha = 0.72f),
                        shadowGlowColor = TruLime.copy(alpha = 0.25f),
                        onClick = onOpenVerifier
                    ) {
                        Row(
                            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = TruLime, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Verify",
                                style = MaterialTheme.typography.labelLarge,
                                color = TruLime,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun FeaturedPosterCard(event: TruEvent, onClick: () -> Unit) {
    TicketStubSurface(
        cornerRadius = 28.dp,
        onClick = onClick,
        modifier = Modifier.testTag("featured_event_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
            Image(
                painter = painterResource(id = event.imageResId),
                contentDescription = "${event.title} poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(TruBg.copy(alpha = 0.1f), TruBg.copy(alpha = 0.92f))
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TruStatusBadge(text = "Featured drop", tone = TruStatusTone.INFO, icon = Icons.Filled.Bolt)
                TruStatusBadge(text = "110% cap", tone = TruStatusTone.SUCCESS, icon = Icons.Filled.Shield)
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${event.city.label.uppercase()} · ${event.dateLabel.uppercase()}",
                    style = MonoSmallStyle,
                    fontWeight = FontWeight.Bold,
                    color = TruLime
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TruTextPrimary
                )
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TruTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        PerforatedDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("STARTS AT", style = MonoSmallStyle, fontWeight = FontWeight.Bold, color = TruTextMuted)
                Text(
                    text = TruTicketFormatters.formatInr(event.tiers.minOf { it.priceInr }),
                    style = MonoLargeStyle,
                    fontWeight = FontWeight.Bold,
                    color = TruLime
                )
            }
            Surface(shape = CircleShape, color = TruLime) {
                Text(
                    text = "View passes",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TruBg,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun TrendingDropCard(event: TruEvent, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = TruSurface,
        border = BorderStroke(1.dp, TruBorder),
        modifier = Modifier.width(236.dp).testTag("trending_card_${event.id}")
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                Image(
                    painter = painterResource(id = event.imageResId),
                    contentDescription = "${event.title} photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(TruBg.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(event.city.label, style = MonoSmallStyle, fontWeight = FontWeight.Bold, color = TruLime)
                }
            }
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TruTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.dateLabel} · ${event.timeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TruTextMuted
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${event.soldPercent}% sold",
                            style = MonoSmallStyle,
                            fontWeight = FontWeight.Bold,
                            color = TruWarningAmber
                        )
                        Text(
                            TruTicketFormatters.formatInr(event.tiers.first().priceInr),
                            style = MonoSmallStyle,
                            fontWeight = FontWeight.Bold,
                            color = TruTextPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { event.soldPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = if (event.soldPercent >= 90) TruDangerCoral else TruLime,
                        trackColor = TruSurfaceElevated
                    )
                }
            }
        }
    }
}

@Composable
private fun NearYouEventRow(event: TruEvent, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = TruSurface,
        border = BorderStroke(1.dp, TruBorder),
        modifier = Modifier.fillMaxWidth().testTag("event_row_${event.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = event.imageResId),
                contentDescription = "${event.title} thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    event.category.label.uppercase(),
                    style = MonoSmallStyle,
                    fontWeight = FontWeight.Bold,
                    color = TruViolet
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TruTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.city.label} · ${event.dateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TruTextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = TruTicketFormatters.formatInr(event.tiers.minOf { it.priceInr }),
                    style = MonoMediumStyle,
                    fontWeight = FontWeight.Bold,
                    color = TruLime
                )
                Text("onwards", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TruTextMuted)
            }
        }
    }
}
