package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AllocationStatus
import com.example.data.GroupMemberAllocation
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TruTicketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagerScreen(
    state: TruTicketUiState,
    onBack: () -> Unit,
    onAssignMember: (String, String) -> Unit,
    onRevokePending: (String) -> Unit,
    onLogoLongPress: () -> Unit
) {
    val claimedCount = state.groupAllocations.count { it.status == AllocationStatus.CLAIMED }
    val pendingCount = state.groupAllocations.count { it.status == AllocationStatus.PENDING_CLAIM }
    val unassignedCount = state.groupAllocations.count { it.status == AllocationStatus.UNASSIGNED }

    var activeAllocationForAssign by remember { mutableStateOf<GroupMemberAllocation?>(null) }
    var contactInput by remember { mutableStateOf("") }
    var assignError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .navigationBarsPadding()
    ) {
        TruTopBar(
            title = "Group manager",
            canNavigateBack = true,
            onBack = onBack,
            role = state.userRole,
            onLogoLongPress = onLogoLongPress
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Banner: face value never changes
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruLime.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TruLime.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Shield, null, tint = TruLime, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Face value never changes (₹2,000). Team claims and reassignments are always ₹0.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TruTextPrimary
                        )
                    }
                }
            }

            // Counters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GroupCounterCard("Claimed", claimedCount, TruSuccessMint, Modifier.weight(1f))
                    GroupCounterCard("Pending", pendingCount, TruWarningAmber, Modifier.weight(1f))
                    GroupCounterCard("Unassigned", unassignedCount, TruViolet, Modifier.weight(1f))
                }
            }

            item {
                Text("Team seat allocations", style = MaterialTheme.typography.titleLarge, color = TruTextPrimary)
            }

            items(state.groupAllocations, key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TruSurface,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.fillMaxWidth().testTag("group_row_${item.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.ticketRef, style = MonoSmallStyle, color = TruLime)
                            val tone = when (item.status) {
                                AllocationStatus.CLAIMED -> TruStatusTone.SUCCESS
                                AllocationStatus.PENDING_CLAIM -> TruStatusTone.WARNING
                                AllocationStatus.UNASSIGNED -> TruStatusTone.INFO
                            }
                            TruStatusBadge(text = item.status.label, tone = tone)
                        }

                        Text(
                            text = item.contact ?: "Open corporate seat · ${TruTicketFormatters.formatInr(item.faceValueInr)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TruTextPrimary
                        )
                        Text(item.updatedAt, style = MaterialTheme.typography.bodySmall, color = TruTextMuted)

                        when (item.status) {
                            AllocationStatus.UNASSIGNED -> {
                                TruPrimaryButton(
                                    text = "Assign for free",
                                    onClick = {
                                        contactInput = ""
                                        assignError = null
                                        activeAllocationForAssign = item
                                    },
                                    testTag = "assign_free_${item.id}",
                                    leadingIcon = Icons.Filled.PersonAdd
                                )
                            }
                            AllocationStatus.PENDING_CLAIM -> {
                                OutlinedButton(
                                    onClick = { onRevokePending(item.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, TruDangerCoral.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("revoke_${item.id}")
                                ) {
                                    Icon(Icons.Filled.Undo, null, tint = TruDangerCoral, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Revoke pending invite", color = TruDangerCoral)
                                }
                            }
                            AllocationStatus.CLAIMED -> Unit
                        }
                    }
                }
            }
        }
    }

    if (activeAllocationForAssign != null) {
        val target = activeAllocationForAssign!!
        ModalBottomSheet(
            onDismissRequest = { activeAllocationForAssign = null },
            containerColor = TruSurface,
            contentColor = TruTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Assign seat ${target.ticketRef}", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Enter your colleague's email or mobile number. Face value stays locked at ${TruTicketFormatters.formatInr(target.faceValueInr)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TruTextMuted
                )
                OutlinedTextField(
                    value = contactInput,
                    onValueChange = {
                        contactInput = it
                        assignError = null
                    },
                    label = { Text("Email or +91 phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("assign_contact_input"),
                    shape = RoundedCornerShape(14.dp)
                )
                if (assignError != null) {
                    TruStatusBadge(text = assignError!!, tone = TruStatusTone.DANGER)
                }
                TruPrimaryButton(
                    text = "Assign for free",
                    onClick = {
                        if (contactInput.trim().length < 5) {
                            assignError = "Enter a valid phone number or email address."
                        } else {
                            onAssignMember(target.id, contactInput.trim())
                            activeAllocationForAssign = null
                        }
                    },
                    testTag = "confirm_assign_button"
                )
            }
        }
    }
}

@Composable
private fun GroupCounterCard(
    label: String,
    count: Int,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TruSurface,
        border = BorderStroke(1.dp, TruBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(count.toString(), style = MonoLargeStyle, color = accent)
            Text(label, style = MaterialTheme.typography.labelMedium, color = TruTextMuted)
        }
    }
}
