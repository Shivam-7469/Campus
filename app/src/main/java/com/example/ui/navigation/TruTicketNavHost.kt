package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.UserRole
import com.example.ui.components.RoleSwitcherBottomSheet
import com.example.ui.components.TruAmbientDarkBackdrop
import com.example.ui.components.TruBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.TruBg
import com.example.ui.theme.TruLime
import com.example.ui.theme.TruSurfaceElevated
import com.example.ui.theme.TruTextPrimary
import com.example.viewmodel.TruTicketUiState
import com.example.viewmodel.TruTicketViewModel

@Composable
fun TruTicketApp(
    viewModel: TruTicketViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: TruRoutes.ONBOARDING
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }

    val onSwitchRole: (UserRole) -> Unit = { role ->
        viewModel.selectRole(role)
        if (role == UserRole.GATE_STAFF) {
            navController.navigate(TruRoutes.GATE_SCANNER) { popUpTo(0) { inclusive = true } }
        } else if (currentRoute.startsWith("gate_")) {
            viewModel.selectCustomerTab(TruRoutes.EXPLORE)
            navController.navigate(TruRoutes.CUSTOMER_TABS) { popUpTo(0) { inclusive = true } }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(TruBg),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            modifier = Modifier.widthIn(max = 430.dp).fillMaxSize(),
            containerColor = TruBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = TruSurfaceElevated,
                        contentColor = TruTextPrimary,
                        actionColor = TruLime
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TruRoutes.ONBOARDING,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(240)) + fadeIn(tween(180))
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(240)) + fadeOut(tween(150))
                },
                popEnterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(240)) + fadeIn(tween(180))
                },
                popExitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(240)) + fadeOut(tween(150))
                }
            ) {
                composable(TruRoutes.ONBOARDING) {
                    OnboardingScreen(
                        onFinished = {
                            navController.navigate(TruRoutes.LOGIN) {
                                popUpTo(TruRoutes.ONBOARDING) { inclusive = true }
                            }
                        },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.LOGIN) {
                    LoginScreen(
                        selectedRole = state.userRole,
                        onSelectRole = { viewModel.selectRole(it) },
                        onLoginSuccess = {
                            val target = if (state.userRole == UserRole.GATE_STAFF) TruRoutes.GATE_SCANNER else TruRoutes.CUSTOMER_TABS
                            navController.navigate(target) { popUpTo(TruRoutes.LOGIN) { inclusive = true } }
                        },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.CUSTOMER_TABS) {
                    CustomerTabsShell(
                        state = state,
                        viewModel = viewModel,
                        onSwitchRole = onSwitchRole,
                        onOpenEvent = { eventId ->
                            viewModel.openEvent(eventId)
                            navController.navigate(TruRoutes.EVENT_DETAIL)
                        },
                        onOpenVerifier = { navController.navigate(TruRoutes.VERIFIER) },
                        onOpenTicketDna = { ref ->
                            viewModel.selectTicket(ref)
                            navController.navigate(TruRoutes.TICKET_DNA)
                        },
                        onOpenGroupManager = { navController.navigate(TruRoutes.GROUP_MANAGER) },
                        onBuyResaleSuccess = { navController.navigate(TruRoutes.TICKET_DNA) },
                        onClaimWaitlist = { eventId ->
                            viewModel.openEvent(eventId)
                            viewModel.startFairQueue()
                            navController.navigate(TruRoutes.FAIR_QUEUE)
                        },
                        onLogout = {
                            navController.navigate(TruRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }

                composable(TruRoutes.EVENT_DETAIL) {
                    EventDetailScreen(
                        event = state.selectedEvent,
                        onBack = { navController.popBackStack() },
                        onJoinFairQueue = {
                            viewModel.startFairQueue()
                            navController.navigate(TruRoutes.FAIR_QUEUE)
                        }
                    )
                }

                composable(TruRoutes.FAIR_QUEUE) {
                    FairQueueScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onQueueComplete = {
                            navController.navigate(TruRoutes.TICKET_SELECTION) {
                                popUpTo(TruRoutes.FAIR_QUEUE) { inclusive = true }
                            }
                        },
                        onFastForward = viewModel::fastForwardQueue,
                        onCopyHash = viewModel::showMessage,
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.TICKET_SELECTION) {
                    TicketSelectionScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onSelectTier = viewModel::selectTier,
                        onAdjustQty = viewModel::adjustQty,
                        onProceedToCheckout = { navController.navigate(TruRoutes.CHECKOUT) },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.CHECKOUT) {
                    val (base, fee, tax) = viewModel.calculateOrderTotals()
                    CheckoutScreen(
                        state = state,
                        totalInr = base + fee + tax,
                        onBack = { navController.popBackStack() },
                        onSelectMethod = viewModel::setPaymentMethod,
                        onToggleSimulateFailure = viewModel::setSimulateFailure,
                        onPayNow = viewModel::submitPayment,
                        onPaymentSuccess = {
                            navController.navigate(TruRoutes.ORDER_CONFIRMATION) {
                                popUpTo(TruRoutes.EVENT_DETAIL) { inclusive = false }
                            }
                        },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.ORDER_CONFIRMATION) {
                    OrderConfirmationScreen(
                        state = state,
                        onViewInWallet = {
                            viewModel.selectCustomerTab(TruRoutes.MY_TICKETS)
                            navController.popBackStack(TruRoutes.CUSTOMER_TABS, inclusive = false)
                        },
                        onAllocateToTeam = { navController.navigate(TruRoutes.GROUP_MANAGER) }
                    )
                }

                composable(TruRoutes.TICKET_DNA) {
                    val ticket = state.activeTicket ?: state.tickets.first()
                    TicketDnaScreen(
                        state = state,
                        ticket = ticket,
                        onBack = { navController.popBackStack() },
                        onForceRotateQr = viewModel::forceRotateTicketDna,
                        onListForResale = {
                            viewModel.openResaleInSellMode()
                            navController.popBackStack(TruRoutes.CUSTOMER_TABS, inclusive = false)
                        },
                        onShareVerifier = { ref ->
                            viewModel.showMessage("Verification link copied for $ref")
                            navController.navigate("${TruRoutes.VERIFIER}?ref=$ref")
                        },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.GROUP_MANAGER) {
                    GroupManagerScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onAssignMember = viewModel::assignGroupMember,
                        onRevokePending = viewModel::revokePendingAllocation,
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(
                    route = "${TruRoutes.VERIFIER}?ref={ref}",
                    arguments = listOf(navArgument("ref") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { entry ->
                    VerifierScreen(
                        state = state,
                        initialTicketRef = entry.arguments?.getString("ref"),
                        onBack = { navController.popBackStack() },
                        onVerify = viewModel::verifyTicketRef,
                        onReportScam = viewModel::submitScamReport,
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.GATE_SCANNER) {
                    GateScannerScreen(
                        state = state,
                        onToggleOnline = viewModel::toggleGateOnline,
                        onSimulateScan = { ref ->
                            viewModel.performGateScan(ref)
                            navController.navigate(TruRoutes.GATE_RESULT)
                        },
                        onOpenManualLookup = { navController.navigate(TruRoutes.GATE_LOOKUP) },
                        onOpenActivityLog = { navController.navigate(TruRoutes.GATE_LOG) },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }

                composable(TruRoutes.GATE_RESULT) {
                    GateResultScreen(record = state.lastGateResult, onScanNext = { navController.popBackStack() })
                }

                composable(TruRoutes.GATE_LOG) {
                    GateActivityLogScreen(state = state, onBack = { navController.popBackStack() }, onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) })
                }

                composable(TruRoutes.GATE_LOOKUP) {
                    GateManualLookupScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onRedeem = { ref ->
                            viewModel.performGateScan(ref)
                            navController.navigate(TruRoutes.GATE_RESULT)
                        },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }
            }
        }
    }

    if (state.showRoleSwitcherSheet) {
        RoleSwitcherBottomSheet(
            currentRole = state.userRole,
            onSelectRole = onSwitchRole,
            onDismiss = { viewModel.toggleRoleSwitcherSheet(false) }
        )
    }
}

@Composable
private fun CustomerTabsShell(
    state: TruTicketUiState,
    viewModel: TruTicketViewModel,
    onSwitchRole: (UserRole) -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenVerifier: () -> Unit,
    onOpenTicketDna: (String) -> Unit,
    onOpenGroupManager: () -> Unit,
    onBuyResaleSuccess: () -> Unit,
    onClaimWaitlist: (String) -> Unit,
    onLogout: () -> Unit
) {
    BackHandler(enabled = state.activeCustomerTab != TruRoutes.EXPLORE) {
        viewModel.selectCustomerTab(TruRoutes.EXPLORE)
    }

    val bottomBackdropLayer = rememberGraphicsLayer()
    var backdropOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TruBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { backdropOffset = it.positionInRoot() }
                .drawWithContent {
                    bottomBackdropLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(bottomBackdropLayer)
                }
        ) {
            TruAmbientDarkBackdrop(modifier = Modifier.fillMaxSize())

            Crossfade(
                targetState = state.activeCustomerTab,
                animationSpec = tween(180),
                modifier = Modifier.fillMaxSize(),
                label = "customer_tab_crossfade"
            ) { activeTab ->
                when (activeTab) {
                    TruRoutes.EXPLORE -> ExploreScreen(
                        state = state,
                        onSelectCity = viewModel::setCity,
                        onSelectCategory = viewModel::setCategory,
                        onSearchChange = viewModel::setSearchQuery,
                        onRefreshSkeleton = viewModel::triggerExploreSkeletonRefresh,
                        onOpenEvent = onOpenEvent,
                        onOpenVerifier = onOpenVerifier,
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                    TruRoutes.MY_TICKETS -> MyTicketsScreen(
                        state = state,
                        onOpenTicketDna = onOpenTicketDna,
                        onOpenGroupManager = onOpenGroupManager,
                        onExploreEvents = { viewModel.selectCustomerTab(TruRoutes.EXPLORE) },
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                    TruRoutes.RESALE -> ResaleScreen(
                        state = state,
                        onSetSellMode = viewModel::setResaleSellMode,
                        onBuyListing = { listingId ->
                            viewModel.buyResaleListing(listingId)
                            onBuyResaleSuccess()
                        },
                        onListTicket = viewModel::listTicketForResale,
                        onUpdateListing = viewModel::updateResaleListing,
                        onDelistTicket = viewModel::delistTicket,
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                    TruRoutes.PROFILE -> ProfileScreen(
                        state = state,
                        onSwitchRole = onSwitchRole,
                        onSignOutSession = viewModel::signOutSession,
                        onToggleTwoStep = { viewModel.updateSecurityToggles(twoStep = it) },
                        onToggleDropNotif = { viewModel.updateSecurityToggles(drop = it) },
                        onToggleQueueNotif = { viewModel.updateSecurityToggles(queue = it) },
                        onToggleResaleNotif = { viewModel.updateSecurityToggles(resale = it) },
                        onClaimWaitlist = onClaimWaitlist,
                        onOpenVerifier = onOpenVerifier,
                        onOpenGroupManager = onOpenGroupManager,
                        onLogout = onLogout,
                        onLogoLongPress = { viewModel.toggleRoleSwitcherSheet(true) }
                    )
                }
            }
        }

        TruBottomBar(
            currentRoute = state.activeCustomerTab,
            onSelectTab = viewModel::selectCustomerTab,
            modifier = Modifier.align(Alignment.BottomCenter),
            backdropLayer = bottomBackdropLayer,
            backdropContainerOffset = backdropOffset
        )
    }
}
