package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState
import kotlin.math.roundToInt

private enum class ResaleFilter(val label: String) {
    ALL("All Events"),
    CONCERTS("Concerts"),
    COMEDY("Comedy"),
    CRICKET("Cricket"),
    UNDER_1500("Under ₹1,500")
}

private enum class ResaleSort(val label: String) {
    LOWEST_ASK("Lowest Ask First"),
    FACE_VALUE("Face Value")
}

@Composable
fun ResaleScreen(
    state: TruTicketUiState,
    onSetSellMode: (Boolean) -> Unit,
    onBuyListing: (String) -> Unit,
    onListTicket: (String, Int) -> Unit,
    onUpdateListing: (String, Int) -> Unit,
    onDelistTicket: (String) -> Unit,
    onLogoLongPress: () -> Unit
) {
    val sellMode = state.resaleSellMode
    var pendingBuyListing by remember { mutableStateOf<ResaleListing?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(ResaleFilter.ALL) }
    var activeSort by remember { mutableStateOf(ResaleSort.LOWEST_ASK) }

    val filteredListings = remember(state.resaleListings, searchQuery, activeFilter, activeSort) {
        state.resaleListings.filter { item ->
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                item.eventTitle.contains(q, ignoreCase = true) ||
                item.city.contains(q, ignoreCase = true) ||
                item.venue.contains(q, ignoreCase = true) ||
                item.tierName.contains(q, ignoreCase = true)
            val matchesFilter = when (activeFilter) {
                ResaleFilter.ALL -> true
                ResaleFilter.CONCERTS -> item.category == EventCategory.CONCERTS
                ResaleFilter.COMEDY -> item.category == EventCategory.STAND_UP
                ResaleFilter.CRICKET -> item.category == EventCategory.CRICKET
                ResaleFilter.UNDER_1500 -> item.askPriceInr <= 1500
            }
            matchesQuery && matchesFilter
        }.let { list ->
            when (activeSort) {
                ResaleSort.LOWEST_ASK -> list.sortedBy { it.askPriceInr }
                ResaleSort.FACE_VALUE -> list.sortedBy { it.askPriceInr - it.faceValueInr }
            }
        }
    }

    val manageableTickets = remember(state.tickets) {
        state.tickets.filter { it.status == TicketStatus.UPCOMING || it.status == TicketStatus.LISTED }
    }
    var selectedTicketRef by remember(manageableTickets, state.activeTicketRef) {
        val preferred = manageableTickets.firstOrNull { it.ticketRef == state.activeTicketRef }
            ?: manageableTickets.firstOrNull()
        mutableStateOf(preferred?.ticketRef ?: "")
    }
    val selectedTicket = manageableTickets.firstOrNull { it.ticketRef == selectedTicketRef }
        ?: manageableTickets.firstOrNull()

    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val topPadding = with(density) { topBarHeightPx.toDp() } + 12.dp
    val topBackdropLayer = rememberGraphicsLayer()
    var scrollContainerOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { scrollContainerOffset = it.positionInRoot() }
                .drawWithContent {
                    topBackdropLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(topBackdropLayer)
                }
        ) {
            if (!sellMode) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = topPadding, bottom = 118.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ResaleHeaderAndToggle(
                                sellMode = sellMode,
                                listingCount = state.resaleListings.size,
                                onSetSellMode = onSetSellMode
                            )
                        }
                    }

                    item {
                        TruGlassSearchBar(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search verified resale tickets, artists, venues…",
                            testTag = "resale_search_input",
                            leadingContentDescription = "Search resale",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ResaleFilter.entries) { filter ->
                                val selected = activeFilter == filter
                                TruGlassFilterChip(
                                    label = filter.label,
                                    selected = selected,
                                    onClick = { activeFilter = filter },
                                    modifier = Modifier.testTag("resale_filter_${filter.name}")
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredListings.size} VERIFIED LISTINGS READY",
                                style = MonoSmallStyle,
                                color = TruTextMuted
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        activeSort = if (activeSort == ResaleSort.LOWEST_ASK) ResaleSort.FACE_VALUE else ResaleSort.LOWEST_ASK
                                    }
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                                    .testTag("resale_sort_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Sort, contentDescription = "Sort listings", tint = TruLime, modifier = Modifier.size(15.dp))
                                Text("Sort: ${activeSort.label}", style = MaterialTheme.typography.labelMedium, color = TruTextPrimary)
                            }
                        }
                    }

                    if (filteredListings.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = TruSurface,
                                border = BorderStroke(1.dp, TruBorder),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Filled.SearchOff, null, tint = TruTextMuted, modifier = Modifier.size(36.dp))
                                    Text("No matching resale passes", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                                    TextButton(onClick = {
                                        searchQuery = ""
                                        activeFilter = ResaleFilter.ALL
                                    }) {
                                        Text("Reset filters", color = TruLime)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredListings, key = { it.id }) { listing ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                FairResaleListingCard(listing = listing, onBuy = { pendingBuyListing = listing })
                            }
                        }
                    }
                }
            } else {
                SellAndManageSection(
                    tickets = manageableTickets,
                    selectedTicket = selectedTicket,
                    topPadding = topPadding,
                    sellMode = sellMode,
                    listingCount = state.resaleListings.size,
                    onSetSellMode = onSetSellMode,
                    onSelectTicket = { selectedTicketRef = it },
                    onListTicket = { ref, ask ->
                        onListTicket(ref, ask)
                        onSetSellMode(false)
                    },
                    onUpdateListing = { ref, ask ->
                        onUpdateListing(ref, ask)
                        onSetSellMode(false)
                    },
                    onDelistTicket = onDelistTicket
                )
            }
        }

        TruTopBar(
            title = null,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress,
            backdropLayer = topBackdropLayer,
            backdropContainerOffset = scrollContainerOffset,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height },
            trailingAction = {
                TruGlassSurface(
                    shape = CircleShape,
                    blurRadius = 12.dp,
                    shadowElevation = 8.dp,
                    topTint = Color(0xB8212131),
                    bottomTint = Color(0xA614141F),
                    rimHighlightColor = Color.White.copy(alpha = 0.28f),
                    rimGlowColor = TruLime.copy(alpha = 0.34f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = TruViolet, modifier = Modifier.size(13.dp))
                        Text("110% CAP", style = MonoSmallStyle, color = TruTextPrimary, maxLines = 1, softWrap = false)
                    }
                }
            }
        )
    }

    if (pendingBuyListing != null) {
        ResaleReviewSheet(
            listing = pendingBuyListing!!,
            onDismiss = { pendingBuyListing = null },
            onConfirmPay = {
                val id = pendingBuyListing!!.id
                pendingBuyListing = null
                onBuyListing(id)
            }
        )
    }
}

@Composable
private fun SegTabButton(
    label: String,
    badge: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) TruLime else TruSurface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) TruBg else TruTextMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(5.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) TruBg.copy(alpha = 0.14f) else TruSurfaceElevated
            ) {
                Text(
                    text = badge,
                    style = MonoSmallStyle,
                    color = if (selected) TruBg else TruSuccessMint,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ResaleHeaderAndToggle(
    sellMode: Boolean,
    listingCount: Int,
    onSetSellMode: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = TruSuccessMint, modifier = Modifier.size(20.dp))
                Text("Fair Resale Marketplace", style = MaterialTheme.typography.headlineMedium, color = TruTextPrimary)
            }
            Text(
                text = "Capped at 110% MRP · 0% Scalping Engine",
                style = MaterialTheme.typography.bodySmall,
                color = TruTextMuted
            )
        }

        // Fixed-height 50.dp segmented toggle (prevents vertical expansion bug)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TruSurface)
                .border(1.dp, TruBorder, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SegTabButton(
                label = "Buy Passes",
                badge = listingCount.toString(),
                selected = !sellMode,
                onClick = { onSetSellMode(false) },
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("resale_toggle_buy")
            )
            SegTabButton(
                label = "Sell Passes",
                badge = "Max +10%",
                selected = sellMode,
                onClick = { onSetSellMode(true) },
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("resale_toggle_sell")
            )
        }
    }
}

@Composable
private fun SellAndManageSection(
    tickets: List<TicketItem>,
    selectedTicket: TicketItem?,
    topPadding: androidx.compose.ui.unit.Dp,
    sellMode: Boolean,
    listingCount: Int,
    onSetSellMode: (Boolean) -> Unit,
    onSelectTicket: (String) -> Unit,
    onListTicket: (String, Int) -> Unit,
    onUpdateListing: (String, Int) -> Unit,
    onDelistTicket: (String) -> Unit
) {
    if (selectedTicket == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ResaleHeaderAndToggle(sellMode = sellMode, listingCount = listingCount, onSetSellMode = onSetSellMode)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You have no upcoming or listed tickets to manage.", style = MaterialTheme.typography.bodyLarge, color = TruTextMuted)
            }
        }
        return
    }

    val isAlreadyListed = selectedTicket.status == TicketStatus.LISTED
    val minAsk = (selectedTicket.faceValueInr * 0.7f).roundToInt()
    val maxCap = selectedTicket.maxResaleInr()
    var askPrice by remember(selectedTicket.ticketRef, selectedTicket.listedAskInr) {
        val initial = (selectedTicket.listedAskInr ?: selectedTicket.faceValueInr).coerceIn(minAsk, maxCap)
        mutableFloatStateOf(initial.toFloat())
    }
    val askInt = askPrice.roundToInt().coerceIn(minAsk, maxCap)
    val protocolFee = (askInt * 3) / 100
    val netProceeds = askInt - protocolFee

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ResaleHeaderAndToggle(sellMode = sellMode, listingCount = listingCount, onSetSellMode = onSetSellMode)
        }
        item {
            TruStatusBadge(text = "Cap enforced · 11000 bps (110% max)", tone = TruStatusTone.SUCCESS, icon = Icons.Filled.Shield)
        }
        item {
            Text("Select pass to list or manage", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
        }
        items(tickets, key = { it.ticketRef }) { t ->
            val picked = t.ticketRef == selectedTicket.ticketRef
            val listed = t.status == TicketStatus.LISTED
            Surface(
                onClick = { onSelectTicket(t.ticketRef) },
                shape = RoundedCornerShape(16.dp),
                color = if (picked) TruSurfaceElevated else TruSurface,
                border = BorderStroke(if (picked) 1.5.dp else 1.dp, if (picked) TruLime else TruBorder),
                modifier = Modifier.fillMaxWidth().testTag("sell_ticket_${t.ticketRef}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(t.eventTitle, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${t.ticketRef} · ${t.tierName}", style = MonoSmallStyle, color = TruTextMuted)
                            TruStatusBadge(text = if (listed) "Listed" else "Upcoming", tone = if (listed) TruStatusTone.WARNING else TruStatusTone.SUCCESS)
                        }
                    }
                    Text(TruTicketFormatters.formatInr(t.listedAskInr ?: t.faceValueInr), style = MonoMediumStyle, color = TruLime)
                }
            }
        }
        item {
            TicketStubSurface(cornerRadius = 24.dp) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isAlreadyListed) "Edit ask price" else "Your ask price", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        Text(TruTicketFormatters.formatInr(askInt), style = MonoLargeStyle, color = TruLime)
                    }
                    Slider(
                        value = askPrice,
                        onValueChange = { askPrice = it.coerceIn(minAsk.toFloat(), maxCap.toFloat()) },
                        valueRange = minAsk.toFloat()..maxCap.toFloat(),
                        colors = SliderDefaults.colors(thumbColor = TruLime, activeTrackColor = TruLime, inactiveTrackColor = TruSurfaceElevated),
                        modifier = Modifier.testTag("resale_cap_slider")
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Min ${TruTicketFormatters.formatInr(minAsk)}", style = MonoSmallStyle, color = TruTextMuted)
                        Text("Hard cap ${TruTicketFormatters.formatInr(maxCap)}", style = MonoSmallStyle, color = TruWarningAmber)
                    }
                }
                PerforatedDivider()
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("3% escrow & verification fee", style = MaterialTheme.typography.bodyMedium, color = TruTextMuted)
                        Text("-${TruTicketFormatters.formatInr(protocolFee)}", style = MonoMediumStyle, color = TruDangerCoral)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Your net proceeds", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                        Text(TruTicketFormatters.formatInr(netProceeds), style = MonoLargeStyle, color = TruSuccessMint)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TruPrimaryButton(
                    text = if (isAlreadyListed) "Update ask to ${TruTicketFormatters.formatInr(askInt)}" else "List for ${TruTicketFormatters.formatInr(askInt)}",
                    onClick = {
                        if (isAlreadyListed) onUpdateListing(selectedTicket.ticketRef, askInt)
                        else onListTicket(selectedTicket.ticketRef, askInt)
                    },
                    testTag = "list_resale_submit",
                    leadingIcon = Icons.Filled.SwapHoriz
                )
                if (isAlreadyListed) {
                    OutlinedButton(
                        onClick = { onDelistTicket(selectedTicket.ticketRef) },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, TruDangerCoral.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("delist_resale_button")
                    ) {
                        Icon(Icons.Filled.Undo, contentDescription = null, tint = TruDangerCoral, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delist ticket", style = MaterialTheme.typography.titleMedium, color = TruDangerCoral)
                    }
                }
            }
        }
    }
}
