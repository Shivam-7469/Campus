package com.example.data

import androidx.annotation.DrawableRes

enum class UserRole(val label: String, val subtitle: String) {
    ATTENDEE("Attendee", "Browse, join FairQueue, hold Ticket DNA & buy/sell within cap"),
    GROUP_MANAGER("Group manager", "Buy 10–50 corporate passes & allocate to team members"),
    GATE_STAFF("Gate staff", "High-contrast entry scanner, manual lookup & live gate log")
}

enum class CityOption(val label: String) {
    ALL("All cities"),
    BENGALURU("Bengaluru"),
    MUMBAI("Mumbai"),
    DELHI("Delhi")
}

enum class EventCategory(val label: String) {
    ALL("All"),
    CONCERTS("Concerts"),
    STAND_UP("Stand-up"),
    CRICKET("Cricket"),
    TECH_SUMMITS("Tech summits"),
    COLLEGE_FESTS("College fests")
}

data class TicketTier(
    val id: String,
    val name: String,
    val priceInr: Int,
    val remaining: Int,
    val total: Int,
    val perks: String,
    val isGroupTier: Boolean = false,
    val minQty: Int = 1,
    val maxQty: Int = 4
)

data class EventScheduleSlot(
    val time: String,
    val title: String,
    val stageTag: String,
    val isHighlight: Boolean = false
)

data class TruEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: EventCategory,
    val city: CityOption,
    val venue: String,
    val dateLabel: String,
    val timeLabel: String,
    val organizer: String,
    val isVerifiedOrganizer: Boolean,
    @DrawableRes val imageResId: Int,
    val resaleCapBps: Int = 11000,
    val groupPolicySummary: String,
    val resalePolicySummary: String,
    val soldPercent: Int,
    val tiers: List<TicketTier>,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val scheduleTitle: String = "Live Lineup Schedule",
    val scheduleSubtitle: String = "Continuous sets across synchronized stages.",
    val scheduleSlots: List<EventScheduleSlot> = emptyList(),
    val venueGuidelines: List<String> = emptyList(),
    val venueLocality: String = "",
    val venuePinLabel: String = ""
) {
    fun maxResaleInr(faceValueInr: Int): Int = (faceValueInr * resaleCapBps) / 10000
}

enum class TicketStatus(val label: String) {
    UPCOMING("Upcoming"),
    PAST("Past"),
    LISTED("Listed"),
    UNASSIGNED_GROUP("Unassigned group"),
    REVOKED("Revoked"),
    EXPIRED("Expired")
}

data class TicketHistoryEntry(
    val title: String,
    val timestamp: String,
    val detail: String
)

data class TicketItem(
    val ticketRef: String,
    val eventId: String,
    val eventTitle: String,
    val venue: String,
    val city: String,
    val dateLabel: String,
    val timeLabel: String,
    val tierName: String,
    val faceValueInr: Int,
    val status: TicketStatus,
    val transferCount: Int,
    val seatLabel: String,
    val capBps: Int = 11000,
    val listedAskInr: Int? = null,
    val history: List<TicketHistoryEntry>,
    val isWrongEventForGate: Boolean = false
) {
    fun maxResaleInr(): Int = (faceValueInr * capBps) / 10000
}

enum class AllocationStatus(val label: String) {
    CLAIMED("CLAIMED"),
    PENDING_CLAIM("PENDING_CLAIM"),
    UNASSIGNED("UNASSIGNED")
}

data class GroupMemberAllocation(
    val id: String,
    val ticketRef: String,
    val eventTitle: String,
    val tierName: String,
    val faceValueInr: Int,
    val contact: String?,
    val status: AllocationStatus,
    val updatedAt: String
)

data class ResaleListing(
    val id: String,
    val ticketRef: String,
    val eventTitle: String,
    val city: String,
    val dateLabel: String,
    val tierName: String,
    val faceValueInr: Int,
    val askPriceInr: Int,
    val capBps: Int = 11000,
    val category: EventCategory = EventCategory.CONCERTS,
    val venue: String = "",
    @DrawableRes val imageResId: Int = com.example.R.drawable.img_event_concert_1794660145289,
    val transferCount: Int = 0
) {
    fun maxCapInr(): Int = (faceValueInr * capBps) / 10000
}

data class ActiveSession(
    val id: String,
    val deviceName: String,
    val city: String,
    val lastActive: String,
    val isCurrent: Boolean
)

data class WaitlistClaim(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val tierName: String,
    val priceInr: Int,
    val secondsRemaining: Int
)

enum class ScanVerdict(val title: String) {
    VALID("VALID"),
    ALREADY_USED("ALREADY USED"),
    EXPIRED("EXPIRED"),
    REVOKED("REVOKED"),
    INVALID("INVALID"),
    WRONG_EVENT("WRONG EVENT"),
    OFFLINE_UNVERIFIED("OFFLINE-UNVERIFIED")
}

data class GateScanRecord(
    val id: String,
    val ticketRef: String,
    val tierName: String,
    val verdict: ScanVerdict,
    val timestampLabel: String,
    val firstScanTimeLabel: String? = null
)

enum class PaymentMethod(val label: String, val subtitle: String) {
    UPI("UPI Instant", "GPay, PhonePe, BHIM · Zero surcharge"),
    CARD("Credit / Debit Card", "Visa, Mastercard, RuPay · 3DS2"),
    NETBANKING("Netbanking", "HDFC, ICICI, SBI, Axis")
}
