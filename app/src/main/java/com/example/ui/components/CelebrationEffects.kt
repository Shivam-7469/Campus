package com.example.ui.components

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R

@Composable
fun CelebratoryConfettiOverlay(
    triggerKey: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val animationsEnabled = remember(context) {
        val scale = runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f)
        scale > 0f
    }

    var shouldPlay by remember(triggerKey, enabled) {
        mutableStateOf(enabled && animationsEnabled)
    }

    if (!shouldPlay) return

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti_celebration)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = shouldPlay && composition != null,
        iterations = 1,
        speed = 1f,
        restartOnPlay = true
    )

    LaunchedEffect(composition, progress) {
        if (composition != null && progress >= 0.99f) {
            shouldPlay = false
            onFinished()
        }
    }

    if (composition != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("confetti_celebration_overlay"),
            contentAlignment = Alignment.TopCenter
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                renderMode = RenderMode.AUTOMATIC,
                clipToCompositionBounds = false,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
