package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.GateScanRecord
import com.example.data.ScanVerdict
import com.example.ui.theme.*

@Composable
fun GateResultScreen(
    record: GateScanRecord?,
    onScanNext: () -> Unit
) {
    val context = LocalContext.current
    val verdict = record?.verdict ?: ScanVerdict.INVALID

    LaunchedEffect(record?.id) {
        triggerGateHaptic(context, verdict == ScanVerdict.VALID)
    }

    val (bgColor, icon) = when (verdict) {
        ScanVerdict.VALID -> Color(0xFF0A3D2B) to Icons.Filled.CheckCircle
        ScanVerdict.ALREADY_USED -> Color(0xFF422C06) to Icons.Filled.Warning
        ScanVerdict.EXPIRED, ScanVerdict.OFFLINE_UNVERIFIED -> Color(0xFF422C06) to Icons.Filled.Schedule
        ScanVerdict.REVOKED, ScanVerdict.INVALID, ScanVerdict.WRONG_EVENT -> Color(0xFF4C0D1A) to Icons.Filled.Cancel
    }

    val accent = when (verdict) {
        ScanVerdict.VALID -> TruSuccessMint
        ScanVerdict.ALREADY_USED, ScanVerdict.EXPIRED, ScanVerdict.OFFLINE_UNVERIFIED -> TruWarningAmber
        ScanVerdict.REVOKED, ScanVerdict.INVALID, ScanVerdict.WRONG_EVENT -> TruDangerCoral
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GATE VERIFICATION", style = MonoSmallStyle, color = TruTextPrimary.copy(alpha = 0.8f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = verdict.title,
                tint = accent,
                modifier = Modifier.size(116.dp)
            )
            Text(
                text = verdict.title,
                style = MaterialTheme.typography.displayLarge,
                color = TruTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("gate_verdict_title")
            )
            if (verdict == ScanVerdict.ALREADY_USED && record?.firstScanTimeLabel != null) {
                Text(
                    text = "First scanned at ${record.firstScanTimeLabel}",
                    style = MonoMediumStyle,
                    color = accent
                )
            }

            // Minimal privacy-preserving metadata: ONLY tier and ticket ref
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TICKET REF", style = MonoSmallStyle, color = TruTextMuted)
                        Text(record?.ticketRef ?: "—", style = MonoLargeStyle, color = TruTextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TIER", style = MonoSmallStyle, color = TruTextMuted)
                        Text(record?.tierName ?: "—", style = MonoLargeStyle, color = accent)
                    }
                }
            }
        }

        Button(
            onClick = onScanNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TruTextPrimary, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("gate_scan_next_button")
        ) {
            Text("Scan next", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Suppress("DEPRECATION")
private fun triggerGateHaptic(context: Context, isValid: Boolean) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ms = if (isValid) 90L else 220L
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(if (isValid) 90L else 220L)
        }
    } catch (_: Exception) {
    }
}
