package com.example.ui.navigation

object TruRoutes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val CUSTOMER_TABS = "customer_tabs"
    const val EXPLORE = "explore"
    const val EVENT_DETAIL = "event_detail"
    const val FAIR_QUEUE = "fair_queue"
    const val TICKET_SELECTION = "ticket_selection"
    const val CHECKOUT = "checkout"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val MY_TICKETS = "tickets"
    const val TICKET_DNA = "ticket_dna"
    const val GROUP_MANAGER = "group_manager"
    const val VERIFIER = "verifier"
    const val RESALE = "resale"
    const val PROFILE = "profile"

    // Gate Staff stack (no bottom tabs)
    const val GATE_SCANNER = "gate_scanner"
    const val GATE_RESULT = "gate_result"
    const val GATE_LOG = "gate_log"
    const val GATE_LOOKUP = "gate_lookup"

    val bottomBarRoutes = setOf(EXPLORE, MY_TICKETS, RESALE, PROFILE)
}
