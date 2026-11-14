package com.example.data

import com.example.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object TruTicketFormatters {
    fun formatInr(amount: Int): String {
        val symbols = DecimalFormatSymbols(Locale("en", "IN"))
        val df = DecimalFormat("#,##,##0", symbols)
        return "₹${df.format(amount)}"
    }

    fun shortHash(raw: String): String {
        val clean = raw.filter { it.isLetterOrDigit() }.lowercase()
        return if (clean.length >= 8) {
            "${clean.take(4)}…${clean.takeLast(4)}"
        } else {
            "9f3a…c21d"
        }
    }
}

object TruTicketRepository {
    private val defaultTiers = listOf(
        TicketTier(
            id = "tier_gen",
            name = "General",
            priceInr = 1500,
            remaining = 184,
            total = 1200,
            perks = "Standing floor · RFID wristband · All main stages",
            isGroupTier = false,
            minQty = 1,
            maxQty = 4
        ),
        TicketTier(
            id = "tier_vip",
            name = "VIP",
            priceInr = 4500,
            remaining = 29,
            total = 250,
            perks = "Elevated viewing deck · Fast-track gate · 2 lounge beverages",
            isGroupTier = false,
            minQty = 1,
            maxQty = 4
        ),
        TicketTier(
            id = "tier_stu",
            name = "Student",
            priceInr = 800,
            remaining = 42,
            total = 300,
            perks = "College ID verified at gate · General arena entry",
            isGroupTier = false,
            minQty = 1,
            maxQty = 4
        ),
        TicketTier(
            id = "tier_corp",
            name = "Corporate group",
            priceInr = 2000,
            remaining = 160,
            total = 400,
            perks = "10–50 seats · Zero-fee team allocation · GST invoice",
            isGroupTier = true,
            minQty = 10,
            maxQty = 50
        )
    )

    val events: List<TruEvent> = listOf(
        TruEvent(
            id = "evt_sunburn_blr",
            title = "Echoes of Earth: Nocturne Live",
            subtitle = "Main Stage Electronic & Visual Arts",
            category = EventCategory.CONCERTS,
            city = CityOption.BENGALURU,
            venue = "Embassy International Riding School, Bengaluru",
            dateLabel = "Sat, 21 Nov",
            timeLabel = "18:30 IST",
            organizer = "Echoes Live India",
            isVerifiedOrganizer = true,
            imageResId = R.drawable.img_event_concert_1794660145289,
            resaleCapBps = 11000,
            groupPolicySummary = "Corporate group blocks (10–50 passes) lock face value at ₹2,000. Team claims are always free.",
            resalePolicySummary = "Capped at 110% of face value (11000 bps). Protocol fee 3% on resale.",
            soldPercent = 88,
            tiers = defaultTiers,
            isFeatured = true,
            isTrending = true,
            scheduleTitle = "Live Lineup Schedule",
            scheduleSubtitle = "Continuous sets across two synchronized stages. Time-stamped artist progression.",
            scheduleSlots = listOf(
                EventScheduleSlot("18:00", "Karan Kanchan", "Main Arena"),
                EventScheduleSlot("19:30", "Lost Stories", "Main Arena"),
                EventScheduleSlot("21:00", "NUCLEYA LIVE", "Headliner", isHighlight = true)
            ),
            venueGuidelines = listOf(
                "Digital dynamic barcode only. Static screenshots will fail gate scanner.",
                "Zero resale scalping margin: transfer unlocked only through TruTicket app.",
                "Dedicated shuttle points available from Yelahanka & Nagawara Metro."
            ),
            venueLocality = "Tarahunise, North Bengaluru",
            venuePinLabel = "Embassy Riding Arena"
        ),
        TruEvent(
            id = "evt_cricket_bom",
            title = "Mumbai vs Chennai Night T20",
            subtitle = "Wankhede Floodlit Derby",
            category = EventCategory.CRICKET,
            city = CityOption.MUMBAI,
            venue = "Wankhede Stadium, Churchgate, Mumbai",
            dateLabel = "Sun, 29 Nov",
            timeLabel = "19:30 IST",
            organizer = "MCA Official Box Office",
            isVerifiedOrganizer = true,
            imageResId = R.drawable.img_event_cricket_1794660146519,
            resaleCapBps = 11000,
            groupPolicySummary = "Corporate hospitality packs allow 10–50 passes with instant phone/email claim.",
            resalePolicySummary = "Strict 110% cap protects fans from scalping. Max ₹1,650 for General.",
            soldPercent = 94,
            tiers = defaultTiers,
            isFeatured = false,
            isTrending = true,
            scheduleTitle = "Matchday Gate Schedule",
            scheduleSubtitle = "Gates open 2 hours prior to first ball. Express Ticket DNA lane at North Stand.",
            scheduleSlots = listOf(
                EventScheduleSlot("17:30", "Gates & Fan Zone Open", "All Gates"),
                EventScheduleSlot("19:00", "Captains' Toss & Warmup", "Center Pitch"),
                EventScheduleSlot("19:30", "First Ball · Innings 1", "Live Match", isHighlight = true)
            ),
            venueGuidelines = listOf(
                "Dynamic QR refreshes every 10 seconds; keep brightness above 70% at turnstiles.",
                "Re-entry is strictly prohibited once your pass is scanned at Wankhede gates.",
                "2-minute walk from Churchgate Western Railway & Aqua Line Metro."
            ),
            venueLocality = "D Rd, Churchgate",
            venuePinLabel = "Wankhede Stadium"
        ),
        TruEvent(
            id = "evt_comedy_del",
            title = "Late Night Basement: 5 Comics",
            subtitle = "Uncensored Stand-Up Showcase",
            category = EventCategory.STAND_UP,
            city = CityOption.DELHI,
            venue = "Habitat Studio, Hauz Khas, Delhi",
            dateLabel = "Fri, 27 Nov",
            timeLabel = "21:00 IST",
            organizer = "Canvas & Mic Collective",
            isVerifiedOrganizer = true,
            imageResId = R.drawable.img_event_comedy_1794660146098,
            resaleCapBps = 11000,
            groupPolicySummary = "Studio team outings (10–50 seats) can be reassigned before doors open.",
            resalePolicySummary = "Resale capped at 110% face value. Screenshots disabled via 10s Ticket DNA.",
            soldPercent = 76,
            tiers = defaultTiers,
            isFeatured = false,
            isTrending = true,
            scheduleTitle = "Showtime Lineup",
            scheduleSubtitle = "Intimate 180-seat basement room. Doors close promptly at 21:05 IST.",
            scheduleSlots = listOf(
                EventScheduleSlot("20:30", "Doors & Seating Check-in", "Studio Entry"),
                EventScheduleSlot("21:00", "Opening 4 Comics", "Main Mic"),
                EventScheduleSlot("22:15", "Surprise Headliner Set", "Headliner", isHighlight = true)
            ),
            venueGuidelines = listOf(
                "No flash photography or audio recording inside the studio room.",
                "Ticket DNA check-in closes 15 minutes after curtain-up for late arrivals.",
                "Nearest exit: Hauz Khas Metro Gate 3 (Yellow & Magenta Lines)."
            ),
            venueLocality = "Hauz Khas Village, South Delhi",
            venuePinLabel = "Habitat Studio"
        ),
        TruEvent(
            id = "evt_tech_blr",
            title = "Builders & AI Infra Summit",
            subtitle = "Keynotes, Systems Track & Demo Alley",
            category = EventCategory.TECH_SUMMITS,
            city = CityOption.BENGALURU,
            venue = "KTPO Convention Centre, Whitefield, Bengaluru",
            dateLabel = "Thu, 03 Dec",
            timeLabel = "09:30 IST",
            organizer = "Deccan Tech Foundation",
            isVerifiedOrganizer = true,
            imageResId = R.drawable.img_event_tech_1794660145974,
            resaleCapBps = 11000,
            groupPolicySummary = "Engineering team passes (10–50) include GST invoice and free seat swaps.",
            resalePolicySummary = "Capped at 110% face value. Non-transferable within 2 hours of keynote.",
            soldPercent = 71,
            tiers = defaultTiers,
            isFeatured = false,
            isTrending = false,
            scheduleTitle = "Keynote & Track Schedule",
            scheduleSubtitle = "Badge printing via instant Ticket DNA scan at Hall A registration kiosks.",
            scheduleSlots = listOf(
                EventScheduleSlot("09:30", "Systems Keynote & Coffee", "Hall A"),
                EventScheduleSlot("11:30", "Inference & Edge Runtime", "Track 1"),
                EventScheduleSlot("14:30", "Founders Fireside & Demos", "Main Stage", isHighlight = true)
            ),
            venueGuidelines = listOf(
                "Corporate group passes can be swapped freely in Group Manager until doors open.",
                "Bring government ID matching attendee profile for physical lanyard pickup.",
                "Purple Line Metro to Hoodi / Pattandur Agrahara + EV feeder shuttle."
            ),
            venueLocality = "EPIP Zone, Whitefield",
            venuePinLabel = "KTPO Convention Centre"
        ),
        TruEvent(
            id = "evt_fest_bom",
            title = "Mood Indigo Pro-Nite Finale",
            subtitle = "IIT Bombay Annual Cultural Fest",
            category = EventCategory.COLLEGE_FESTS,
            city = CityOption.MUMBAI,
            venue = "OAT Convocation Grounds, Powai, Mumbai",
            dateLabel = "Sat, 12 Dec",
            timeLabel = "19:00 IST",
            organizer = "IITB Cultural Council",
            isVerifiedOrganizer = true,
            imageResId = R.drawable.img_event_concert_1794660145289,
            resaleCapBps = 11000,
            groupPolicySummary = "Alumni & campus sponsor blocks (10–50) managed in Group Manager.",
            resalePolicySummary = "Student & General passes capped at 110% face value.",
            soldPercent = 82,
            tiers = defaultTiers,
            isFeatured = false,
            isTrending = true,
            scheduleTitle = "Pro-Nite Stage Schedule",
            scheduleSubtitle = "Campus main gate verification opens 17:30 IST. Carry student ID for Student tier.",
            scheduleSlots = listOf(
                EventScheduleSlot("18:30", "Indie Band Finals", "OAT Stage"),
                EventScheduleSlot("19:45", "When Chai Met Toast", "OAT Stage"),
                EventScheduleSlot("21:15", "Amit Trivedi Collective", "Headliner", isHighlight = true)
            ),
            venueGuidelines = listOf(
                "Student tier requires valid college photo ID alongside live Ticket DNA QR.",
                "Zero outside food or large backpacks permitted past Powai Main Gate.",
                "Campus shuttle runs every 10 mins from Kanjurmarg & Vikhroli stations."
            ),
            venueLocality = "IIT Area, Powai",
            venuePinLabel = "OAT Convocation Grounds"
        )
    )

    val initialTickets: List<TicketItem> = listOf(
        TicketItem(
            ticketRef = "TT-BLR-8841",
            eventId = "evt_sunburn_blr",
            eventTitle = "Echoes of Earth: Nocturne Live",
            venue = "Embassy Riding School, Bengaluru",
            city = "Bengaluru",
            dateLabel = "Sat, 21 Nov",
            timeLabel = "18:30 IST",
            tierName = "VIP",
            faceValueInr = 4500,
            status = TicketStatus.UPCOMING,
            transferCount = 0,
            seatLabel = "DECK-B · ROW 2",
            history = listOf(
                TicketHistoryEntry("Ticket DNA issued", "14 Nov · 10:14", "Mint proof 9f3a…c21d"),
                TicketHistoryEntry("FairQueue slot cleared", "14 Nov · 10:12", "Queue seed 4a1e…89b0"),
                TicketHistoryEntry("Order paid via UPI", "14 Nov · 10:13", "Razorpay test ref")
            )
        ),
        TicketItem(
            ticketRef = "TT-DEL-3910",
            eventId = "evt_comedy_del",
            eventTitle = "Late Night Basement: 5 Comics",
            venue = "Habitat Studio, Hauz Khas, Delhi",
            city = "Delhi",
            dateLabel = "Fri, 27 Nov",
            timeLabel = "21:00 IST",
            tierName = "General",
            faceValueInr = 1500,
            status = TicketStatus.UPCOMING,
            transferCount = 1,
            seatLabel = "FLOOR-A · FREE SEATING",
            history = listOf(
                TicketHistoryEntry("Transferred via TruResale", "13 Nov · 19:40", "Within 110% cap (₹1,620)"),
                TicketHistoryEntry("Ticket DNA issued", "09 Nov · 14:05", "Mint proof 7c12…a904")
            )
        ),
        TicketItem(
            ticketRef = "TT-BOM-2049",
            eventId = "evt_cricket_bom",
            eventTitle = "Mumbai vs Chennai Night T20",
            venue = "Wankhede Stadium, Mumbai",
            city = "Mumbai",
            dateLabel = "Sun, 29 Nov",
            timeLabel = "19:30 IST",
            tierName = "General",
            faceValueInr = 1500,
            status = TicketStatus.LISTED,
            transferCount = 0,
            seatLabel = "NORTH STAND · L2",
            listedAskInr = 1600,
            history = listOf(
                TicketHistoryEntry("Listed on TruResale", "14 Nov · 09:00", "Ask ₹1,600 (cap ₹1,650)"),
                TicketHistoryEntry("Ticket DNA issued", "10 Nov · 11:22", "Mint proof 2d81…e41a")
            )
        ),
        TicketItem(
            ticketRef = "TT-BLR-7704",
            eventId = "evt_tech_blr",
            eventTitle = "Builders & AI Infra Summit",
            venue = "KTPO Whitefield, Bengaluru",
            city = "Bengaluru",
            dateLabel = "Thu, 03 Dec",
            timeLabel = "09:30 IST",
            tierName = "Corporate group",
            faceValueInr = 2000,
            status = TicketStatus.UNASSIGNED_GROUP,
            transferCount = 0,
            seatLabel = "TEAM BLOCK · 4 SEATS LEFT",
            history = listOf(
                TicketHistoryEntry("Corporate batch issued", "12 Nov · 16:30", "12 passes · ₹2,000 each")
            )
        ),
        TicketItem(
            ticketRef = "TT-BLR-1102",
            eventId = "evt_sunburn_blr",
            eventTitle = "Indiranagar Indie Sessions",
            venue = "Fandom at Gilly's, Bengaluru",
            city = "Bengaluru",
            dateLabel = "Sun, 18 Oct",
            timeLabel = "20:00 IST",
            tierName = "General",
            faceValueInr = 1500,
            status = TicketStatus.PAST,
            transferCount = 0,
            seatLabel = "MAIN FLOOR",
            history = listOf(
                TicketHistoryEntry("Scanned at Gate 2", "18 Oct · 19:48", "Verified by Gate Staff"),
                TicketHistoryEntry("Ticket DNA issued", "02 Oct · 12:10", "Mint proof 5e44…91bc")
            )
        ),
        TicketItem(
            ticketRef = "TT-EXP-9001",
            eventId = "evt_sunburn_blr",
            eventTitle = "Echoes of Earth: Nocturne Live",
            venue = "Embassy Riding School, Bengaluru",
            city = "Bengaluru",
            dateLabel = "Sat, 21 Nov",
            timeLabel = "18:30 IST",
            tierName = "Student",
            faceValueInr = 800,
            status = TicketStatus.EXPIRED,
            transferCount = 0,
            seatLabel = "GATE C",
            history = listOf(
                TicketHistoryEntry("Dynamic QR window expired", "14 Nov · 08:00", "Refresh required")
            )
        ),
        TicketItem(
            ticketRef = "TT-REV-4410",
            eventId = "evt_sunburn_blr",
            eventTitle = "Echoes of Earth: Nocturne Live",
            venue = "Embassy Riding School, Bengaluru",
            city = "Bengaluru",
            dateLabel = "Sat, 21 Nov",
            timeLabel = "18:30 IST",
            tierName = "VIP",
            faceValueInr = 4500,
            status = TicketStatus.REVOKED,
            transferCount = 0,
            seatLabel = "DECK-A",
            history = listOf(
                TicketHistoryEntry("Revoked by Group Manager", "13 Nov · 17:15", "Seat re-allocated")
            )
        ),
        TicketItem(
            ticketRef = "TT-WRG-5520",
            eventId = "evt_cricket_bom",
            eventTitle = "Mumbai vs Chennai Night T20",
            venue = "Wankhede Stadium, Mumbai",
            city = "Mumbai",
            dateLabel = "Sun, 29 Nov",
            timeLabel = "19:30 IST",
            tierName = "General",
            faceValueInr = 1500,
            status = TicketStatus.UPCOMING,
            transferCount = 0,
            seatLabel = "EAST STAND",
            isWrongEventForGate = true,
            history = listOf(
                TicketHistoryEntry("Ticket DNA issued", "11 Nov · 15:00", "Valid for Mumbai only")
            )
        )
    )

    val initialGroupAllocations: List<GroupMemberAllocation> = listOf(
        GroupMemberAllocation(
            id = "grp_1",
            ticketRef = "TT-GRP-7701",
            eventTitle = "Builders & AI Infra Summit",
            tierName = "Corporate group",
            faceValueInr = 2000,
            contact = "ananya.rao@deccantech.in",
            status = AllocationStatus.CLAIMED,
            updatedAt = "Claimed 12 Nov · 18:10"
        ),
        GroupMemberAllocation(
            id = "grp_2",
            ticketRef = "TT-GRP-7702",
            eventTitle = "Builders & AI Infra Summit",
            tierName = "Corporate group",
            faceValueInr = 2000,
            contact = "+91 98450 21834",
            status = AllocationStatus.PENDING_CLAIM,
            updatedAt = "Invited 13 Nov · 11:20"
        ),
        GroupMemberAllocation(
            id = "grp_3",
            ticketRef = "TT-GRP-7703",
            eventTitle = "Builders & AI Infra Summit",
            tierName = "Corporate group",
            faceValueInr = 2000,
            contact = "rohan.mehta@deccantech.in",
            status = AllocationStatus.PENDING_CLAIM,
            updatedAt = "Invited 14 Nov · 08:45"
        ),
        GroupMemberAllocation(
            id = "grp_4",
            ticketRef = "TT-GRP-7704",
            eventTitle = "Builders & AI Infra Summit",
            tierName = "Corporate group",
            faceValueInr = 2000,
            contact = null,
            status = AllocationStatus.UNASSIGNED,
            updatedAt = "Available to assign"
        ),
        GroupMemberAllocation(
            id = "grp_5",
            ticketRef = "TT-GRP-7705",
            eventTitle = "Builders & AI Infra Summit",
            tierName = "Corporate group",
            faceValueInr = 2000,
            contact = null,
            status = AllocationStatus.UNASSIGNED,
            updatedAt = "Available to assign"
        )
    )

    val initialResaleListings: List<ResaleListing> = listOf(
        ResaleListing(
            id = "res_1",
            ticketRef = "TT-BLR-6102",
            eventTitle = "Echoes of Earth: Nocturne Live",
            city = "Bengaluru",
            dateLabel = "Sat, 21 Nov",
            tierName = "General · Standing Floor",
            faceValueInr = 1500,
            askPriceInr = 1590,
            capBps = 11000,
            category = EventCategory.CONCERTS,
            venue = "Embassy Riding School",
            imageResId = R.drawable.img_event_concert_1794660145289,
            transferCount = 0
        ),
        ResaleListing(
            id = "res_2",
            ticketRef = "TT-DEL-3307",
            eventTitle = "Late Night Basement: 5 Comics",
            city = "Delhi",
            dateLabel = "Fri, 27 Nov",
            tierName = "Student · Tier 1 Seated",
            faceValueInr = 800,
            askPriceInr = 800,
            capBps = 11000,
            category = EventCategory.STAND_UP,
            venue = "Habitat Studio, Hauz Khas",
            imageResId = R.drawable.img_event_comedy_1794660146098,
            transferCount = 0
        ),
        ResaleListing(
            id = "res_3",
            ticketRef = "TT-BOM-4918",
            eventTitle = "Mumbai vs Chennai Night T20",
            city = "Mumbai",
            dateLabel = "Sun, 29 Nov",
            tierName = "VIP · North Hospitality",
            faceValueInr = 4500,
            askPriceInr = 4950,
            capBps = 11000,
            category = EventCategory.CRICKET,
            venue = "Wankhede Stadium",
            imageResId = R.drawable.img_event_cricket_1794660146519,
            transferCount = 1
        )
    )

    val initialSessions: List<ActiveSession> = listOf(
        ActiveSession("ses_1", "Pixel 8 Pro · Bengaluru", "Bengaluru, IN", "Active now", true),
        ActiveSession("ses_2", "Chrome macOS · Work", "Bengaluru, IN", "Yesterday · 21:14", false),
        ActiveSession("ses_3", "iPad Air · Home", "Mumbai, IN", "11 Nov · 16:02", false)
    )

    val initialWaitlistClaims: List<WaitlistClaim> = listOf(
        WaitlistClaim(
            id = "wl_1",
            eventId = "evt_cricket_bom",
            eventTitle = "Mumbai vs Chennai Night T20",
            tierName = "VIP",
            priceInr = 4500,
            secondsRemaining = 412
        )
    )

    val initialGateLogs: List<GateScanRecord> = listOf(
        GateScanRecord(
            id = "scan_init_1",
            ticketRef = "TT-BLR-5120",
            tierName = "General",
            verdict = ScanVerdict.VALID,
            timestampLabel = "18:12:04"
        ),
        GateScanRecord(
            id = "scan_init_2",
            ticketRef = "TT-BLR-4998",
            tierName = "VIP",
            verdict = ScanVerdict.ALREADY_USED,
            timestampLabel = "18:14:29",
            firstScanTimeLabel = "18:02:11"
        )
    )
}
