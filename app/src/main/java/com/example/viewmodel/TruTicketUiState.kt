package com.example.viewmodel

import com.example.data.ActiveSession
import com.example.data.CityOption
import com.example.data.EventCategory
import com.example.data.GateScanRecord
import com.example.data.GroupMemberAllocation
import com.example.data.PaymentMethod
import com.example.data.ResaleListing
import com.example.data.TicketItem
import com.example.data.TicketTier
import com.example.data.TruEvent
import com.example.data.UserRole
import com.example.data.WaitlistClaim

enum class CheckoutPhase {
    IDLE,
    PROCESSING,
    FAILED,
    SUCCESS
}

enum class VerificationOutcome {
    IDLE,
    GENUINE,
    NOT_FOUND
}

data class VerifierResultState(
    val outcome: VerificationOutcome = VerificationOutcome.IDLE,
    val ticketRef: String = "",
    val eventTitle: String = "",
    val tierName: String = "",
    val faceValueInr: Int = 0,
    val transferCount: Int = 0,
    val statusLabel: String = "",
    val scamReported: Boolean = false
)

data class TruTicketUiState(
    val userRole: UserRole = UserRole.ATTENDEE,
    val showRoleSwitcherSheet: Boolean = false,
    val activeCustomerTab: String = com.example.ui.navigation.TruRoutes.EXPLORE,
    val resaleSellMode: Boolean = false,
    val selectedCity: CityOption = CityOption.ALL,
    val selectedCategory: EventCategory = EventCategory.ALL,
    val searchQuery: String = "",
    val isExploreLoading: Boolean = false,
    val events: List<TruEvent> = emptyList(),
    val selectedEventId: String = "evt_sunburn_blr",
    val queueSecondsLeft: Int = 8,
    val queueSeedHash: String = "9f3a81d4e72bc21d",
    val queueCompleted: Boolean = false,
    val selectedTierId: String = "tier_gen",
    val selectedQty: Int = 2,
    val reservationSecondsLeft: Int = 600,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val simulatePaymentFailure: Boolean = false,
    val checkoutPhase: CheckoutPhase = CheckoutPhase.IDLE,
    val checkoutErrorMessage: String? = null,
    val lastOrderRef: String = "ORD-TT-8492",
    val lastOrderIsGroup: Boolean = false,
    val lastOrderTotalInr: Int = 0,
    val tickets: List<TicketItem> = emptyList(),
    val activeTicketRef: String = "TT-BLR-8841",
    val qrSecondsLeft: Int = 10,
    val qrNonce: String = "n84f2a9c",
    val qrExpiresAtEpoch: Long = 0L,
    val groupAllocations: List<GroupMemberAllocation> = emptyList(),
    val resaleListings: List<ResaleListing> = emptyList(),
    val verifierState: VerifierResultState = VerifierResultState(),
    val sessions: List<ActiveSession> = emptyList(),
    val twoStepEnabled: Boolean = true,
    val notifDropAlerts: Boolean = true,
    val notifQueueAlerts: Boolean = true,
    val notifResaleAlerts: Boolean = false,
    val waitlistClaims: List<WaitlistClaim> = emptyList(),
    val gateIsOnline: Boolean = true,
    val gateEventTitle: String = "Echoes of Earth: Nocturne Live",
    val gateEventId: String = "evt_sunburn_blr",
    val gateScanLogs: List<GateScanRecord> = emptyList(),
    val lastGateResult: GateScanRecord? = null,
    val redeemedScanTimes: Map<String, String> = mapOf("TT-BLR-4998" to "18:02:11"),
    val snackbarMessage: String? = null
) {
    val selectedEvent: TruEvent
        get() = events.firstOrNull { it.id == selectedEventId } ?: events.first()

    val selectedTier: TicketTier
        get() = selectedEvent.tiers.firstOrNull { it.id == selectedTierId } ?: selectedEvent.tiers.first()

    val activeTicket: TicketItem?
        get() = tickets.firstOrNull { it.ticketRef == activeTicketRef }
}
