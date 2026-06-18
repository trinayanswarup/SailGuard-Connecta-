package com.sailguard.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
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
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.AppSurface2
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.ConnectaOrange
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.ui.theme.WarningAmber
import com.sailguard.app.viewmodel.TripViewModel
import com.sailguard.app.viewmodel.UsageViewModel

@Composable
fun UsageEstimatorScreen(usageVm: UsageViewModel, tripVm: TripViewModel) {
    val sliders   by usageVm.sliders.collectAsState()
    val tripState by tripVm.state.collectAsState()

    val dailyGb    = sliders.dailyGb
    val tripDays   = tripState.durationDays
    val totalGb    = dailyGb * tripDays
    val planGb     = tripState.selectedPlan?.dataGB ?: 0.0
    val sufficient = planGb >= totalGb

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Column {
            Text("Usage Estimator",
                 style = MaterialTheme.typography.headlineSmall,
                 color = TextPrimary)
            Text("Adjust sliders to reflect your daily habits.",
                 style = MaterialTheme.typography.bodyMedium,
                 color = TextSecondary)
        }

        // ── Sliders ───────────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape  = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                UsageSliderRow(
                    icon     = Icons.Filled.Movie,
                    label    = "Video Streaming",
                    subLabel = "HD, Netflix/YouTube",
                    value    = sliders.videoStreaming,
                    hours    = sliders.videoStreamingHrs(),
                    maxHours = 4f,
                    mbPerHr  = 700,
                    onChange = { usageVm.setVideoStreaming(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.MyLocation,
                    label    = "Maps & Navigation",
                    subLabel = "Google Maps, Waze",
                    value    = sliders.maps,
                    hours    = sliders.mapsHrs(),
                    maxHours = 4f,
                    mbPerHr  = 20,
                    onChange = { usageVm.setMaps(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.VideoCameraFront,
                    label    = "Video Calls",
                    subLabel = "FaceTime, WhatsApp",
                    value    = sliders.videoCalls,
                    hours    = sliders.videoCallsHrs(),
                    maxHours = 2f,
                    mbPerHr  = 300,
                    onChange = { usageVm.setVideoCalls(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.Share,
                    label    = "Social Media",
                    subLabel = "Instagram, TikTok",
                    value    = sliders.socialMedia,
                    hours    = sliders.socialMediaHrs(),
                    maxHours = 4f,
                    mbPerHr  = 100,
                    onChange = { usageVm.setSocialMedia(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.Wifi,
                    label    = "Mobile Hotspot",
                    subLabel = "Sharing data with devices",
                    value    = sliders.hotspot,
                    hours    = sliders.hotspotHrs(),
                    maxHours = 4f,
                    mbPerHr  = 500,
                    onChange = { usageVm.setHotspot(it) }
                )
            }
        }

        // ── Estimates ─────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EstimateCard(
                label    = "Daily Usage",
                value    = "${"%.2f".format(dailyGb)} GB",
                subValue = "${"%.0f".format(dailyGb * 1024)} MB",
                modifier = Modifier.weight(1f)
            )
            EstimateCard(
                label    = "Total ($tripDays days)",
                value    = "${"%.1f".format(totalGb)} GB",
                subValue = "${"%.0f".format(totalGb * 1024)} MB",
                modifier = Modifier.weight(1f)
            )
        }

        // ── Plan compatibility ────────────────────────────────────────────────
        if (planGb > 0.0) {
            CompatibilityBanner(
                planGb     = planGb,
                neededGb   = totalGb,
                sufficient = sufficient
            )
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text     = "Set up a trip on the Setup tab to see plan compatibility.",
                    modifier = Modifier.padding(14.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun UsageSliderRow(
    icon: ImageVector,
    label: String,
    subLabel: String,
    value: Float,
    hours: Float,
    maxHours: Float,
    mbPerHr: Int,
    onChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null,
                     tint = NearBlack, modifier = Modifier.size(20.dp))
                Column {
                    Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(subLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${"%.1f".format(hours)} hrs/day",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = NearBlack,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "~${"%.0f".format(hours * mbPerHr)} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
        Slider(
            value         = value,
            onValueChange = onChange,
            colors        = SliderDefaults.colors(
                thumbColor         = ConnectaOrange,
                activeTrackColor   = ConnectaOrange,
                inactiveTrackColor = AppSurface2
            )
        )
    }
}

@Composable
private fun EstimateCard(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = AppSurface),
        shape    = RoundedCornerShape(14.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label.uppercase(),
                 style = MaterialTheme.typography.labelSmall,
                 color = TextSecondary)
            Text(value,
                 style      = MaterialTheme.typography.headlineSmall,
                 color      = TextPrimary,
                 fontWeight = FontWeight.Bold)
            Text(subValue,
                 style = MaterialTheme.typography.bodySmall,
                 color = TextSecondary)
        }
    }
}

@Composable
private fun CompatibilityBanner(
    planGb: Double,
    neededGb: Double,
    sufficient: Boolean
) {
    val bgColor  = if (sufficient) SuccessGreen.copy(alpha = 0.10f) else ErrorRed.copy(alpha = 0.10f)
    val border   = if (sufficient) SuccessGreen else ErrorRed
    val icon     = if (sufficient) "✓" else "⚠"
    val headline = if (sufficient) "Plan is sufficient" else "Plan may not be enough"
    val detail   = if (sufficient)
        "Your ${planGb}GB Connecta plan covers the estimated ${"%.1f".format(neededGb)} GB. " +
        "You have ${"%.1f".format(planGb - neededGb)} GB headroom."
    else
        "You need ~${"%.1f".format(neededGb)} GB but your plan is only ${planGb} GB. " +
        "Consider upgrading or reducing usage."

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape  = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Text(icon, fontSize = 20.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(headline,
                     style      = MaterialTheme.typography.titleSmall,
                     color      = TextPrimary,
                     fontWeight = FontWeight.SemiBold)
                Text(detail,
                     style = MaterialTheme.typography.bodySmall,
                     color = TextSecondary)
            }
        }
    }
}
