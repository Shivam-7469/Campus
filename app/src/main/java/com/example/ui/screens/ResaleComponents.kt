package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ResaleListing
import com.example.data.TruTicketFormatters
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun FairResaleListingCard(
    listing: ResaleListing,
    onBuy: () -> Unit
) {
    val isFaceValue = listing.askPriceInr <= listing.faceValueInr
    val isMaxCap = listing.askPriceInr >= listing.maxCapInr()
    val capTag = when {
        isFaceValue -> "FACE VALUE (100%)"
        isMaxCap -> "MAX CAP (110%)"
        else -> "WITHIN CAP"
    }
    val priceHeader = when {
        isFaceValue -> "ZERO MARKUP RESALE"
        isMaxCap -> "CAPPED ASK PRICE"
        else -> "RESALE FAIR PRICE"
    }
    val transferText = if (listing.transferCount == 0) {
        "Verified Fan · 1st Hand Transfer · 0 Prior Resales"
    } else {
        "Verified Fan · ${listing.transferCount} Prior Transfer · Instant QR Burn"
    }

    TicketStubSurface(
        cornerRadius = 24.dp,
        modifier = Modifier.testTag("resale_card_${listing.id}")
    ) {
        // Top event section
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = listing.imageResId),
                contentDescription = "${listing.eventTitle} artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${listing.category.label.uppercase()} · ${listing.city.uppercase()}",
                        style = MonoSmallStyle,
                        color = TruViolet,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = TruSuccessMint.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, TruSuccessMint.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = capTag,
                            style = MonoSmallStyle,
                            color = TruSuccessMint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = listing.eventTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TruTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ConfirmationNumber,
                        contentDescription = null,
                        tint = TruTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${listing.tierName} · ${listing.dateLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TruTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        PerforatedDivider()

        // Bottom pricing + trust + CTA section
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ORIGINAL MRP", style = MonoSmallStyle, color = TruTextMuted)
                    Text(
                        text = TruTicketFormatters.formatInr(listing.faceValueInr),
                        style = MonoMediumStyle.copy(textDecoration = TextDecoration.LineThrough),
                        color = TruTextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(priceHeader, style = MonoSmallStyle, color = TruSuccessMint)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = TruTicketFormatters.formatInr(listing.askPriceInr),
                            style = MonoLargeStyle,
                            color = TruTextPrimary
                        )
                        Text("INR", style = MonoSmallStyle, color = TruTextMuted, modifier = Modifier.padding(bottom = 3.dp))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TruSurfaceElevated,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = TruSuccessMint, modifier = Modifier.size(15.dp))
                    Text(
                        text = transferText,
                        style = MaterialTheme.typography.labelMedium,
                        color = TruTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBuy,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TruLime, contentColor = TruBg),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .testTag("buy_resale_${listing.id}")
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Buy for ${TruTicketFormatters.formatInr(listing.askPriceInr)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = TruSurfaceElevated,
                    border = BorderStroke(1.dp, TruBorder),
                    modifier = Modifier.height(52.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = "Direct pass", tint = TruSuccessMint, modifier = Modifier.size(16.dp))
                        Text("DIRECT", style = MonoSmallStyle, color = TruTextMuted)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResaleReviewSheet(
    listing: ResaleListing,
    onDismiss: () -> Unit,
    onConfirmPay: () -> Unit
) {
    val fee = (listing.askPriceInr * 3) / 100
    val total = listing.askPriceInr + fee
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Review & pay", style = MaterialTheme.typography.headlineMedium, color = TruTextPrimary)
                TruStatusBadge(text = "Within cap", tone = TruStatusTone.SUCCESS)
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TruSurfaceElevated,
                border = BorderStroke(1.dp, TruBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(listing.ticketRef, style = MonoSmallStyle, color = TruLime)
                    Text(listing.eventTitle, style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                    Text("${listing.tierName} · ${listing.city} · ${listing.dateLabel}", style = MaterialTheme.typography.bodySmall, color = TruTextMuted)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetPriceRow("Ticket ask (MRP ${TruTicketFormatters.formatInr(listing.faceValueInr)})", TruTicketFormatters.formatInr(listing.askPriceInr))
                SheetPriceRow("3% verification & escrow fee", TruTicketFormatters.formatInr(fee))
                HorizontalDivider(color = TruBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total payable · UPI", style = MaterialTheme.typography.titleMedium, color = TruTextPrimary)
                    Text(TruTicketFormatters.formatInr(total), style = MonoLargeStyle, color = TruLime)
                }
            }
            TruPrimaryButton(
                text = "Pay ${TruTicketFormatters.formatInr(total)} & transfer",
                onClick = onConfirmPay,
                testTag = "confirm_resale_buy_button",
                leadingIcon = Icons.Filled.Lock
            )
        }
    }
}

@Composable
private fun SheetPriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TruTextMuted)
        Text(value, style = MonoMediumStyle, color = TruTextPrimary)
    }
}
