package com.sailguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.sailguard.app.data.model.TripHistoryEntity
import com.sailguard.app.data.network.ConnectaApiClient
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.TealPrimary
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

@Composable
fun HistoryScreen(historyVm: HistoryViewModel) {
    val trips        by historyVm.trips.collectAsState()
    val syncedTrips  by historyVm.syncedTrips.collectAsState()
    val totalCount = trips.size + syncedTrips.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.History, contentDescription = null,
                 tint = NearBlack, modifier = Modifier.size(24.dp))
            Column {
                Text("Trip History",
                     style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Text("$totalCount trip${if (totalCount == 1) "" else "s"} recorded",
                     style = MaterialTheme.typography.bodySmall,  color = TextSecondary)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (trips.isEmpty() && syncedTrips.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.History, contentDescription = null,
                         tint = TextSecondary.copy(alpha = 0.4f),
                         modifier = Modifier.size(56.dp))
                    Text("No trips yet",
                         style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    Text("Complete a trip to see your history here.",
                         style = MaterialTheme.typography.bodySmall,  color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (syncedTrips.isNotEmpty()) {
                    item {
                        Text(
                            "Synced from Connecta",
                            style      = MaterialTheme.typography.titleSmall,
                            color      = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    items(syncedTrips, key = { it.id }) { trip ->
                        SyncedTripCard(trip)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
                items(trips, key = { it.id }) { trip ->
                    TripHistoryCard(trip)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SyncedTripCard(trip: ConnectaApiClient.SyncedTrip) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = AppSurface),
        shape     = RoundedCornerShape(16.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .background(SuccessGreen.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint               = SuccessGreen,
                    modifier           = Modifier.size(22.dp)
                )
            }
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    trip.destination,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${trip.startDate} → ${trip.endDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                val planInfo = listOfNotNull(
                    trip.dataLabel,
                    trip.priceUsd?.let { "$${"%.2f".format(it)}" }
                ).joinToString(" · ")
                if (planInfo.isNotEmpty()) {
                    Text(planInfo, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        trip.planProvider ?: "Connecta Local",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Web checkout",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TripHistoryCard(trip: TripHistoryEntity) {
    val accent = if (trip.planWasEnough) SuccessGreen else ErrorRed

    Card(
        colors    = CardDefaults.cardColors(containerColor = AppSurface),
        shape     = RoundedCornerShape(16.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.Top
        ) {
            // Status dot
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (trip.planWasEnough) Icons.Filled.CheckCircle
                                         else Icons.Filled.Warning,
                    contentDescription = null,
                    tint               = accent,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Destination
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(trip.countryFlag, fontSize = 18.sp)
                    Text(trip.destination,
                         style      = MaterialTheme.typography.titleMedium,
                         color      = TextPrimary,
                         fontWeight = FontWeight.SemiBold)
                }
                // Date · duration
                Text(
                    "${dateFormat.format(Date(trip.date))}  ·  ${trip.tripDays} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                // GB used vs plan
                val planLabel = if (trip.planSizeGb < 0) "Unlimited"
                                else "${trip.planSizeGb.toLong()} GB"
                Text(
                    "${"%.2f".format(trip.actualGbUsed)} GB used of $planLabel plan",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                // Status text + cost
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        if (trip.planWasEnough) "Plan was sufficient" else "Plan ran short",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = accent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$${"%.2f".format(trip.totalCost)}",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = NearBlack,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
