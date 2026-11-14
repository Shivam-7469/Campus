package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.UserRole
import com.example.ui.components.TruGlassFilterChip
import com.example.ui.components.TruPrimaryButton
import com.example.ui.components.TruStatusBadge
import com.example.ui.components.TruStatusTone
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    selectedRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    onLoginSuccess: () -> Unit,
    onLogoLongPress: () -> Unit
) {
    var usePhone by remember { mutableStateOf(true) }
    var contactInput by remember { mutableStateOf("9845021834") }
    var otpRequested by remember { mutableStateOf(true) }
    var otpValue by remember { mutableStateOf("482910") }
    var resendSeconds by remember { mutableIntStateOf(24) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(otpRequested, resendSeconds) {
        if (otpRequested && resendSeconds > 0) {
            delay(1000L)
            resendSeconds -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TruBg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(onClick = {}, onLongClick = onLogoLongPress)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TruLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ConfirmationNumber, null, tint = TruBg, modifier = Modifier.size(20.dp))
            }
            Text("TruTicket", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Sign in to your pass vault",
                style = MaterialTheme.typography.headlineLarge,
                color = TruTextPrimary
            )
            Text(
                text = "Fair access. Genuine tickets. Controlled resale.",
                style = MaterialTheme.typography.bodyMedium,
                color = TruTextMuted
            )
        }

        // Role chips (Attendee, Group manager)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Account type", style = MaterialTheme.typography.labelLarge, color = TruTextMuted)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(UserRole.ATTENDEE, UserRole.GROUP_MANAGER).forEach { role ->
                    val active = selectedRole == role
                    TruGlassFilterChip(
                        label = role.label,
                        selected = active,
                        onClick = { onSelectRole(role) },
                        minHeight = 48.dp,
                        modifier = Modifier.testTag("login_role_${role.name}")
                    )
                }
            }
        }

        // Phone or email toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(TruSurface)
                .border(1.dp, TruBorder, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(true to "Phone (+91)", false to "Email").forEach { (isPhone, label) ->
                val selected = usePhone == isPhone
                Surface(
                    onClick = {
                        usePhone = isPhone
                        contactInput = if (isPhone) "9845021834" else "aarav.sharma@gmail.com"
                        errorMessage = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) TruSurfaceElevated else TruSurface,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isPhone) Icons.Filled.PhoneAndroid else Icons.Filled.Email,
                            contentDescription = null,
                            tint = if (selected) TruLime else TruTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) TruTextPrimary else TruTextMuted
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = contactInput,
            onValueChange = {
                contactInput = it
                errorMessage = null
            },
            label = { Text(if (usePhone) "Mobile number" else "Email address") },
            prefix = if (usePhone) { { Text("+91 ", style = MonoMediumStyle, color = TruLime) } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (usePhone) KeyboardType.Phone else KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("login_contact_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TruLime,
                unfocusedBorderColor = TruBorder,
                focusedContainerColor = TruSurface,
                unfocusedContainerColor = TruSurface
            )
        )

        // 6-box OTP section
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = TruSurface,
            border = BorderStroke(1.dp, TruBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("6-digit OTP", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                    if (resendSeconds > 0) {
                        Text(
                            text = "Resend in 00:${resendSeconds.toString().padStart(2, '0')}",
                            style = MonoSmallStyle,
                            color = TruTextMuted
                        )
                    } else {
                        TextButton(
                            onClick = {
                                resendSeconds = 30
                                otpValue = "482910"
                            }
                        ) {
                            Text("Resend OTP", color = TruLime)
                        }
                    }
                }

                // Visual 6-box OTP cells backed by a clean input field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 6) {
                        val char = otpValue.getOrNull(i)?.toString() ?: ""
                        val focused = i == otpValue.length.coerceAtMost(5)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TruSurfaceElevated)
                                .border(
                                    width = if (focused) 1.5.dp else 1.dp,
                                    color = if (focused) TruLime else TruBorder,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = MonoLargeStyle,
                                color = TruTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = otpValue,
                    onValueChange = { raw ->
                        otpValue = raw.filter { it.isDigit() }.take(6)
                        errorMessage = null
                    },
                    label = { Text("Enter 6-digit verification code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("login_otp_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TruLime,
                        unfocusedBorderColor = TruBorder
                    )
                )
            }
        }

        if (errorMessage != null) {
            TruStatusBadge(
                text = errorMessage!!,
                tone = TruStatusTone.DANGER
            )
        }

        TruPrimaryButton(
            text = "Verify & continue",
            onClick = {
                val validContact = if (usePhone) contactInput.filter { it.isDigit() }.length >= 10 else contactInput.contains("@")
                when {
                    !validContact -> errorMessage = if (usePhone) "Enter a valid 10-digit Indian mobile number." else "Enter a valid email address."
                    otpValue.length != 6 -> errorMessage = "Enter the complete 6-digit OTP to continue."
                    else -> onLoginSuccess()
                }
            },
            testTag = "login_submit_button"
        )
    }
}
