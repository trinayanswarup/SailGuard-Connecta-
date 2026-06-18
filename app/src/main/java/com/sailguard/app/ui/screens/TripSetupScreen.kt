package com.sailguard.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.TripHistoryEntity
import com.sailguard.app.data.model.UsageStyle
import com.sailguard.app.data.repository.PlanRepository
import com.sailguard.app.data.repository.Region
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.AppSurface2
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.NearBlack
import com.sailguard.app.ui.theme.SailyYellow
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.ui.theme.WarningAmber
import com.sailguard.app.viewmodel.HistoryViewModel
import com.sailguard.app.viewmodel.TripViewModel
import com.sailguard.app.viewmodel.UsageSliders
import com.sailguard.app.viewmodel.UsageViewModel

@Composable
fun TripSetupScreen(
    vm:         TripViewModel,
    usageVm:    UsageViewModel,
    historyVm:  HistoryViewModel,
    onGoToCart: () -> Unit
) {
    val state   by vm.state.collectAsState()
    val sliders by usageVm.sliders.collectAsState()

    var step by rememberSaveable { mutableStateOf(1) }

    val canAdvance = when (step) {
        1    -> state.destination.isNotEmpty()
        else -> true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        WizardStepBar(
            currentStep = step,
            modifier    = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        )

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                }
            },
            modifier = Modifier.weight(1f),
            label    = "wizard_step"
        ) { targetStep ->
            when (targetStep) {
                1    -> Step1Content(state, vm)
                2    -> Step2Content(sliders, usageVm, state.durationDays)
                else -> Step3Content(state, sliders, vm, historyVm)
            }
        }

        Surface(
            color           = AppSurface,
            shadowElevation = 8.dp,
            tonalElevation  = 0.dp
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick  = { step-- },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("← Back", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
                Button(
                    onClick  = {
                        if (step < 3) step++
                        else onGoToCart()
                    },
                    enabled  = canAdvance,
                    modifier = Modifier.weight(if (step > 1) 2f else 1f).height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = SailyYellow,
                        disabledContainerColor = AppSurface2
                    )
                ) {
                    Text(
                        text       = if (step < 3) "Next →" else "Review Order →",
                        color      = if (canAdvance) Color(0xFF0D0D0D) else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Destination, Duration, Usage Style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Step1Content(
    state: com.sailguard.app.viewmodel.TripSetupState,
    vm:    TripViewModel
) {
    val linkCode       by vm.linkCode.collectAsState()
    var linkExpanded   by rememberSaveable { mutableStateOf(false) }
    var linkCodeInput  by rememberSaveable { mutableStateOf(linkCode ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text("⛵ SailGuard",
                 style         = MaterialTheme.typography.titleSmall,
                 color         = NearBlack,
                 fontWeight    = FontWeight.Bold,
                 letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text("Where are you headed?",
                 style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text("Select your destination, trip length, and usage style.",
                 style = MaterialTheme.typography.bodyMedium,    color = TextSecondary)
        }

        // ── Destination picker ────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Destination")
            DestinationPicker(
                selectedDestination = state.destination,
                onSelect            = { vm.setDestination(it) },
                onSelectRegion      = { vm.setRegion(it) }
            )
        }

        // ── Duration ──────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Trip Duration")
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick  = { if (state.durationDays > 1) vm.setDuration(state.durationDays - 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = SailyYellow)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.durationDays}",
                             style      = MaterialTheme.typography.displaySmall,
                             color      = TextPrimary,
                             fontWeight = FontWeight.Bold)
                        Text("days", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(
                        onClick  = { if (state.durationDays < 60) vm.setDuration(state.durationDays + 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase", tint = SailyYellow)
                    }
                }
            }
        }

        // ── Usage Style ───────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Usage Style")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UsageStyle.entries.forEach { style ->
                    val selected = state.usageStyle == style
                    Surface(
                        modifier = Modifier.weight(1f).clickable { vm.setUsageStyle(style) },
                        shape    = RoundedCornerShape(12.dp),
                        color    = if (selected) SailyYellow else AppSurface,
                        border   = if (selected) null else BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier            = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(style.label,
                                 style      = MaterialTheme.typography.titleSmall,
                                 color      = if (selected) NearBlack else TextPrimary,
                                 fontWeight = FontWeight.SemiBold)
                            Text("${"%.0f".format(style.dailyGb * 1000)} MB/day",
                                 style = MaterialTheme.typography.labelSmall,
                                 color = if (selected) NearBlack.copy(alpha = 0.7f) else TextSecondary)
                        }
                    }
                }
            }
        }

        // ── Link to Connecta (optional) ───────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Link to Connecta (optional)")
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (linkCode != null) SailyYellow.copy(alpha = 0.5f) else CardBorder)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { linkExpanded = !linkExpanded }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (linkCode != null) "Session ID linked" else "Not linked",
                                style      = MaterialTheme.typography.titleSmall,
                                color      = if (linkCode != null) SailyYellow else TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Sync trips to Connecta web dashboard",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            imageVector        = if (linkExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint               = TextSecondary,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = linkExpanded,
                        enter   = expandVertically() + fadeIn(),
                        exit    = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier            = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value         = linkCodeInput,
                                onValueChange = {
                                    linkCodeInput = it
                                    vm.setLinkCode(it)
                                },
                                placeholder   = { Text("Paste connecta_session_id here", color = TextSecondary) },
                                modifier      = Modifier.fillMaxWidth(),
                                singleLine    = true,
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor        = TextPrimary,
                                    unfocusedTextColor      = TextPrimary,
                                    focusedBorderColor      = SailyYellow,
                                    unfocusedBorderColor    = CardBorder,
                                    focusedContainerColor   = AppSurface,
                                    unfocusedContainerColor = AppSurface
                                )
                            )
                            Text(
                                "Find this in Connecta web app → Settings → Session ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Destination Picker ────────────────────────────────────────────────────────

@Composable
private fun DestinationPicker(
    selectedDestination: String,
    onSelect:            (String) -> Unit,
    onSelectRegion:      (Region) -> Unit
) {
    val isRegionDest   = Region.entries.any { PlanRepository.regionDisplayName(it) == selectedDestination }
    var searchText     by remember { mutableStateOf(if (selectedDestination.isNotEmpty() && !isRegionDest) selectedDestination else "") }
    var expandedRegion by remember { mutableStateOf<Region?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Search bar
        OutlinedTextField(
            value         = searchText,
            onValueChange = { searchText = it },
            placeholder   = { Text("Search country... e.g. Thailand", color = TextSecondary) },
            leadingIcon   = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                focusedBorderColor      = SailyYellow,
                unfocusedBorderColor    = CardBorder,
                focusedContainerColor   = AppSurface,
                unfocusedContainerColor = AppSurface
            )
        )

        if (searchText.isNotEmpty() &&
            (selectedDestination.isEmpty() || searchText != selectedDestination)) {
            // Search results
            val matches = PlanRepository.countries
                .filter { it.name.contains(searchText, ignoreCase = true) }
            if (matches.isNotEmpty()) {
                Card(
                    colors    = CardDefaults.cardColors(containerColor = AppSurface),
                    shape     = RoundedCornerShape(12.dp),
                    border    = BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        matches.take(8).forEach { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(country.name)
                                        searchText = country.name
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(country.flag, fontSize = 20.sp)
                                Column {
                                    Text(country.name,
                                         style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    Text(PlanRepository.regionForCountry(country.name).displayName,
                                         style = MaterialTheme.typography.labelSmall,  color = TextSecondary)
                                }
                                if (country.name == selectedDestination) {
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Filled.Check, contentDescription = null,
                                         tint = SailyYellow, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Region cards
            Region.entries.forEach { region ->
                RegionCard(
                    region               = region,
                    isExpanded           = expandedRegion == region,
                    selectedDestination  = selectedDestination,
                    isRegionSelected     = selectedDestination == PlanRepository.regionDisplayName(region),
                    onSelectRegion       = { onSelectRegion(region) },
                    onToggle             = {
                        expandedRegion = if (expandedRegion == region) null else region
                    },
                    onSelectCountry      = { name ->
                        onSelect(name)
                        searchText = name
                    }
                )
            }
        }
    }
}

@Composable
private fun RegionCard(
    region:              Region,
    isExpanded:          Boolean,
    selectedDestination: String,
    isRegionSelected:    Boolean,
    onSelectRegion:      () -> Unit,
    onToggle:            () -> Unit,
    onSelectCountry:     (String) -> Unit
) {
    val countries   = PlanRepository.countriesInRegion(region)
    val hasCountry  = countries.any { it.name == selectedDestination }
    val highlighted = isRegionSelected || hasCountry

    Card(
        colors    = CardDefaults.cardColors(
            containerColor = if (highlighted) SailyYellow.copy(alpha = 0.12f) else AppSurface),
        shape     = RoundedCornerShape(14.dp),
        border    = BorderStroke(1.dp, if (highlighted) SailyYellow else CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Header: left area selects the whole region; right chevron expands country list
            Row(
                modifier              = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    modifier              = Modifier.weight(1f).clickable(onClick = onSelectRegion),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(region.emoji, fontSize = 26.sp)
                    Column {
                        Text(region.displayName,
                             style      = MaterialTheme.typography.titleSmall,
                             color      = if (isRegionSelected) NearBlack else TextPrimary,
                             fontWeight = FontWeight.SemiBold)
                        Text(region.description,
                             style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
                when {
                    isRegionSelected -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = SailyYellow) {
                            Text("Region Plan",
                                 modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                 style      = MaterialTheme.typography.labelSmall,
                                 color      = NearBlack,
                                 fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    hasCountry -> {
                        val sel = countries.find { it.name == selectedDestination }
                        if (sel != null) {
                            Text("${sel.flag} ${sel.name}",
                                 style = MaterialTheme.typography.labelSmall, color = NearBlack)
                            Spacer(Modifier.width(6.dp))
                        }
                    }
                }
                IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector        = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint               = TextSecondary,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded country list
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    countries.forEach { country ->
                        val isSelected = country.name == selectedDestination
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) SailyYellow.copy(alpha = 0.20f)
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectCountry(country.name) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(country.flag, fontSize = 18.sp)
                            Text(country.name,
                                 style    = MaterialTheme.typography.bodyMedium,
                                 color    = if (isSelected) NearBlack else TextPrimary,
                                 modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null,
                                     tint = SailyYellow, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Usage Sliders (Feature 3: updated MB/hr rates)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Step2Content(
    sliders:  UsageSliders,
    usageVm:  UsageViewModel,
    tripDays: Int
) {
    val dailyGb = sliders.dailyGb
    val totalGb = dailyGb * tripDays

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text("Fine-tune Your Usage",
                 style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text("Drag sliders to reflect your daily habits on the road.",
                 style = MaterialTheme.typography.bodyMedium,    color = TextSecondary)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape  = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardBorder)
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
                    mbPerHr  = 933,
                    onChange = { usageVm.setVideoStreaming(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.MyLocation,
                    label    = "Maps & Navigation",
                    subLabel = "Google Maps, Waze",
                    value    = sliders.maps,
                    hours    = sliders.mapsHrs(),
                    mbPerHr  = 10,
                    onChange = { usageVm.setMaps(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.VideoCameraFront,
                    label    = "Video Calls",
                    subLabel = "FaceTime, WhatsApp",
                    value    = sliders.videoCalls,
                    hours    = sliders.videoCallsHrs(),
                    mbPerHr  = 933,
                    onChange = { usageVm.setVideoCalls(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.Share,
                    label    = "Social Media",
                    subLabel = "Instagram, TikTok",
                    value    = sliders.socialMedia,
                    hours    = sliders.socialMediaHrs(),
                    mbPerHr  = 140,
                    onChange = { usageVm.setSocialMedia(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.Wifi,
                    label    = "Mobile Hotspot",
                    subLabel = "Sharing data with devices",
                    value    = sliders.hotspot,
                    hours    = sliders.hotspotHrs(),
                    mbPerHr  = 500,
                    onChange = { usageVm.setHotspot(it) }
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EstimateCard("Daily Usage",  "${"%.2f".format(dailyGb)} GB",
                         "${"%.0f".format(dailyGb * 1024)} MB/day", Modifier.weight(1f))
            EstimateCard("Trip Total",   "${"%.1f".format(totalGb)} GB",
                         "for $tripDays days",                       Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3 — Plan Review (Features 5 & 7)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Step3Content(
    state:     com.sailguard.app.viewmodel.TripSetupState,
    sliders:   UsageSliders,
    vm:        TripViewModel,
    historyVm: HistoryViewModel
) {
    val trips by historyVm.trips.collectAsState()

    // ── History-based recommendation logic (Feature 7) ────────────────────────
    val sameCountryTrips = remember(trips, state.destination) {
        trips.filter { it.destination == state.destination }
    }
    val sameCountryDailyGb: Double? = remember(sameCountryTrips) {
        if (sameCountryTrips.isEmpty()) null
        else sameCountryTrips.sumOf { it.actualGbUsed / it.tripDays.coerceAtLeast(1) } /
             sameCountryTrips.size
    }
    val allTripsDailyGb: Double? = remember(trips) {
        if (trips.isEmpty()) null
        else trips.sumOf { it.actualGbUsed / it.tripDays.coerceAtLeast(1) } / trips.size
    }

    val blend: BlendResult = remember(sameCountryDailyGb, allTripsDailyGb, sliders.dailyGb) {
        when {
            sameCountryDailyGb != null -> BlendResult(
                blendedDailyGb = sliders.dailyGb * 0.40 + sameCountryDailyGb * 0.60,
                historyDailyGb = sameCountryDailyGb,
                tripCount      = sameCountryTrips.size,
                isSameCountry  = true,
                lastTrip       = sameCountryTrips.firstOrNull()
            )
            allTripsDailyGb != null -> BlendResult(
                blendedDailyGb = sliders.dailyGb * 0.60 + allTripsDailyGb * 0.40,
                historyDailyGb = allTripsDailyGb,
                tripCount      = trips.size,
                isSameCountry  = false,
                lastTrip       = null
            )
            else -> BlendResult(null, null, 0, false, null)
        }
    }

    val blendedRecommendedPlan: SailyPlan? = remember(blend, state.availablePlans, state.durationDays) {
        val bd = blend.blendedDailyGb ?: return@remember null
        val needed = bd * state.durationDays * 1.2
        state.availablePlans.filter { !it.isUnlimited && it.dataGB >= needed }
            .minByOrNull { it.priceUSD }
            ?: state.availablePlans.filter { !it.isUnlimited }.maxByOrNull { it.dataGB }
    }

    // Best value plan = highest GB / price ratio among data plans
    val bestValuePlan: SailyPlan? = remember(state.availablePlans) {
        state.availablePlans.filter { !it.isUnlimited && it.dataGB > 0 }
            .maxByOrNull { it.dataGB / it.priceUSD }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Review Your Plan",
                 style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            Text("${state.flag}  ${state.destination}  ·  ${state.durationDays} days",
                 style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        if (state.availablePlans.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text("No plans available. Go back and try another destination.",
                     modifier = Modifier.padding(16.dp),
                     style    = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        } else {
            // ── Smart history card (Feature 7) ────────────────────────────────
            if (blend.blendedDailyGb != null && blend.historyDailyGb != null &&
                blendedRecommendedPlan != null) {
                SmartHistoryCard(
                    blend                  = blend,
                    sliderDailyGb          = sliders.dailyGb,
                    durationDays           = state.durationDays,
                    recommendedGb          = if (blendedRecommendedPlan.isUnlimited) -1.0
                                             else blendedRecommendedPlan.dataGB,
                    destination            = state.destination
                )
            }

            // ── All plans (Feature 5: Saily-style cards) ──────────────────────
            SectionLabel("Available Plans")

            state.availablePlans.forEach { plan ->
                SailyPlanCard(
                    plan           = plan,
                    isSelected     = state.selectedPlan?.id == plan.id ||
                                     (state.selectedPlan == null && plan.id == state.suggestedPlan?.id),
                    isBestChoice   = plan.id == bestValuePlan?.id,
                    isBlendMatch   = plan.id == blendedRecommendedPlan?.id && blend.blendedDailyGb != null,
                    onSelect       = { vm.selectPlan(it) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private data class BlendResult(
    val blendedDailyGb: Double?,
    val historyDailyGb: Double?,
    val tripCount:      Int,
    val isSameCountry:  Boolean,
    val lastTrip:       TripHistoryEntity?
)

// ── Smart history insight card ────────────────────────────────────────────────

@Composable
private fun SmartHistoryCard(
    blend:             BlendResult,
    sliderDailyGb:     Double,
    durationDays:      Int,
    recommendedGb:     Double,
    destination:       String
) {
    val historyGb  = blend.historyDailyGb ?: return
    val blendedGb  = blend.blendedDailyGb ?: return
    val recLabel   = if (recommendedGb < 0) "Unlimited" else "${recommendedGb.toLong()} GB"
    val histWeight = if (blend.isSameCountry) 60 else 40
    val sliderWeight = 100 - histWeight

    Card(
        colors = CardDefaults.cardColors(containerColor = SailyYellow.copy(alpha = 0.10f)),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SailyYellow.copy(alpha = 0.5f))
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Based on your usage history + current settings, we recommend $recLabel",
                style      = MaterialTheme.typography.titleSmall,
                color      = NearBlack,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val tripWord = if (blend.tripCount == 1) "trip" else "trips"
                if (blend.isSameCountry) {
                    HistoryInsightLine(
                        "Your ${blend.tripCount} past $tripWord to $destination averaged " +
                        "${"%.2f".format(historyGb)} GB/day"
                    )
                    blend.lastTrip?.let { t ->
                        HistoryInsightLine(
                            "Last trip: used ${"%.2f".format(t.actualGbUsed)} GB in ${t.tripDays} days"
                        )
                    }
                } else {
                    HistoryInsightLine(
                        "Your ${blend.tripCount} past $tripWord averaged " +
                        "${"%.2f".format(historyGb)} GB/day overall"
                    )
                }
                HistoryInsightLine("Slider estimate: ${"%.2f".format(sliderDailyGb)} GB/day")
                HistoryInsightLine(
                    "$histWeight% history + $sliderWeight% slider = " +
                    "${"%.2f".format(blendedGb)} GB/day blended"
                )
                HistoryInsightLine(
                    "${"%.2f".format(blendedGb)} × $durationDays days × 1.2 buffer = " +
                    "${"%.1f".format(blendedGb * durationDays * 1.2)} GB needed → $recLabel"
                )
            }
        }
    }
}

@Composable
private fun HistoryInsightLine(text: String) {
    Text("· $text", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
}

// ── Saily-style plan card (Feature 5) ─────────────────────────────────────────

@Composable
private fun SailyPlanCard(
    plan:         SailyPlan,
    isSelected:   Boolean,
    isBestChoice: Boolean,
    isBlendMatch: Boolean,
    onSelect:     (SailyPlan) -> Unit
) {
    var selectedDays by remember { mutableStateOf(15) }
    val displayPrice = if (plan.isUnlimited)
        plan.unlimitedPrices[selectedDays] ?: PlanRepository.unlimitedPriceForDays(plan.priceUSD, selectedDays)
    else plan.priceUSD

    val borderColor    = when {
        isSelected   -> SailyYellow
        isBestChoice -> SailyYellow.copy(alpha = 0.6f)
        else         -> CardBorder
    }
    val borderWidth    = if (isSelected || isBestChoice) 2.dp else 1.dp
    val containerColor = when {
        isBestChoice && !isSelected -> NearBlack
        isSelected                  -> SailyYellow.copy(alpha = 0.10f)
        else                        -> AppSurface
    }
    val textOnDark = isBestChoice && !isSelected

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onSelect(plan) },
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        shape     = RoundedCornerShape(14.dp),
        border    = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Badges row ────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isBestChoice) {
                    PlanBadge("Best Choice", NearBlack, SailyYellow)
                }
                if (isBlendMatch && !isBestChoice) {
                    PlanBadge("History Pick", NearBlack, SailyYellow.copy(alpha = 0.20f))
                }
            }

            // ── GB + price ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = if (plan.isUnlimited) "Unlimited" else "${plan.dataGB.toLong()} GB",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = if (textOnDark) Color.White else TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$${"%.2f".format(displayPrice)}",
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = if (textOnDark) Color.White else SailyYellow,
                        fontWeight = FontWeight.Bold
                    )
                    Text("USD",
                         style = MaterialTheme.typography.labelSmall,
                         color = if (textOnDark) Color.White.copy(0.6f) else TextSecondary)
                }
            }

            // ── Unlimited: days selector ──────────────────────────────────────
            if (plan.isUnlimited) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7, 15, 30, 90).forEach { days ->
                        val active = selectedDays == days
                        Surface(
                            modifier = Modifier.clickable { selectedDays = days },
                            shape    = RoundedCornerShape(6.dp),
                            color    = if (active) SailyYellow else AppSurface2,
                            border   = if (active) null else BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                "${days}d",
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style      = MaterialTheme.typography.labelSmall,
                                color      = if (active) NearBlack else TextSecondary,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // Validity
                Text(
                    "${plan.validDays} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (textOnDark) Color.White.copy(0.7f) else TextSecondary
                )
            }

            // ── Bottom row: Saily credits + network + selected check ──────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 3% Saily credits badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (textOnDark) SailyYellow.copy(0.3f) else SailyYellow.copy(0.12f)
                    ) {
                        Text(
                            "3% Saily credits",
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (textOnDark) Color.White else NearBlack,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(plan.network,
                         style = MaterialTheme.typography.labelSmall,
                         color = if (textOnDark) Color.White.copy(0.6f) else TextSecondary)
                }
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = "Selected",
                         tint = SailyYellow, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun PlanBadge(label: String, textColor: Color, bgColor: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Text(
            label,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style      = MaterialTheme.typography.labelSmall,
            color      = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wizard step bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStepBar(currentStep: Int, modifier: Modifier = Modifier) {
    val labels = listOf("Destination", "Usage", "Plan")
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        labels.forEachIndexed { index, label ->
            val stepNum   = index + 1
            val isDone    = stepNum < currentStep
            val isCurrent = stepNum == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.width(56.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(28.dp)
                        .background(
                            if (isDone || isCurrent) SailyYellow else AppSurface2,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Filled.Check, contentDescription = null,
                             tint = NearBlack, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            "$stepNum",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (isCurrent) NearBlack else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = if (stepNum <= currentStep) NearBlack else TextSecondary,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            if (index < labels.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 13.dp)
                        .height(2.dp)
                        .background(if (currentStep > stepNum) SailyYellow else CardBorder)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = TextSecondary,
        letterSpacing = 1.sp
    )
}

@Composable
private fun UsageSliderRow(
    icon:     ImageVector,
    label:    String,
    subLabel: String,
    value:    Float,
    hours:    Float,
    mbPerHr:  Int,
    onChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = SailyYellow, modifier = Modifier.size(20.dp))
                Column {
                    Text(label,    style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(subLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(hours)} hrs/day",
                     style = MaterialTheme.typography.bodySmall, color = NearBlack,
                     fontWeight = FontWeight.SemiBold)
                Text("~${"%.0f".format(hours * mbPerHr)} MB",
                     style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Slider(
            value         = value,
            onValueChange = onChange,
            colors        = SliderDefaults.colors(
                thumbColor         = SailyYellow,
                activeTrackColor   = SailyYellow,
                inactiveTrackColor = AppSurface2
            )
        )
    }
}

@Composable
private fun EstimateCard(
    label:    String,
    value:    String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = AppSurface),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.headlineSmall,
                 color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(subValue, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
