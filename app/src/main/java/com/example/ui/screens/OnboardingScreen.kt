package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.TruGlassSurface
import com.example.ui.components.TruMascotIllustration
import com.example.ui.components.TruPrimaryButton
import com.example.ui.components.WelcomeGlyphKind
import com.example.ui.components.WelcomeGridBackdrop
import com.example.ui.components.WelcomePixelGlyphIcon
import com.example.ui.components.WelcomePixelSparkleCluster
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.MonoSmallStyle
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TruBg
import com.example.ui.theme.TruGlassLimeRim
import com.example.ui.theme.TruGlassSurface
import com.example.ui.theme.TruGlassWhiteRim
import com.example.ui.theme.TruLime
import com.example.ui.theme.TruTextMuted
import com.example.ui.theme.TruTextPrimary

private enum class WelcomePillar {
    FAIR_QUEUE,
    TICKET_DNA,
    RESALE_CAP,
    VERIFIED
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    var contentVisible by remember { mutableStateOf(false) }
    var selectedPillar by remember { mutableStateOf<WelcomePillar?>(null) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    val summaryText = when (selectedPillar) {
        WelcomePillar.FAIR_QUEUE -> stringResource(R.string.welcome_detail_fairqueue)
        WelcomePillar.TICKET_DNA -> stringResource(R.string.welcome_detail_ticket_dna)
        WelcomePillar.RESALE_CAP -> stringResource(R.string.welcome_detail_resale_cap)
        WelcomePillar.VERIFIED -> stringResource(R.string.welcome_detail_verified)
        null -> stringResource(R.string.welcome_summary_default)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TruBg)
    ) {
        WelcomeGridBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar preserving brand mark + long-press role switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .combinedClickable(
                            onClick = { selectedPillar = null },
                            onLongClick = onLogoLongPress
                        )
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .testTag("truticket_logo_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(TruLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ConfirmationNumber,
                            contentDescription = stringResource(R.string.welcome_logo_description),
                            tint = TruBg,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = TruTextPrimary
                    )
                }

                TruGlassSurface(
                    shape = CircleShape,
                    rimHighlightColor = TruGlassWhiteRim,
                    rimGlowColor = TruLime.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_domain_tag),
                        style = MonoSmallStyle,
                        color = TruTextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Scroll-safe poster hero stage
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 16 },
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.welcome_eyebrow),
                        style = MonoSmallStyle,
                        color = TruTextMuted
                    )
                    Spacer(Modifier.height(10.dp))

                    // Two-tone stacked headline + pixel sparkle accent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.welcome_headline_prefix),
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp,
                                lineHeight = 42.sp,
                                letterSpacing = (-1.1).sp,
                                color = TruTextPrimary
                            )
                            Text(
                                text = stringResource(R.string.welcome_headline_brand),
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                lineHeight = 44.sp,
                                letterSpacing = (-1.1).sp,
                                color = TruLime
                            )
                        }
                        WelcomePixelSparkleCluster(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(48.dp)
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    // Split stage: Left pill matrix + Right spiky neon-lime mascot
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val compactWidth = maxWidth < 330.dp
                        val mascotWidth = if (compactWidth) 118.dp else 138.dp
                        val mascotHeight = if (compactWidth) 204.dp else 232.dp

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Row 1: [->] [FairQueue]
                                WelcomePillMatrixRow(
                                    selected = selectedPillar == WelcomePillar.FAIR_QUEUE,
                                    onClick = {
                                        selectedPillar = if (selectedPillar == WelcomePillar.FAIR_QUEUE) null else WelcomePillar.FAIR_QUEUE
                                    },
                                    testTag = "welcome_pill_fairqueue"
                                ) {
                                    WelcomeLimeGlyphPill(
                                        kind = WelcomeGlyphKind.RIGHT_ARROW,
                                        highlighted = selectedPillar == WelcomePillar.FAIR_QUEUE
                                    )
                                    WelcomeWordPill(
                                        text = stringResource(R.string.welcome_pill_fairqueue),
                                        containerColor = TruTextPrimary,
                                        highlighted = selectedPillar == WelcomePillar.FAIR_QUEUE
                                    )
                                }

                                // Row 2: [Ticket DNA] [Live]
                                WelcomePillMatrixRow(
                                    selected = selectedPillar == WelcomePillar.TICKET_DNA,
                                    onClick = {
                                        selectedPillar = if (selectedPillar == WelcomePillar.TICKET_DNA) null else WelcomePillar.TICKET_DNA
                                    },
                                    testTag = "welcome_pill_ticket_dna"
                                ) {
                                    WelcomeWordPill(
                                        text = stringResource(R.string.welcome_pill_ticket_dna),
                                        containerColor = Color(0xFFE9FCE5),
                                        highlighted = selectedPillar == WelcomePillar.TICKET_DNA
                                    )
                                    WelcomeWordPill(
                                        text = stringResource(R.string.welcome_pill_live),
                                        containerColor = TruTextPrimary,
                                        highlighted = selectedPillar == WelcomePillar.TICKET_DNA
                                    )
                                }

                                // Row 3: [v] [110% Cap]
                                WelcomePillMatrixRow(
                                    selected = selectedPillar == WelcomePillar.RESALE_CAP,
                                    onClick = {
                                        selectedPillar = if (selectedPillar == WelcomePillar.RESALE_CAP) null else WelcomePillar.RESALE_CAP
                                    },
                                    testTag = "welcome_pill_resale_cap"
                                ) {
                                    WelcomeLimeGlyphPill(
                                        kind = WelcomeGlyphKind.DOWN_ARROW,
                                        highlighted = selectedPillar == WelcomePillar.RESALE_CAP
                                    )
                                    WelcomeWordPill(
                                        text = stringResource(R.string.welcome_pill_resale_cap),
                                        containerColor = TruTextPrimary,
                                        highlighted = selectedPillar == WelcomePillar.RESALE_CAP
                                    )
                                }

                                // Row 4: [Verified] [*]
                                WelcomePillMatrixRow(
                                    selected = selectedPillar == WelcomePillar.VERIFIED,
                                    onClick = {
                                        selectedPillar = if (selectedPillar == WelcomePillar.VERIFIED) null else WelcomePillar.VERIFIED
                                    },
                                    testTag = "welcome_pill_verified"
                                ) {
                                    WelcomeWordPill(
                                        text = stringResource(R.string.welcome_pill_verified),
                                        containerColor = TruTextPrimary,
                                        highlighted = selectedPillar == WelcomePillar.VERIFIED
                                    )
                                    WelcomeLimeGlyphPill(
                                        kind = WelcomeGlyphKind.STARBURST,
                                        highlighted = selectedPillar == WelcomePillar.VERIFIED
                                    )
                                }
                            }

                            TruMascotIllustration(
                                contentDescriptionText = stringResource(R.string.welcome_mascot_description),
                                modifier = Modifier
                                    .width(mascotWidth)
                                    .height(mascotHeight)
                            )
                        }
                    }
                }
            }

            // Bottom summary strip + primary CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TruGlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    rimHighlightColor = if (selectedPillar != null) TruGlassLimeRim else TruGlassWhiteRim,
                    rimGlowColor = if (selectedPillar != null) TruLime.copy(alpha = 0.45f) else TruLime.copy(alpha = 0.24f),
                    fillMaxWidthContent = true
                ) {
                    AnimatedContent(
                        targetState = summaryText,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "welcome_summary_copy"
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedPillar != null) TruTextPrimary else TruTextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TruPrimaryButton(
                    text = stringResource(R.string.welcome_cta_get_started),
                    onClick = onFinished,
                    testTag = "onboarding_get_started_button",
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward
                )
            }
        }
    }
}

@Composable
private fun WelcomePillMatrixRow(
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else if (selected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_row_scale"
    )
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = TruLime.copy(alpha = 0.28f)),
                onClick = onClick
            )
            .padding(vertical = 2.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun WelcomeLimeGlyphPill(
    kind: WelcomeGlyphKind,
    highlighted: Boolean
) {
    Surface(
        shape = CircleShape,
        color = TruLime,
        border = if (highlighted) BorderStroke(2.dp, TruTextPrimary) else null,
        modifier = Modifier.size(width = 48.dp, height = 42.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            WelcomePixelGlyphIcon(
                kind = kind,
                tint = TruBg,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WelcomeWordPill(
    text: String,
    containerColor: Color,
    highlighted: Boolean
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = if (highlighted) BorderStroke(2.dp, TruLime) else null,
        modifier = Modifier.height(42.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 15.dp)
        ) {
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TruBg,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
