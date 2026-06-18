package com.sailguard.app.ui.screens

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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sailguard.app.data.model.NetworkStrength
import com.sailguard.app.data.model.SmartRecommendation
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.AppSurface2
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.ConnectaOrange
import com.sailguard.app.ui.theme.TealPrimary
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.ui.theme.WarningAmber
import com.sailguard.app.viewmodel.SmartModeViewModel

@Composable
fun SmartModeScreen(vm: SmartModeViewModel) {
    val state by vm.state.collectAsState()
    val ds    = state.deviceStatus
    val rec   = state.recommendation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("Smart Mode",
                     style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Text("Real-time device monitoring",
                     style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (state.isSmartModeOn) "ON" else "OFF",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = if (state.isSmartModeOn) NearBlack else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked         = state.isSmartModeOn,
                    onCheckedChange = { vm.setSmartMode(it) },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = Color.White,
                        checkedTrackColor   = ConnectaOrange,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = AppSurface2
                    )
                )
            }
        }

        // ── Device status cards ───────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Battery
            Card(
                modifier = Modifier.weight(1f),
                colors   = CardDefaults.cardColors(containerColor = AppSurface),
                shape    = RoundedCornerShape(16.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                ds.isCharging        -> Icons.Filled.BatteryChargingFull
                                ds.batteryLevel < 20 -> Icons.Filled.BatteryAlert
                                else                 -> Icons.Filled.BatteryFull
                            },
                            contentDescription = null,
                            tint     = battColor(ds.batteryLevel, ds.isCharging),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Battery", style = MaterialTheme.typography.labelSmall,
                             color = TextSecondary)
                    }
                    Text(
                        "${ds.batteryLevel}%",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress   = { ds.batteryLevel / 100f },
                        modifier   = Modifier.fillMaxWidth().height(4.dp)
                                             .clip(RoundedCornerShape(2.dp)),
                        color      = battColor(ds.batteryLevel, ds.isCharging),
                        trackColor = AppSurface2,
                        strokeCap  = StrokeCap.Round
                    )
                    Text(
                        if (ds.isCharging) "Charging" else battLabel(ds.batteryLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = battColor(ds.batteryLevel, ds.isCharging)
                    )
                }
            }

            // Network
            Card(
                modifier = Modifier.weight(1f),
                colors   = CardDefaults.cardColors(containerColor = AppSurface),
                shape    = RoundedCornerShape(16.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (ds.networkStrength == NetworkStrength.NONE)
                                              Icons.Filled.WifiOff
                                          else Icons.Filled.NetworkCheck,
                            contentDescription = null,
                            tint     = netColor(ds.networkStrength),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Network", style = MaterialTheme.typography.labelSmall,
                             color = TextSecondary)
                    }
                    Text(
                        ds.networkType,
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    NetworkStrengthDots(ds.networkStrength)
                    Text(
                        ds.networkStrength.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = netColor(ds.networkStrength)
                    )
                }
            }
        }

        // ── Recommendation banner ─────────────────────────────────────────────
        if (state.isSmartModeOn) {
            val (bg, border, icon) = when (rec) {
                SmartRecommendation.NORMAL     -> Triple(SuccessGreen.copy(0.10f), SuccessGreen,  Icons.Filled.CheckCircle)
                SmartRecommendation.REDUCE     -> Triple(WarningAmber.copy(0.10f), WarningAmber,  Icons.Filled.Warning)
                SmartRecommendation.LIGHT_MODE -> Triple(ErrorRed.copy(0.10f),    ErrorRed,       Icons.Filled.Warning)
                SmartRecommendation.CRITICAL   -> Triple(ErrorRed.copy(0.15f),    ErrorRed,       Icons.Filled.Error)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = bg),
                shape  = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, border)
            ) {
                Row(
                    modifier              = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Icon(icon, contentDescription = null, tint = border,
                         modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(rec.label,
                             style      = MaterialTheme.typography.titleMedium,
                             color      = TextPrimary,
                             fontWeight = FontWeight.SemiBold)
                        Text(rec.detail,
                             style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        // ── Reasons list ──────────────────────────────────────────────────────
        if (state.reasons.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("WHY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    state.reasons.forEach { reason ->
                        Row(
                            verticalAlignment     = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null,
                                 tint = NearBlack, modifier = Modifier.size(16.dp))
                            Text(reason, style = MaterialTheme.typography.bodySmall,
                                 color = TextPrimary)
                        }
                    }
                }
            }
        }

        // ── How Smart Mode helps ──────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape  = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("HOW SMART MODE HELPS",
                     style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                SmartTip("Monitors battery level every few seconds")
                SmartTip("Detects WiFi, 4G/LTE, 3G, and 2G signal strength")
                SmartTip("Warns you when battery < 20% or signal is weak")
                SmartTip("Recommends switching to light usage to conserve data & battery")
                SmartTip("Generates alerts for combined low battery + weak signal")
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun NetworkStrengthDots(strength: NetworkStrength) {
    val levels = when (strength) {
        NetworkStrength.NONE     -> 0
        NetworkStrength.WEAK     -> 1
        NetworkStrength.MODERATE -> 2
        NetworkStrength.STRONG   -> 3
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.Bottom
    ) {
        repeat(3) { i ->
            val active = i < levels
            val color  = when {
                active && levels == 1 -> ErrorRed
                active && levels == 2 -> WarningAmber
                active               -> SuccessGreen
                else                 -> CardBorder
            }
            Spacer(
                Modifier
                    .size(width = 10.dp, height = (8 + i * 5).dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun SmartTip(text: String) {
    Row(
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("•", color = ConnectaOrange, style = MaterialTheme.typography.bodySmall)
        Text(text, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

private fun battColor(level: Int, charging: Boolean): Color = when {
    charging    -> SuccessGreen
    level < 20  -> ErrorRed
    level < 40  -> WarningAmber
    else        -> SuccessGreen
}

private fun battLabel(level: Int): String = when {
    level < 20  -> "Low — data usage affected"
    level < 40  -> "Moderate"
    else        -> "Good"
}

private fun netColor(strength: NetworkStrength): Color = when (strength) {
    NetworkStrength.STRONG   -> SuccessGreen
    NetworkStrength.MODERATE -> WarningAmber
    NetworkStrength.WEAK     -> ErrorRed
    NetworkStrength.NONE     -> Color.Gray
}
