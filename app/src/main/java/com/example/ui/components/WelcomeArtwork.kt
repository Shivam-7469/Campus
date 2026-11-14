package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TruBg
import com.example.ui.theme.TruLime
import com.example.ui.theme.TruTextPrimary
import com.example.ui.theme.TruViolet

enum class WelcomeGlyphKind {
    RIGHT_ARROW,
    DOWN_ARROW,
    STARBURST
}

@Composable
fun WelcomeGridBackdrop(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = TruBg)

        // Subtle atmospheric glow behind the mascot & headline
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TruLime.copy(alpha = 0.14f), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.56f),
                radius = size.width * 0.65f
            ),
            center = Offset(size.width * 0.82f, size.height * 0.56f),
            radius = size.width * 0.65f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TruViolet.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.14f, size.height * 0.18f),
                radius = size.width * 0.55f
            ),
            center = Offset(size.width * 0.14f, size.height * 0.18f),
            radius = size.width * 0.55f
        )

        // Subtle dark architectural grid
        val cell = 44.dp.toPx()
        val gridColor = Color.White.copy(alpha = 0.045f)
        val strokePx = 1.dp.toPx()

        var x = cell * 0.5f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokePx
            )
            x += cell
        }

        var y = cell * 0.5f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokePx
            )
            y += cell
        }

        // Thin curved neon-lime wave lines inspired by the poster backdrop
        val upperWave = Path().apply {
            moveTo(-size.width * 0.05f, size.height * 0.28f)
            cubicTo(
                size.width * 0.28f, size.height * 0.34f,
                size.width * 0.66f, size.height * 0.18f,
                size.width * 1.06f, size.height * 0.25f
            )
        }
        drawPath(
            path = upperWave,
            color = TruLime.copy(alpha = 0.20f),
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )

        val lowerWave = Path().apply {
            moveTo(-size.width * 0.08f, size.height * 0.72f)
            cubicTo(
                size.width * 0.32f, size.height * 0.62f,
                size.width * 0.68f, size.height * 0.80f,
                size.width * 1.08f, size.height * 0.66f
            )
        }
        drawPath(
            path = lowerWave,
            color = TruLime.copy(alpha = 0.16f),
            style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun WelcomePixelGlyphIcon(
    kind: WelcomeGlyphKind,
    tint: Color = TruBg,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val grid = 9
        val cell = minOf(size.width, size.height) / grid
        val startX = (size.width - cell * grid) / 2f
        val startY = (size.height - cell * grid) / 2f

        fun px(col: Int, row: Int) {
            drawRect(
                color = tint,
                topLeft = Offset(startX + col * cell, startY + row * cell),
                size = Size(cell * 0.96f, cell * 0.96f)
            )
        }

        when (kind) {
            WelcomeGlyphKind.RIGHT_ARROW -> {
                for (c in 1..7) px(c, 4)
                px(5, 2)
                px(6, 3)
                px(6, 5)
                px(5, 6)
            }
            WelcomeGlyphKind.DOWN_ARROW -> {
                for (r in 1..7) px(4, r)
                px(2, 5)
                px(3, 6)
                px(5, 6)
                px(6, 5)
            }
            WelcomeGlyphKind.STARBURST -> {
                for (i in 1..7) {
                    px(4, i)
                    px(i, 4)
                }
                px(2, 2); px(6, 2)
                px(2, 6); px(6, 6)
            }
        }
    }
}

@Composable
fun WelcomePixelSparkleCluster(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "sparkle_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_alpha"
    )

    Canvas(modifier = modifier) {
        val unit = minOf(size.width, size.height) / 14f

        fun drawPixelBlock(color: Color, x: Float, y: Float, scale: Float = 1f) {
            drawRect(
                color = color,
                topLeft = Offset(x * unit, y * unit),
                size = Size(unit * scale, unit * scale)
            )
        }

        // Hollow pixel 4-point sparkle in neon-lime
        val limeGlow = TruLime.copy(alpha = pulseAlpha)
        val starCoords = listOf(
            3f to 3f,
            3f to 4f, 3f to 5f,
            2f to 6f, 4f to 6f,
            1f to 7f, 5f to 7f,
            0f to 8f, 6f to 8f,
            1f to 9f, 5f to 9f,
            2f to 10f, 4f to 10f,
            3f to 11f, 3f to 12f
        )
        starCoords.forEach { (cx, cy) ->
            drawPixelBlock(limeGlow, cx + 1f, cy, 0.92f)
        }

        // Smaller crisp white pixel sparkle offset top-right
        val whiteSparkle = TruTextPrimary.copy(alpha = pulseAlpha)
        val miniCoords = listOf(
            10f to 1f,
            9f to 2f, 10f to 2f, 11f to 2f,
            10f to 3f
        )
        miniCoords.forEach { (cx, cy) ->
            drawPixelBlock(whiteSparkle, cx, cy, 0.95f)
        }
    }
}

@Composable
fun TruMascotIllustration(
    contentDescriptionText: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mascot_idle")
    val floatOffset by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_bob"
    )

    Canvas(
        modifier = modifier.semantics {
            contentDescription = contentDescriptionText
        }
    ) {
        val w = size.width
        val h = size.height
        val dy = floatOffset.dp.toPx()

        val mascotLime = TruLime
        val stripeOlive = Color(0xFF9AC41A)
        val eyeWhite = Color(0xFFF7F7FA)

        // Body silhouette with dual asymmetrical pointy top peaks
        val bodyPath = Path().apply {
            moveTo(w * 0.24f, h * 0.80f + dy)
            lineTo(w * 0.30f, h * 0.15f + dy)
            lineTo(w * 0.42f, h * 0.04f + dy) // Tall left peak
            lineTo(w * 0.56f, h * 0.22f + dy) // Valley
            lineTo(w * 0.65f, h * 0.13f + dy) // Shorter right peak
            lineTo(w * 0.81f, h * 0.44f + dy)
            lineTo(w * 0.84f, h * 0.80f + dy)
            close()
        }

        // Left & right arms behind body silhouette
        val armStroke = (w * 0.068f).coerceAtLeast(5.dp.toPx())
        // Left arm
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.28f, h * 0.56f + dy),
            end = Offset(w * 0.14f, h * 0.78f + dy),
            strokeWidth = armStroke,
            cap = StrokeCap.Round
        )
        // Left hand fingers
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.14f, h * 0.78f + dy),
            end = Offset(w * 0.10f, h * 0.84f + dy),
            strokeWidth = armStroke * 0.45f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.14f, h * 0.78f + dy),
            end = Offset(w * 0.16f, h * 0.85f + dy),
            strokeWidth = armStroke * 0.45f,
            cap = StrokeCap.Round
        )

        // Right arm
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.79f, h * 0.56f + dy),
            end = Offset(w * 0.91f, h * 0.78f + dy),
            strokeWidth = armStroke,
            cap = StrokeCap.Round
        )
        // Right hand fingers
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.91f, h * 0.78f + dy),
            end = Offset(w * 0.89f, h * 0.85f + dy),
            strokeWidth = armStroke * 0.45f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.91f, h * 0.78f + dy),
            end = Offset(w * 0.95f, h * 0.84f + dy),
            strokeWidth = armStroke * 0.45f,
            cap = StrokeCap.Round
        )

        // Legs and flat outward-facing feet
        val legStroke = (w * 0.085f).coerceAtLeast(6.dp.toPx())
        // Left leg + foot
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.40f, h * 0.79f + dy),
            end = Offset(w * 0.40f, h * 0.95f + dy),
            strokeWidth = legStroke,
            cap = StrokeCap.Butt
        )
        drawRoundRect(
            color = mascotLime,
            topLeft = Offset(w * 0.25f, h * 0.92f + dy),
            size = Size(w * 0.20f, h * 0.055f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Right leg + foot
        drawLine(
            color = mascotLime,
            start = Offset(w * 0.67f, h * 0.79f + dy),
            end = Offset(w * 0.67f, h * 0.95f + dy),
            strokeWidth = legStroke,
            cap = StrokeCap.Butt
        )
        drawRoundRect(
            color = mascotLime,
            topLeft = Offset(w * 0.62f, h * 0.92f + dy),
            size = Size(w * 0.20f, h * 0.055f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Main neon-lime torso + clipped side shading stripes
        drawPath(path = bodyPath, color = mascotLime)
        clipPath(path = bodyPath) {
            val bandHeight = h * 0.036f
            // Left stripes
            drawRoundRect(
                color = stripeOlive,
                topLeft = Offset(w * 0.16f, h * 0.48f + dy),
                size = Size(w * 0.21f, bandHeight),
                cornerRadius = CornerRadius(bandHeight / 2f, bandHeight / 2f)
            )
            drawRoundRect(
                color = stripeOlive,
                topLeft = Offset(w * 0.14f, h * 0.59f + dy),
                size = Size(w * 0.19f, bandHeight),
                cornerRadius = CornerRadius(bandHeight / 2f, bandHeight / 2f)
            )
            // Right stripes
            drawRoundRect(
                color = stripeOlive,
                topLeft = Offset(w * 0.72f, h * 0.48f + dy),
                size = Size(w * 0.20f, bandHeight),
                cornerRadius = CornerRadius(bandHeight / 2f, bandHeight / 2f)
            )
            drawRoundRect(
                color = stripeOlive,
                topLeft = Offset(w * 0.75f, h * 0.59f + dy),
                size = Size(w * 0.19f, bandHeight),
                cornerRadius = CornerRadius(bandHeight / 2f, bandHeight / 2f)
            )
        }

        // Cool half-lidded eyes
        val leftEyeRect = Rect(
            left = w * 0.35f,
            top = h * 0.31f + dy,
            right = w * 0.53f,
            bottom = h * 0.395f + dy
        )
        val rightEyeRect = Rect(
            left = w * 0.56f,
            top = h * 0.31f + dy,
            right = w * 0.73f,
            bottom = h * 0.395f + dy
        )

        // Lower half-oval eye whites
        drawArc(
            color = eyeWhite,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = leftEyeRect.topLeft,
            size = leftEyeRect.size
        )
        drawArc(
            color = eyeWhite,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = rightEyeRect.topLeft,
            size = rightEyeRect.size
        )

        // Dark pupils tucked beneath upper eyelids
        val pupilRadius = w * 0.036f
        drawCircle(
            color = TruBg,
            radius = pupilRadius,
            center = Offset(leftEyeRect.center.x + w * 0.01f, leftEyeRect.center.y + h * 0.015f)
        )
        drawCircle(
            color = TruBg,
            radius = pupilRadius,
            center = Offset(rightEyeRect.center.x + w * 0.01f, rightEyeRect.center.y + h * 0.015f)
        )

        // Flat upper eyelid lines for relaxed expression
        val lidStroke = 2.dp.toPx()
        drawLine(
            color = TruBg,
            start = Offset(leftEyeRect.left, leftEyeRect.center.y),
            end = Offset(leftEyeRect.right, leftEyeRect.center.y),
            strokeWidth = lidStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = TruBg,
            start = Offset(rightEyeRect.left, rightEyeRect.center.y),
            end = Offset(rightEyeRect.right, rightEyeRect.center.y),
            strokeWidth = lidStroke,
            cap = StrokeCap.Round
        )

        // Subtle confident smile curve
        val smilePath = Path().apply {
            moveTo(w * 0.48f, h * 0.435f + dy)
            quadraticTo(
                w * 0.55f, h * 0.465f + dy,
                w * 0.62f, h * 0.435f + dy
            )
        }
        drawPath(
            path = smilePath,
            color = TruBg,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
