package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.PerforatedDivider
import com.example.ui.components.TicketStubSurface
import com.example.ui.components.TruPrimaryButton
import com.example.ui.components.TruStatusBadge
import com.example.ui.components.TruStatusTone
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val badge: String,
    val title: String,
    val body: String,
    val metricLabel: String,
    val metricValue: String,
    val icon: ImageVector,
    val tone: TruStatusTone
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(900L)
        showSplash = false
    }

    val slides = remember {
        listOf(
            OnboardingSlide(
                badge = "01 · FAIRQUEUE",
                title = "Bots never cut the line.",
                body = "Every drop opens a verifiable cryptographic lottery. Seed commitments prove your queue turn wasn't rigged.",
                metricLabel = "BOT MITIGATION",
                metricValue = "SHA-256 SEED",
                icon = Icons.Filled.Shield,
                tone = TruStatusTone.SUCCESS
            ),
            OnboardingSlide(
                badge = "02 · TICKET DNA",
                title = "Live rotating QR passes.",
                body = "Your ticket is a living pass that refreshes its encrypted QR nonce every 10 seconds. Screenshots expire immediately.",
                metricLabel = "REFRESH CADENCE",
                metricValue = "10 SEC",
                icon = Icons.Filled.QrCode2,
                tone = TruStatusTone.INFO
            ),
            OnboardingSlide(
                badge = "03 · CAPPED RESALE",
                title = "Zero scalper markups.",
                body = "Resell spare seats safely at up to 110% of face value. Instant escrow settlement with verified authenticity.",
                metricLabel = "MAX RESALE CAP",
                metricValue = "110% BPS",
                icon = Icons.Filled.SwapHoriz,
                tone = TruStatusTone.WARNING
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .systemBarsPadding()
    ) {
        if (showSplash) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(TruLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ConfirmationNumber,
                        contentDescription = "TruTicket emblem",
                        tint = TruBg,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "TruTicket",
                    style = MaterialTheme.typography.displayLarge,
                    color = TruTextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Fair access. Genuine tickets. Controlled resale.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TruTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            AnimatedVisibility(visible = !showSplash, enter = fadeIn()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(onClick = {}, onLongClick = onLogoLongPress)
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TruLime),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ConfirmationNumber, null, tint = TruBg, modifier = Modifier.size(16.dp))
                            }
                            Text("TruTicket", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
                        }
                        TextButton(
                            onClick = onFinished,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("onboarding_skip_button")
                        ) {
                            Text("Skip", color = TruTextMuted, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        val slide = slides[page]
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            TicketStubSurface(
                                cornerRadius = 28.dp,
                                borderColor = TruBorder
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(TruViolet.copy(alpha = 0.24f), TruSurfaceElevated)
                                            )
                                        )
                                        .padding(24.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = slide.badge,
                                                style = MonoSmallStyle,
                                                color = TruLime
                                            )
                                            TruStatusBadge(
                                                text = "Verified protocol",
                                                tone = slide.tone,
                                                icon = slide.icon
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(TruLime.copy(alpha = 0.14f))
                                                .border(1.dp, TruLime.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = slide.icon,
                                                contentDescription = slide.title,
                                                tint = TruLime,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Text(
                                            text = slide.title,
                                            style = MaterialTheme.typography.displayLarge,
                                            color = TruTextPrimary
                                        )
                                        Text(
                                            text = slide.body,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TruTextMuted
                                        )
                                    }
                                }
                                PerforatedDivider()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = slide.metricLabel,
                                            style = MonoSmallStyle,
                                            color = TruTextMuted
                                        )
                                        Text(
                                            text = slide.metricValue,
                                            style = MonoLargeStyle,
                                            color = TruLime
                                        )
                                    }
                                    Text(
                                        text = "SLIDE ${page + 1} / ${slides.size}",
                                        style = MonoSmallStyle,
                                        color = TruTextMuted
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(slides.size) { index ->
                                val active = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(6.dp)
                                        .width(if (active) 28.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(if (active) TruLime else TruBorder)
                                )
                            }
                        }
                        if (pagerState.currentPage < slides.lastIndex) {
                            TruPrimaryButton(
                                text = "Next",
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                testTag = "onboarding_next_button"
                            )
                        } else {
                            TruPrimaryButton(
                                text = "Get started",
                                onClick = onFinished,
                                testTag = "onboarding_get_started_button"
                            )
                        }
                    }
                }
            }
        }
    }
}
