package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

enum class TruStatusTone {
    SUCCESS,
    WARNING,
    DANGER,
    INFO
}

@Composable
fun TruStatusBadge(
    text: String,
    tone: TruStatusTone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val (color, defaultIcon) = when (tone) {
        TruStatusTone.SUCCESS -> TruSuccessMint to Icons.Filled.CheckCircle
        TruStatusTone.WARNING -> TruWarningAmber to Icons.Filled.Warning
        TruStatusTone.DANGER -> TruDangerCoral to Icons.Filled.Error
        TruStatusTone.INFO -> TruViolet to Icons.Filled.CheckCircle
    }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon ?: defaultIcon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TruPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "primary_button",
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_press"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .scale(scale)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TruLime,
            contentColor = TruBg,
            disabledContainerColor = TruSurfaceElevated,
            disabledContentColor = TruTextMuted
        )
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TruSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "secondary_button",
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TruBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = TruSurfaceElevated,
            contentColor = TruTextPrimary
        )
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val LocalTicketStubOffscreen = staticCompositionLocalOf { false }

@Composable
fun PerforatedDivider(
    modifier: Modifier = Modifier,
    notchColor: Color = Color(0xFF071612),
    lineColor: Color = TruBorder
) {
    val useClearCutout = LocalTicketStubOffscreen.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cy = size.height / 2f
            val r = 10.dp.toPx()
            drawLine(
                color = lineColor,
                start = Offset(r + 6.dp.toPx(), cy),
                end = Offset(size.width - r - 6.dp.toPx(), cy),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
            if (useClearCutout) {
                drawCircle(color = Color.Transparent, radius = r, center = Offset(0f, cy), blendMode = BlendMode.Clear)
                drawCircle(color = Color.Transparent, radius = r, center = Offset(size.width, cy), blendMode = BlendMode.Clear)
            } else {
                drawCircle(color = notchColor, radius = r, center = Offset(0f, cy))
                drawCircle(color = notchColor, radius = r, center = Offset(size.width, cy))
            }
        }
    }
}

@Composable
fun TicketStubSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    borderColor: Color = TruBorder,
    containerColor: Color = TruSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val mod = modifier
        .fillMaxWidth()
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .clip(shape)
        .background(containerColor)
        .border(1.dp, borderColor, shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    CompositionLocalProvider(LocalTicketStubOffscreen provides true) {
        Column(modifier = mod, content = content)
    }
}

@Composable
fun ShimmerSkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(TruSurfaceElevated.copy(alpha = alpha))
            .border(1.dp, TruBorder, RoundedCornerShape(16.dp))
    )
}

@Composable
fun QrCodeMatrix(
    payload: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 188.dp
) {
    val qrBitmap = remember(payload) {
        generateQrBitmap(payload, 512)
    }
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(10.dp)
            .semantics { contentDescription = "Dynamic Ticket DNA QR code" },
        contentAlignment = Alignment.Center
    ) {
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Scannable Ticket DNA QR code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun generateQrBitmap(content: String, sizePx: Int): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}
