package com.sailguard.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.ConnectaOrange
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.viewmodel.TripViewModel

@Composable
fun CartScreen(
    tripVm:    TripViewModel,
    onConfirm: () -> Unit,
    onBack:    () -> Unit
) {
    val state by tripVm.state.collectAsState()
    val plan  = state.selectedPlan ?: state.suggestedPlan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Order Summary",
                     style      = MaterialTheme.typography.headlineSmall,
                     color      = TextPrimary,
                     fontWeight = FontWeight.Bold)
                Text("Review your Connecta eSIM plan before confirming",
                     style = MaterialTheme.typography.bodyMedium,
                     color = TextSecondary)
            }

            // ── Trip info card ────────────────────────────────────────────────
            Card(
                colors    = CardDefaults.cardColors(containerColor = AppSurface),
                shape     = RoundedCornerShape(16.dp),
                border    = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Destination row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(state.flag.ifEmpty { "🌍" }, fontSize = 36.sp)
                        Column {
                            Text(state.destination.ifEmpty { "Your Destination" },
                                 style      = MaterialTheme.typography.titleLarge,
                                 color      = TextPrimary,
                                 fontWeight = FontWeight.Bold)
                            Text("${state.durationDays} days  ·  ${state.usageStyle.label} usage",
                                 style = MaterialTheme.typography.bodySmall,
                                 color = TextSecondary)
                        }
                    }

                    HorizontalDivider(color = CardBorder)

                    // Plan details
                    if (plan != null) {
                        OrderLineItem(
                            label = "Data Plan",
                            value = if (plan.isUnlimited) "Unlimited" else "${plan.dataGB.toLong()} GB"
                        )
                        OrderLineItem(
                            label = "Validity",
                            value = "${plan.validDays} days"
                        )
                        OrderLineItem(
                            label = "Network",
                            value = plan.network
                        )

                        HorizontalDivider(color = CardBorder)

                        // Price row
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Total",
                                 style      = MaterialTheme.typography.titleMedium,
                                 color      = TextPrimary,
                                 fontWeight = FontWeight.SemiBold)
                            Text("$${"%.2f".format(plan.priceUSD)} USD",
                                 style      = MaterialTheme.typography.titleLarge,
                                 color      = TextPrimary,
                                 fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("No plan selected — go back and choose a plan.",
                             style = MaterialTheme.typography.bodyMedium,
                             color = TextSecondary)
                    }
                }
            }

            // ── Connecta plan badge ───────────────────────────────────────────
            if (plan != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null,
                             tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Connecta Local — Global coverage",
                                 style      = MaterialTheme.typography.titleSmall,
                                 color      = NearBlack,
                                 fontWeight = FontWeight.SemiBold)
                            Text("Works in 200+ destinations worldwide",
                                 style = MaterialTheme.typography.bodySmall,
                                 color = TextSecondary)
                        }
                    }
                }

                // Perks list
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("WHAT'S INCLUDED",
                             style         = MaterialTheme.typography.labelSmall,
                             color         = TextSecondary,
                             letterSpacing = 1.sp)
                        PerkRow("Instant eSIM activation")
                        PerkRow("No roaming fees")
                        PerkRow("Works on any unlocked device")
                        PerkRow("Cancel anytime before activation")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Bottom action bar ─────────────────────────────────────────────────
        Surface(
            color           = AppSurface,
            shadowElevation = 8.dp,
            tonalElevation  = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick  = {
                        tripVm.startTrip()
                        onConfirm()
                    },
                    enabled  = plan != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = ConnectaOrange,
                        disabledContainerColor = Color(0xFFE5E7EB)
                    )
                ) {
                    Text(
                        "Confirm & Start Trip",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleSmall
                    )
                }
                OutlinedButton(
                    onClick  = onBack,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = BorderStroke(1.dp, CardBorder)
                ) {
                    Text("← Back to Plans", color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun OrderLineItem(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value,
             style      = MaterialTheme.typography.bodyMedium,
             color      = TextPrimary,
             fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PerkRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Check, contentDescription = null,
             tint = SuccessGreen, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}
