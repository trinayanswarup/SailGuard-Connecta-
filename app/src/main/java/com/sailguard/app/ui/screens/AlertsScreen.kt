package com.sailguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sailguard.app.data.model.Alert
import com.sailguard.app.data.model.AlertSeverity
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.InfoBlue
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.ConnectaOrange
import com.sailguard.app.ui.theme.TealPrimary
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.ui.theme.WarningAmber
import com.sailguard.app.viewmodel.DashboardViewModel
import com.sailguard.app.viewmodel.SmartModeViewModel
import com.sailguard.app.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm  dd MMM", Locale.getDefault())

@Composable
fun AlertsScreen(
    tripVm: TripViewModel,
    dashVm: DashboardViewModel,
    smartVm: SmartModeViewModel
) {
    val tripAlerts by tripVm.alerts.collectAsState()
    val dashState  by dashVm.state.collectAsState()
    val smartState by smartVm.state.collectAsState()

    val allAlerts = remember(tripAlerts, dashState.dashAlerts, smartState.smartAlerts) {
        (tripAlerts + dashState.dashAlerts + smartState.smartAlerts)
            .sortedByDescending { it.timestamp }
    }

    var filter by remember { mutableStateOf<AlertSeverity?>(null) }
    val displayed = if (filter == null) allAlerts else allAlerts.filter { it.severity == filter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("Alerts",
                     style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Text("${allAlerts.size} total",
                     style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (allAlerts.any { it.severity == AlertSeverity.CRITICAL }) ErrorRed
                        else if (allAlerts.any { it.severity == AlertSeverity.WARNING }) WarningAmber
                        else SuccessGreen,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${allAlerts.size}",
                    style      = MaterialTheme.typography.titleSmall,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Filter chips ──────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("All",      filter == null, ConnectaOrange, selectedTextColor = NearBlack) { filter = null }
            FilterChip("Info",     filter == AlertSeverity.INFO,           InfoBlue)     { filter = AlertSeverity.INFO }
            FilterChip("Warning",  filter == AlertSeverity.WARNING,        WarningAmber) { filter = AlertSeverity.WARNING }
            FilterChip("Critical", filter == AlertSeverity.CRITICAL,       ErrorRed)     { filter = AlertSeverity.CRITICAL }
        }

        Spacer(Modifier.height(16.dp))

        // ── List ──────────────────────────────────────────────────────────────
        if (displayed.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null,
                         tint = SuccessGreen, modifier = Modifier.size(48.dp))
                    Text(
                        if (filter == null) "No alerts — all clear!"
                        else "No ${filter?.name?.lowercase()} alerts",
                        style = MaterialTheme.typography.bodyLarge, color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayed, key = { it.id }) { alert ->
                    AlertCard(alert)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── Alert card ────────────────────────────────────────────────────────────────

@Composable
private fun AlertCard(alert: Alert) {
    val (color, icon) = when (alert.severity) {
        AlertSeverity.INFO     -> Pair(InfoBlue,     Icons.Filled.Info)
        AlertSeverity.WARNING  -> Pair(WarningAmber, Icons.Filled.Warning)
        AlertSeverity.CRITICAL -> Pair(ErrorRed,     Icons.Filled.Error)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape  = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Icon(icon, contentDescription = null,
                 tint = color, modifier = Modifier.size(22.dp))
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(alert.title,
                         style      = MaterialTheme.typography.titleSmall,
                         color      = TextPrimary,
                         fontWeight = FontWeight.SemiBold,
                         modifier   = Modifier.weight(1f))
                    Text(timeFormat.format(Date(alert.timestamp)),
                         style = MaterialTheme.typography.labelSmall,
                         color = TextSecondary)
                }
                Text(alert.message,
                     style = MaterialTheme.typography.bodySmall,
                     color = TextSecondary)
            }
        }
    }
}

// ── Filter chip ───────────────────────────────────────────────────────────────

@Composable
private fun FilterChip(
    label:             String,
    selected:          Boolean,
    color:             Color,
    selectedTextColor: Color = Color.White,
    onClick:           () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(8.dp),
        color    = if (selected) color else AppSurface,
        border   = if (selected) null
                   else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Text(
            text       = label,
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style      = MaterialTheme.typography.labelSmall,
            color      = if (selected) selectedTextColor else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
