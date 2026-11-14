package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class TruTicketViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        TruTicketUiState(
            events = TruTicketRepository.events,
            tickets = TruTicketRepository.initialTickets,
            groupAllocations = TruTicketRepository.initialGroupAllocations,
            resaleListings = TruTicketRepository.initialResaleListings,
            sessions = TruTicketRepository.initialSessions,
            waitlistClaims = TruTicketRepository.initialWaitlistClaims,
            gateScanLogs = TruTicketRepository.initialGateLogs,
            qrExpiresAtEpoch = (System.currentTimeMillis() / 1000L) + 10L
        )
    )
    val uiState: StateFlow<TruTicketUiState> = _uiState.asStateFlow()

    init {
        startTimers()
    }

    private fun startTimers() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { state ->
                    val nextQrSec = if (state.qrSecondsLeft <= 1) 10 else state.qrSecondsLeft - 1
                    val rotateQr = state.qrSecondsLeft <= 1
                    val nextNonce = if (rotateQr) randomHex(8) else state.qrNonce
                    val nextExpires = if (rotateQr) (System.currentTimeMillis() / 1000L) + 10L else state.qrExpiresAtEpoch
                    val nextRes = if (state.reservationSecondsLeft > 0) state.reservationSecondsLeft - 1 else 600
                    val nextClaims = state.waitlistClaims.map {
                        it.copy(secondsRemaining = (it.secondsRemaining - 1).coerceAtLeast(0))
                    }
                    state.copy(
                        qrSecondsLeft = nextQrSec,
                        qrNonce = nextNonce,
                        qrExpiresAtEpoch = nextExpires,
                        reservationSecondsLeft = nextRes,
                        waitlistClaims = nextClaims
                    )
                }
            }
        }
    }

    fun selectRole(role: UserRole) {
        _uiState.update { it.copy(userRole = role, showRoleSwitcherSheet = false) }
    }

    fun selectCustomerTab(tabRoute: String) {
        _uiState.update { state ->
            state.copy(
                activeCustomerTab = tabRoute,
                resaleSellMode = if (tabRoute == com.example.ui.navigation.TruRoutes.RESALE && state.activeCustomerTab != com.example.ui.navigation.TruRoutes.RESALE) {
                    false
                } else {
                    state.resaleSellMode
                }
            )
        }
    }

    fun setResaleSellMode(sellMode: Boolean) {
        _uiState.update { it.copy(resaleSellMode = sellMode) }
    }

    fun openResaleInSellMode() {
        _uiState.update {
            it.copy(
                activeCustomerTab = com.example.ui.navigation.TruRoutes.RESALE,
                resaleSellMode = true
            )
        }
    }

    fun toggleRoleSwitcherSheet(show: Boolean) {
        _uiState.update { it.copy(showRoleSwitcherSheet = show) }
    }

    fun setCity(city: CityOption) {
        _uiState.update { it.copy(selectedCity = city) }
    }

    fun setCategory(category: EventCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun triggerExploreSkeletonRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExploreLoading = true) }
            delay(650L)
            _uiState.update { it.copy(isExploreLoading = false) }
        }
    }

    fun openEvent(eventId: String) {
        _uiState.update {
            val evt = it.events.firstOrNull { e -> e.id == eventId } ?: it.events.first()
            val defaultTier = if (it.userRole == UserRole.GROUP_MANAGER) {
                evt.tiers.firstOrNull { t -> t.isGroupTier } ?: evt.tiers.first()
            } else {
                evt.tiers.first()
            }
            it.copy(
                selectedEventId = evt.id,
                selectedTierId = defaultTier.id,
                selectedQty = if (defaultTier.isGroupTier) defaultTier.minQty else 2
            )
        }
    }

    fun startFairQueue() {
        _uiState.update {
            it.copy(
                queueSecondsLeft = 8,
                queueCompleted = false,
                queueSeedHash = randomHex(16),
                checkoutPhase = CheckoutPhase.IDLE,
                checkoutErrorMessage = null
            )
        }
        viewModelScope.launch {
            for (sec in 7 downTo 0) {
                delay(1000L)
                _uiState.update { state ->
                    if (state.queueCompleted) state
                    else state.copy(queueSecondsLeft = sec, queueCompleted = sec == 0)
                }
            }
        }
    }

    fun fastForwardQueue() {
        _uiState.update { it.copy(queueSecondsLeft = 0, queueCompleted = true) }
    }

    fun selectTier(tier: TicketTier) {
        _uiState.update {
            it.copy(
                selectedTierId = tier.id,
                selectedQty = if (tier.isGroupTier) tier.minQty.coerceAtLeast(10) else 2.coerceIn(tier.minQty, tier.maxQty),
                reservationSecondsLeft = 600
            )
        }
    }

    fun adjustQty(delta: Int) {
        _uiState.update { state ->
            val tier = state.selectedTier
            val next = (state.selectedQty + delta).coerceIn(tier.minQty, tier.maxQty)
            state.copy(selectedQty = next)
        }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method, checkoutErrorMessage = null) }
    }

    fun setSimulateFailure(shouldFail: Boolean) {
        _uiState.update { it.copy(simulatePaymentFailure = shouldFail) }
    }

    fun calculateOrderTotals(): Triple<Int, Int, Int> {
        val state = _uiState.value
        val base = state.selectedTier.priceInr * state.selectedQty
        val fee = (base * 3) / 100
        val tax = (fee * 18) / 100
        return Triple(base, fee, tax)
    }

    fun submitPayment() {
        val (base, fee, tax) = calculateOrderTotals()
        val total = base + fee + tax
        _uiState.update { it.copy(checkoutPhase = CheckoutPhase.PROCESSING, checkoutErrorMessage = null) }
        viewModelScope.launch {
            delay(1300L)
            val shouldFail = _uiState.value.simulatePaymentFailure
            if (shouldFail) {
                _uiState.update {
                    it.copy(
                        checkoutPhase = CheckoutPhase.FAILED,
                        checkoutErrorMessage = "Razorpay test mode: Bank declined OTP authorization. Switch to UPI Instant or tap Retry."
                    )
                }
            } else {
                val state = _uiState.value
                val evt = state.selectedEvent
                val tier = state.selectedTier
                val ref = "TT-${evt.city.label.take(3).uppercase()}-${Random.nextInt(1000, 9999)}"
                val orderRef = "ORD-TT-${Random.nextInt(1000, 9999)}"
                val newTicket = TicketItem(
                    ticketRef = ref,
                    eventId = evt.id,
                    eventTitle = evt.title,
                    venue = evt.venue,
                    city = evt.city.label,
                    dateLabel = evt.dateLabel,
                    timeLabel = evt.timeLabel,
                    tierName = tier.name,
                    faceValueInr = tier.priceInr,
                    status = if (tier.isGroupTier) TicketStatus.UNASSIGNED_GROUP else TicketStatus.UPCOMING,
                    transferCount = 0,
                    seatLabel = if (tier.isGroupTier) "TEAM BLOCK · ${state.selectedQty} PASSES" else "${tier.name.uppercase()} · QTY ${state.selectedQty}",
                    capBps = evt.resaleCapBps,
                    history = listOf(
                        TicketHistoryEntry("Ticket DNA minted", "Just now", "Proof ${TruTicketFormatters.shortHash(randomHex(16))}"),
                        TicketHistoryEntry("Order $orderRef settled", "Just now", "${state.paymentMethod.label} · ${TruTicketFormatters.formatInr(total)}")
                    )
                )
                val newGroupRows = if (tier.isGroupTier) {
                    (1..3).map { idx ->
                        GroupMemberAllocation(
                            id = "grp_new_${System.currentTimeMillis()}_$idx",
                            ticketRef = "$ref-$idx",
                            eventTitle = evt.title,
                            tierName = tier.name,
                            faceValueInr = tier.priceInr,
                            contact = null,
                            status = AllocationStatus.UNASSIGNED,
                            updatedAt = "Available to assign"
                        )
                    }
                } else emptyList()

                _uiState.update {
                    it.copy(
                        checkoutPhase = CheckoutPhase.SUCCESS,
                        lastOrderRef = orderRef,
                        lastOrderIsGroup = tier.isGroupTier,
                        lastOrderTotalInr = total,
                        activeTicketRef = ref,
                        tickets = listOf(newTicket) + it.tickets,
                        groupAllocations = newGroupRows + it.groupAllocations
                    )
                }
            }
        }
    }

    fun selectTicket(ticketRef: String) {
        _uiState.update { it.copy(activeTicketRef = ticketRef) }
    }

    fun assignGroupMember(allocationId: String, contact: String) {
        val clean = contact.trim()
        if (clean.isEmpty()) return
        _uiState.update { state ->
            val updated = state.groupAllocations.map { item ->
                if (item.id == allocationId) {
                    item.copy(
                        contact = clean,
                        status = AllocationStatus.PENDING_CLAIM,
                        updatedAt = "Invited just now · ₹0 fee"
                    )
                } else item
            }
            state.copy(groupAllocations = updated, snackbarMessage = "Invite sent to $clean at ₹0 fee")
        }
    }

    fun revokePendingAllocation(allocationId: String) {
        _uiState.update { state ->
            val updated = state.groupAllocations.map { item ->
                if (item.id == allocationId && item.status == AllocationStatus.PENDING_CLAIM) {
                    item.copy(
                        contact = null,
                        status = AllocationStatus.UNASSIGNED,
                        updatedAt = "Revoked · Available to assign"
                    )
                } else item
            }
            state.copy(groupAllocations = updated, snackbarMessage = "Pending invite revoked")
        }
    }

    fun buyResaleListing(listingId: String) {
        _uiState.update { state ->
            val listing = state.resaleListings.firstOrNull { it.id == listingId } ?: return@update state
            val newTicket = TicketItem(
                ticketRef = listing.ticketRef,
                eventId = "evt_sunburn_blr",
                eventTitle = listing.eventTitle,
                venue = "${listing.city} Official Venue",
                city = listing.city,
                dateLabel = listing.dateLabel,
                timeLabel = "19:00 IST",
                tierName = listing.tierName,
                faceValueInr = listing.faceValueInr,
                status = TicketStatus.UPCOMING,
                transferCount = 1,
                seatLabel = "${listing.tierName.uppercase()} · CAPPED RESALE",
                capBps = listing.capBps,
                history = listOf(
                    TicketHistoryEntry(
                        "Purchased on TruResale",
                        "Just now",
                        "Paid ${TruTicketFormatters.formatInr(listing.askPriceInr)} (within 110% cap)"
                    )
                )
            )
            state.copy(
                resaleListings = state.resaleListings.filterNot { it.id == listingId },
                tickets = listOf(newTicket) + state.tickets.filterNot { it.ticketRef == listing.ticketRef },
                activeTicketRef = newTicket.ticketRef,
                pendingTicketCelebration = true,
                snackbarMessage = "Purchased ${listing.ticketRef} safely within price cap"
            )
        }
    }

    fun consumeTicketCelebration() {
        _uiState.update { it.copy(pendingTicketCelebration = false) }
    }

    fun listTicketForResale(ticketRef: String, askPriceInr: Int) {
        _uiState.update { state ->
            val ticket = state.tickets.firstOrNull { it.ticketRef == ticketRef } ?: return@update state
            val event = state.events.firstOrNull { it.id == ticket.eventId }
            val minAsk = (ticket.faceValueInr * 0.7f).toInt().coerceAtLeast(100)
            val capped = askPriceInr.coerceIn(minAsk, ticket.maxResaleInr())
            val newListing = ResaleListing(
                id = "res_${System.currentTimeMillis()}",
                ticketRef = ticket.ticketRef,
                eventTitle = ticket.eventTitle,
                city = ticket.city,
                dateLabel = ticket.dateLabel,
                tierName = ticket.tierName,
                faceValueInr = ticket.faceValueInr,
                askPriceInr = capped,
                capBps = ticket.capBps,
                category = event?.category ?: EventCategory.CONCERTS,
                venue = ticket.venue,
                imageResId = event?.imageResId ?: com.example.R.drawable.img_event_concert_1794660145289,
                transferCount = ticket.transferCount
            )
            val updatedTickets = state.tickets.map {
                if (it.ticketRef == ticketRef) {
                    it.copy(
                        status = TicketStatus.LISTED,
                        listedAskInr = capped,
                        history = listOf(
                            TicketHistoryEntry(
                                "Listed on TruResale",
                                "Just now",
                                "Ask ${TruTicketFormatters.formatInr(capped)} (max ${TruTicketFormatters.formatInr(ticket.maxResaleInr())})"
                            )
                        ) + it.history
                    )
                } else it
            }
            state.copy(
                tickets = updatedTickets,
                resaleListings = listOf(newListing) + state.resaleListings.filterNot { it.ticketRef == ticketRef },
                snackbarMessage = "${ticket.ticketRef} listed at ${TruTicketFormatters.formatInr(capped)}"
            )
        }
    }

    fun updateResaleListing(ticketRef: String, newAskPriceInr: Int) {
        _uiState.update { state ->
            val ticket = state.tickets.firstOrNull { it.ticketRef == ticketRef } ?: return@update state
            val event = state.events.firstOrNull { it.id == ticket.eventId }
            val minAsk = (ticket.faceValueInr * 0.7f).toInt().coerceAtLeast(100)
            val capped = newAskPriceInr.coerceIn(minAsk, ticket.maxResaleInr())
            val existingListing = state.resaleListings.firstOrNull { it.ticketRef == ticketRef }
            val updatedListings = if (existingListing != null) {
                state.resaleListings.map {
                    if (it.ticketRef == ticketRef) it.copy(askPriceInr = capped) else it
                }
            } else {
                listOf(
                    ResaleListing(
                        id = "res_${System.currentTimeMillis()}",
                        ticketRef = ticket.ticketRef,
                        eventTitle = ticket.eventTitle,
                        city = ticket.city,
                        dateLabel = ticket.dateLabel,
                        tierName = ticket.tierName,
                        faceValueInr = ticket.faceValueInr,
                        askPriceInr = capped,
                        capBps = ticket.capBps,
                        category = event?.category ?: EventCategory.CONCERTS,
                        venue = ticket.venue,
                        imageResId = event?.imageResId ?: com.example.R.drawable.img_event_concert_1794660145289,
                        transferCount = ticket.transferCount
                    )
                ) + state.resaleListings
            }
            val updatedTickets = state.tickets.map {
                if (it.ticketRef == ticketRef) {
                    it.copy(
                        status = TicketStatus.LISTED,
                        listedAskInr = capped,
                        history = listOf(
                            TicketHistoryEntry(
                                "Updated resale price",
                                "Just now",
                                "New ask ${TruTicketFormatters.formatInr(capped)} (within 110% cap)"
                            )
                        ) + it.history
                    )
                } else it
            }
            state.copy(
                tickets = updatedTickets,
                resaleListings = updatedListings,
                snackbarMessage = "Updated ${ticket.ticketRef} ask price to ${TruTicketFormatters.formatInr(capped)}"
            )
        }
    }

    fun delistTicket(ticketRef: String) {
        _uiState.update { state ->
            val updatedTickets = state.tickets.map {
                if (it.ticketRef == ticketRef) {
                    it.copy(
                        status = TicketStatus.UPCOMING,
                        listedAskInr = null,
                        history = listOf(
                            TicketHistoryEntry(
                                "Delisted from TruResale",
                                "Just now",
                                "Returned to active Upcoming wallet"
                            )
                        ) + it.history
                    )
                } else it
            }
            state.copy(
                tickets = updatedTickets,
                resaleListings = state.resaleListings.filterNot { it.ticketRef == ticketRef },
                snackbarMessage = "$ticketRef delisted and returned to Upcoming"
            )
        }
    }

    fun verifyTicketRef(rawRef: String) {
        val ref = rawRef.trim().uppercase()
        val found = _uiState.value.tickets.firstOrNull { it.ticketRef.equals(ref, ignoreCase = true) }
        if (found != null) {
            _uiState.update {
                it.copy(
                    verifierState = VerifierResultState(
                        outcome = VerificationOutcome.GENUINE,
                        ticketRef = found.ticketRef,
                        eventTitle = found.eventTitle,
                        tierName = found.tierName,
                        faceValueInr = found.faceValueInr,
                        transferCount = found.transferCount,
                        statusLabel = found.status.label
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    verifierState = VerifierResultState(
                        outcome = VerificationOutcome.NOT_FOUND,
                        ticketRef = ref
                    )
                )
            }
        }
    }

    fun submitScamReport(reason: String) {
        _uiState.update {
            it.copy(
                verifierState = it.verifierState.copy(scamReported = true),
                snackbarMessage = "Scam report filed ($reason). Security team notified."
            )
        }
    }

    fun signOutSession(sessionId: String) {
        _uiState.update { state ->
            state.copy(
                sessions = state.sessions.filterNot { it.id == sessionId && !it.isCurrent },
                snackbarMessage = "Remote session signed out"
            )
        }
    }

    fun updateSecurityToggles(twoStep: Boolean? = null, drop: Boolean? = null, queue: Boolean? = null, resale: Boolean? = null) {
        _uiState.update {
            it.copy(
                twoStepEnabled = twoStep ?: it.twoStepEnabled,
                notifDropAlerts = drop ?: it.notifDropAlerts,
                notifQueueAlerts = queue ?: it.notifQueueAlerts,
                notifResaleAlerts = resale ?: it.notifResaleAlerts
            )
        }
    }

    fun toggleGateOnline() {
        _uiState.update { it.copy(gateIsOnline = !it.gateIsOnline) }
    }

    fun performGateScan(ticketRefInput: String): GateScanRecord {
        val cleanRef = ticketRefInput.trim().uppercase()
        val state = _uiState.value
        val nowTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val ticket = state.tickets.firstOrNull { it.ticketRef.equals(cleanRef, ignoreCase = true) }

        val record = when {
            !state.gateIsOnline -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = cleanRef.ifEmpty { "TT-OFF-0000" },
                tierName = ticket?.tierName ?: "Unknown",
                verdict = ScanVerdict.OFFLINE_UNVERIFIED,
                timestampLabel = nowTime
            )
            ticket == null -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = cleanRef.ifEmpty { "UNKNOWN" },
                tierName = "—",
                verdict = ScanVerdict.INVALID,
                timestampLabel = nowTime
            )
            state.redeemedScanTimes.containsKey(ticket.ticketRef) -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = ticket.ticketRef,
                tierName = ticket.tierName,
                verdict = ScanVerdict.ALREADY_USED,
                timestampLabel = nowTime,
                firstScanTimeLabel = state.redeemedScanTimes[ticket.ticketRef]
            )
            ticket.status == TicketStatus.REVOKED -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = ticket.ticketRef,
                tierName = ticket.tierName,
                verdict = ScanVerdict.REVOKED,
                timestampLabel = nowTime
            )
            ticket.status == TicketStatus.EXPIRED -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = ticket.ticketRef,
                tierName = ticket.tierName,
                verdict = ScanVerdict.EXPIRED,
                timestampLabel = nowTime
            )
            ticket.isWrongEventForGate || ticket.eventId != state.gateEventId -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = ticket.ticketRef,
                tierName = ticket.tierName,
                verdict = ScanVerdict.WRONG_EVENT,
                timestampLabel = nowTime
            )
            else -> GateScanRecord(
                id = "scan_${System.currentTimeMillis()}",
                ticketRef = ticket.ticketRef,
                tierName = ticket.tierName,
                verdict = ScanVerdict.VALID,
                timestampLabel = nowTime
            )
        }

        _uiState.update { current ->
            val nextRedeemed = if (record.verdict == ScanVerdict.VALID) {
                current.redeemedScanTimes + (record.ticketRef to nowTime)
            } else {
                current.redeemedScanTimes
            }
            current.copy(
                redeemedScanTimes = nextRedeemed,
                lastGateResult = record,
                gateScanLogs = listOf(record) + current.gateScanLogs
            )
        }
        return record
    }

    fun forceRotateTicketDna() {
        _uiState.update { state ->
            state.copy(
                qrSecondsLeft = 10,
                qrNonce = randomHex(8),
                qrExpiresAtEpoch = (System.currentTimeMillis() / 1000L) + 10L
            )
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun randomHex(length: Int): String {
        val chars = "0123456789abcdef"
        return (1..length).map { chars.random() }.joinToString("")
    }
}
