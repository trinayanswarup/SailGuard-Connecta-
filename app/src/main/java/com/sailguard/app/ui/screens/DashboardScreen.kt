package com.sailguard.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sailguard.app.ui.theme.ConnectaOrange
import com.sailguard.app.ui.theme.ConnectaOrangeLight
import com.sailguard.app.data.model.NetworkStrength
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.AppSurface2
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.TealPrimary
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.ui.theme.WarningAmber
import com.sailguard.app.viewmodel.DashboardViewModel
import com.sailguard.app.viewmodel.RiskLevel
import com.sailguard.app.viewmodel.TripViewModel

@Composable
fun DashboardScreen(
    dashVm:            DashboardViewModel,
    tripVm:            TripViewModel,
    onEndTrip:         () -> Unit = {},
    onNavigateToSetup: () -> Unit = {}
) {
    val dashState by dashVm.state.collectAsState()
    val tripState by tripVm.state.collectAsState()

    var showEndTripDialog by remember { mutableStateOf(false) }

    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text("End this trip?") },
            text  = { Text("This will clear all trip data and return you to Setup.") },
            confirmButton = {
                TextButton(onClick = {
                    showEndTripDialog = false
                    dashVm.saveAndEndTrip()
                    tripVm.resetTrip()
                    onEndTrip()
                }) {
                    Text("Confirm", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTripDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(tripState.activeTrip) {
        val active = tripState.activeTrip
        if (active != null) dashVm.bindTrip(active) else dashVm.clearTrip()
    }

    val trip = dashState.trip

    if (trip == null) {
        Box(
            modifier         = Modifier.fillMaxSize().background(AppBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier            = Modifier.padding(32.dp)
            ) {
                Text("🌍", fontSize = 72.sp)
                Text(
                    text       = "No active trip",
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text      = "Set up a trip to start tracking your data usage",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = TextSecondary,
                    textAlign = TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = onNavigateToSetup,
                    shape   = RoundedCornerShape(12.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = ConnectaOrange)
                ) {
                    Text(
                        text       = "Plan a Trip",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Trip header ───────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(trip.flag, style = MaterialTheme.typography.headlineSmall)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(trip.destination,
                     style = MaterialTheme.typography.titleLarge,
                     color = TextPrimary,
                     fontWeight = FontWeight.Bold)
                Text("${trip.selectedPlan.dataGB.toLong()} GB Connecta Plan  ·  ${trip.durationDays} days",
                     style = MaterialTheme.typography.bodySmall,
                     color = TextSecondary)
                if (trip.connectaTripId != null) {
                    androidx.compose.foundation.layout.Spacer(Modifier.height(2.dp))
                    androidx.compose.material3.Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "Synced with Connecta",
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── Data ring card ────────────────────────────────────────────────────
        Card(
            colors    = CardDefaults.cardColors(containerColor = AppSurface),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("DATA REMAINING",
                     style        = MaterialTheme.typography.labelSmall,
                     color        = TextSecondary,
                     letterSpacing = 1.sp)

                DataRingGauge(
                    usedFraction = dashVm.usagePercent(),
                    remainingGb  = dashVm.remainingGb(),
                    modifier     = Modifier.size(200.dp)
                )

                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Used: ${"%.2f".format(dashState.usedGb)} GB",
                             style = MaterialTheme.typography.bodySmall,
                             color = TextSecondary)
                        Text("Plan: ${trip.selectedPlan.dataGB} GB",
                             style = MaterialTheme.typography.bodySmall,
                             color = TextSecondary)
                    }
                    LinearProgressIndicator(
                        progress   = { dashVm.usagePercent() },
                        modifier   = Modifier.fillMaxWidth().height(6.dp),
                        color      = gaugeColor(dashVm.usagePercent()),
                        trackColor = AppSurface2,
                        strokeCap  = StrokeCap.Round
                    )
                }
            }
        }

        // ── Stats row ─────────────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label    = "Days Left",
                value    = "${"%.1f".format(dashVm.daysLeft())}",
                unit     = "days",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label    = "Burn Rate",
                value    = dashVm.burnRateGbPerDay()?.let { "${"%.2f".format(it)}" } ?: "--",
                unit     = "GB/day",
                modifier = Modifier.weight(1f)
            )
            RiskCard(
                level    = dashState.riskLevel,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Pace warning ──────────────────────────────────────────────────────
        val predictedDays = dashVm.daysUntilEmpty()
        if (predictedDays != null && predictedDays < dashVm.daysLeft()) {
            Card(
                colors    = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.08f)),
                shape     = RoundedCornerShape(12.dp),
                border    = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text     = "⚡ At this pace, plan may run out in " +
                               "${"%.1f".format(predictedDays)} days — " +
                               "${dashVm.daysLeft().toInt()} trip days remain.",
                    modifier = Modifier.padding(14.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = ErrorRed
                )
            }
        }

        // ── Device status ─────────────────────────────────────────────────────
        Text("DEVICE STATUS",
             style        = MaterialTheme.typography.labelSmall,
             color        = TextSecondary,
             letterSpacing = 1.sp)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val batt     = dashState.deviceStatus.batteryLevel
            val charging = dashState.deviceStatus.isCharging
            DeviceCard(
                icon     = if (charging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryFull,
                iconTint = battColor(batt, charging),
                label    = "Battery",
                value    = "$batt%",
                subValue = if (charging) "Charging" else battLabel(batt),
                modifier = Modifier.weight(1f)
            )
            val net = dashState.deviceStatus
            DeviceCard(
                icon     = Icons.Filled.NetworkCheck,
                iconTint = netColor(net.networkStrength),
                label    = "Network",
                value    = net.networkType,
                subValue = net.networkStrength.name.lowercase().replaceFirstChar { it.uppercase() },
                modifier = Modifier.weight(1f)
            )
        }

        // ── End Trip button ───────────────────────────────────────────────────
        Button(
            onClick  = { showEndTripDialog = true },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.12f))
        ) {
            Text("End Trip",
                 style = MaterialTheme.typography.bodyMedium,
                 color = ErrorRed)
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Canvas ring ───────────────────────────────────────────────────────────────

@Composable
private fun DataRingGauge(
    usedFraction: Float,
    remainingGb: Double,
    modifier: Modifier = Modifier
) {
    val color = gaugeColor(usedFraction)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke  = size.width * 0.085f
            val pad     = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(pad, pad)
            // Background track
            drawArc(CardBorder, 135f, 270f, false, topLeft, arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round))
            // Progress
            if (usedFraction > 0f)
                drawArc(color, 135f, 270f * usedFraction, false, topLeft, arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${"%.1f".format(remainingGb)}",
                 style = MaterialTheme.typography.displaySmall,
                 color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("GB left", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("${"%.0f".format((1f - usedFraction) * 100)}%",
                 style = MaterialTheme.typography.bodyMedium,
                 color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Stat cards ────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label: String, value: String, unit: String, modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = AppSurface),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                 color = TextSecondary, letterSpacing = 0.5.sp)
            Text(value, style = MaterialTheme.typography.titleLarge,
                 color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun RiskCard(level: RiskLevel, modifier: Modifier = Modifier) {
    val (bg, fg) = when (level) {
        RiskLevel.SAFE     -> Pair(SuccessGreen.copy(alpha = 0.10f), SuccessGreen)
        RiskLevel.WARNING  -> Pair(WarningAmber.copy(alpha = 0.10f), WarningAmber)
        RiskLevel.CRITICAL -> Pair(ErrorRed.copy(alpha = 0.10f),    ErrorRed)
    }
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = bg),
        shape     = RoundedCornerShape(12.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("RISK", style = MaterialTheme.typography.labelSmall,
                 color = TextSecondary, letterSpacing = 0.5.sp)
            Text(level.label, style = MaterialTheme.typography.titleSmall,
                 color = fg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DeviceCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = AppSurface),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null,
                     tint = iconTint, modifier = Modifier.size(20.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text(value, style = MaterialTheme.typography.titleLarge,
                 color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(subValue, style = MaterialTheme.typography.bodySmall, color = iconTint)
        }
    }
}

// ── Color helpers ─────────────────────────────────────────────────────────────

private fun gaugeColor(f: Float) = when {
    f >= 0.75f -> ErrorRed
    f >= 0.50f -> WarningAmber
    else       -> SuccessGreen
}
private fun battColor(level: Int, charging: Boolean) = when {
    charging   -> SuccessGreen
    level < 20 -> ErrorRed
    level < 40 -> WarningAmber
    else       -> SuccessGreen
}
private fun battLabel(level: Int) = when {
    level < 20 -> "Low battery"
    level < 40 -> "Moderate"
    else       -> "Good"
}
private fun netColor(s: NetworkStrength) = when (s) {
    NetworkStrength.STRONG   -> SuccessGreen
    NetworkStrength.MODERATE -> WarningAmber
    NetworkStrength.WEAK     -> ErrorRed
    NetworkStrength.NONE     -> Color.Gray
}
