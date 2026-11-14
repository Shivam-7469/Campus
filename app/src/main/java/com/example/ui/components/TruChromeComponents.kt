package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.UserRole
import com.example.ui.theme.*

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val TruBottomTabs = listOf(
    BottomNavItem("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomNavItem("tickets", "Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
    BottomNavItem("resale", "Resale", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz),
    BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TruTopBar(
    title: String? = null,
    canNavigateBack: Boolean = false,
    onBack: () -> Unit = {},
    role: UserRole,
    onLogoLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    backdropLayer: GraphicsLayer? = null,
    backdropContainerOffset: Offset = Offset.Zero,
    trailingAction: @Composable (() -> Unit)? = null
) {
    val barShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    TruGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = barShape,
        blurRadius = 18.dp,
        shadowElevation = 14.dp,
        topTint = Color(0xBA161623),
        bottomTint = Color(0xA60E0E17),
        rimHighlightColor = TruGlassWhiteRim,
        rimGlowColor = TruLime.copy(alpha = 0.42f),
        borderWidth = 1.dp,
        shadowGlowColor = TruLime.copy(alpha = 0.14f),
        backdropLayer = backdropLayer,
        backdropContainerOffset = backdropContainerOffset,
        fillMaxWidthContent = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canNavigateBack) {
                    TruGlassSurface(
                        modifier = Modifier.size(48.dp).testTag("back_button"),
                        shape = CircleShape,
                        blurRadius = 12.dp,
                        shadowElevation = 8.dp,
                        topTint = Color(0xB8212131),
                        bottomTint = Color(0xA614141F),
                        rimHighlightColor = Color.White.copy(alpha = 0.30f),
                        rimGlowColor = TruLime.copy(alpha = 0.36f),
                        onClick = onBack
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = TruTextPrimary
                            )
                        }
                    }
                }
                if (title == null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {},
                                onLongClick = onLogoLongPress
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("truticket_logo_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TruLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = "TruTicket logo",
                                tint = TruBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "TruTicket",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TruTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (role != UserRole.ATTENDEE) {
                                Text(
                                    text = role.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TruLime,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TruTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailingAction != null) {
                Spacer(Modifier.width(8.dp))
                trailingAction()
            }
        }
    }
}

@Composable
fun TruBottomBar(
    currentRoute: String,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdropLayer: GraphicsLayer? = null,
    backdropContainerOffset: Offset = Offset.Zero
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        TruGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            blurRadius = 20.dp,
            shadowElevation = 18.dp,
            topTint = Color(0xBA191926),
            bottomTint = Color(0xA80F0F18),
            rimHighlightColor = Color.White.copy(alpha = 0.30f),
            rimGlowColor = TruLime.copy(alpha = 0.50f),
            borderWidth = 1.dp,
            shadowGlowColor = TruLime.copy(alpha = 0.22f),
            backdropLayer = backdropLayer,
            backdropContainerOffset = backdropContainerOffset,
            fillMaxWidthContent = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TruBottomTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) TruLime else Color(0xFFB3B3C0),
                        animationSpec = tween(180),
                        label = "bottom_tab_color"
                    )
                    val tabShape = RoundedCornerShape(22.dp)
                    val activePillModifier = if (selected) {
                        Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        TruLime.copy(alpha = 0.22f),
                                        TruLime.copy(alpha = 0.11f)
                                    )
                                ),
                                tabShape
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.42f),
                                        TruLime.copy(alpha = 0.72f)
                                    )
                                ),
                                shape = tabShape
                            )
                    } else {
                        Modifier
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .clip(tabShape)
                            .then(activePillModifier)
                            .clickable { onSelectTab(tab.route) }
                            .padding(horizontal = 4.dp, vertical = 7.dp)
                            .testTag("tab_${tab.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = contentColor,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitcherBottomSheet(
    currentRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TruSurface,
        contentColor = TruTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Switch prototype role",
                style = MaterialTheme.typography.headlineMedium,
                color = TruTextPrimary
            )
            Text(
                text = "Long-press the TruTicket mark anytime to test customer, corporate, or gate staff views.",
                style = MaterialTheme.typography.bodyMedium,
                color = TruTextMuted
            )
            UserRole.entries.forEach { role ->
                val active = role == currentRole
                Surface(
                    onClick = { onSelectRole(role) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) TruLime.copy(alpha = 0.14f) else TruSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (active) TruLime else TruBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("role_option_${role.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = role.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (active) TruLime else TruTextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = role.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TruTextMuted
                            )
                        }
                        if (active) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Active role",
                                tint = TruLime
                            )
                        }
                    }
                }
            }
        }
    }
}
