package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Subtle ambient radial backdrop behind customer screens so frosted glass surfaces
 * have rich atmospheric depth to refract without hurting text legibility.
 */
@Composable
fun TruAmbientDarkBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(color = TruBg)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TruViolet.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.10f),
                radius = size.width * 0.72f
            ),
            radius = size.width * 0.72f,
            center = Offset(size.width * 0.88f, size.height * 0.10f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TruLime.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.14f, size.height * 0.34f),
                radius = size.width * 0.65f
            ),
            radius = size.width * 0.65f,
            center = Offset(size.width * 0.14f, size.height * 0.34f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TruLime.copy(alpha = 0.09f), Color.Transparent),
                center = Offset(size.width * 0.50f, size.height * 0.94f),
                radius = size.width * 0.68f
            ),
            radius = size.width * 0.68f,
            center = Offset(size.width * 0.50f, size.height * 0.94f)
        )
    }
}

/**
 * Layered dark glassmorphism container:
 * 1) Soft drop shadow with subtle ambient glow
 * 2) Dedicated blurred frosted backdrop layer (including optional real-time captured
 *    scrolling content blur on Android 12+ while keeping foreground text razor-sharp)
 * 3) Semi-transparent dark gradient fill + thin glowing white/neon-lime specular border
 */
@Composable
fun TruGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadius: Dp = 16.dp,
    shadowElevation: Dp = 10.dp,
    topTint: Color = TruGlassElevated,
    bottomTint: Color = TruGlassSurface,
    rimHighlightColor: Color = TruGlassWhiteRim,
    rimGlowColor: Color = TruLime.copy(alpha = 0.34f),
    borderWidth: Dp = 1.dp,
    shadowGlowColor: Color = TruLime.copy(alpha = 0.14f),
    backdropLayer: GraphicsLayer? = null,
    backdropContainerOffset: Offset = Offset.Zero,
    fillMaxWidthContent: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = remember(rimHighlightColor, rimGlowColor) {
        Brush.linearGradient(
            colors = listOf(
                rimHighlightColor,
                rimGlowColor,
                Color.White.copy(alpha = 0.08f)
            )
        )
    }
    val glassFillBrush = remember(topTint, bottomTint) {
        Brush.verticalGradient(colors = listOf(topTint, bottomTint))
    }

    var selfRootOffset by remember { mutableStateOf(Offset.Zero) }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                clip = false,
                ambientColor = shadowGlowColor,
                spotColor = Color.Black.copy(alpha = 0.80f)
            )
            .clip(shape)
            .onGloballyPositioned { coords ->
                selfRootOffset = coords.positionInRoot()
            }
    ) {
        // Live blurred backdrop layer (API 31+) + ambient frosted diffusion behind foreground text
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(radius = blurRadius, edgeTreatment = BlurredEdgeTreatment(shape))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.07f),
                            shadowGlowColor.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            if (backdropLayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dx = backdropContainerOffset.x - selfRootOffset.x
                    val dy = backdropContainerOffset.y - selfRootOffset.y
                    translate(left = dx, top = dy) {
                        drawLayer(backdropLayer)
                    }
                }
            }
        }

        // Crisp semi-transparent frosted glass body & glowing thin border
        val innerWidthMod = if (fillMaxWidthContent) Modifier.fillMaxWidth() else Modifier
        Box(
            modifier = innerWidthMod
                .then(clickableModifier)
                .background(glassFillBrush, shape)
                .border(borderWidth, borderBrush, shape),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Composable
fun TruGlassFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    minHeight: Dp = 44.dp,
    fontWeight: FontWeight? = null
) {
    val topColor by animateColorAsState(
        targetValue = if (selected) Color(0xD41B2312) else Color(0xB31B1B28),
        animationSpec = tween(180),
        label = "chip_top_tint"
    )
    val bottomColor by animateColorAsState(
        targetValue = if (selected) Color(0xCC12170D) else Color(0x9E12121B),
        animationSpec = tween(180),
        label = "chip_bottom_tint"
    )
    val rimGlow by animateColorAsState(
        targetValue = if (selected) TruLime.copy(alpha = 0.85f) else TruLime.copy(alpha = 0.22f),
        animationSpec = tween(180),
        label = "chip_rim_glow"
    )
    val rimWhite by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.55f) else TruGlassWhiteRim,
        animationSpec = tween(180),
        label = "chip_rim_white"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) TruLime else TruTextPrimary,
        animationSpec = tween(180),
        label = "chip_label_color"
    )

    TruGlassSurface(
        modifier = modifier.heightIn(min = minHeight),
        shape = CircleShape,
        blurRadius = 14.dp,
        shadowElevation = if (selected) 10.dp else 6.dp,
        topTint = topColor,
        bottomTint = bottomColor,
        rimHighlightColor = rimWhite,
        rimGlowColor = rimGlow,
        borderWidth = if (selected) 1.25.dp else 1.dp,
        shadowGlowColor = if (selected) TruLime.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.08f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = minHeight)
                .then(
                    if (selected) {
                        Modifier.background(TruGlassActiveLimeBg, CircleShape)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) TruLime else TruLime.copy(alpha = 0.82f)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = fontWeight ?: if (selected) FontWeight.Bold else FontWeight.Medium,
                color = labelColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TruGlassSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String = "glass_search_input",
    leadingContentDescription: String = "Search",
    boldText: Boolean = false,
    onSearchAction: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val rimGlow by animateColorAsState(
        targetValue = if (isFocused) TruLime.copy(alpha = 0.88f) else TruLime.copy(alpha = 0.26f),
        animationSpec = tween(180),
        label = "search_rim_glow"
    )
    val rimWhite by animateColorAsState(
        targetValue = if (isFocused) Color.White.copy(alpha = 0.50f) else TruGlassWhiteRim,
        animationSpec = tween(180),
        label = "search_rim_white"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isFocused) TruLime else TruTextMuted,
        animationSpec = tween(180),
        label = "search_icon_tint"
    )

    TruGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(18.dp),
        blurRadius = 16.dp,
        shadowElevation = if (isFocused) 14.dp else 8.dp,
        topTint = Color(0xC41A1A26),
        bottomTint = Color(0xB011111A),
        rimHighlightColor = rimWhite,
        rimGlowColor = rimGlow,
        borderWidth = if (isFocused) 1.25.dp else 1.dp,
        shadowGlowColor = if (isFocused) TruLime.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
        fillMaxWidthContent = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = leadingContentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (boldText) FontWeight.Bold else FontWeight.Normal,
                        color = TruTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = TruTextPrimary,
                        fontWeight = if (boldText) FontWeight.Bold else FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(TruLime),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchAction?.invoke() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                        .testTag(testTag)
                )
            }
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = TruTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}
