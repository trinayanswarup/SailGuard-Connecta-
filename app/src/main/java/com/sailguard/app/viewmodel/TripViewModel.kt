package com.sailguard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sailguard.app.data.model.Alert
import com.sailguard.app.data.model.AlertSeverity
import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.TripConfig
import com.sailguard.app.data.model.UsageStyle
import com.sailguard.app.data.network.ConnectaApiClient
import com.sailguard.app.data.repository.PlanRepository
import com.sailguard.app.data.repository.Region
import com.sailguard.app.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TripSetupState(
    val destination: String    = "",
    val flag: String           = "",
    val durationDays: Int      = 7,
    val usageStyle: UsageStyle = UsageStyle.MEDIUM,
    val suggestedPlan: SailyPlan?    = null,
    val selectedPlan: SailyPlan?     = null,
    val availablePlans: List<SailyPlan> = emptyList(),
    val tripStarted: Boolean   = false,
    val activeTrip: TripConfig? = null,
    val selectedRegion: Region? = null
)

class TripViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsRepository(app)

    private val _state = MutableStateFlow(TripSetupState())
    val state: StateFlow<TripSetupState> = _state.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _linkCode = MutableStateFlow(settings.connectaLinkCode)
    val linkCode: StateFlow<String?> = _linkCode.asStateFlow()

    // ── Setup mutations ───────────────────────────────────────────────────────

    fun setDestination(country: String) {
        val plans     = PlanRepository.getPlansForCountry(country)
        val suggested = PlanRepository.suggestPlan(country, _state.value.durationDays, _state.value.usageStyle)
        val flag      = PlanRepository.flagForCountry(country)
        _state.value  = _state.value.copy(
            destination    = country,
            flag           = flag,
            selectedRegion = null,
            availablePlans = plans,
            suggestedPlan  = suggested,
            selectedPlan   = null
        )
    }

    fun setRegion(region: Region) {
        val plans     = PlanRepository.getRegionalPlans(region)
        val suggested = PlanRepository.suggestPlan(
            PlanRepository.regionDisplayName(region), _state.value.durationDays, _state.value.usageStyle)
        _state.value  = _state.value.copy(
            destination    = PlanRepository.regionDisplayName(region),
            flag           = region.emoji,
            selectedRegion = region,
            availablePlans = plans,
            suggestedPlan  = suggested,
            selectedPlan   = null
        )
    }

    fun setDuration(days: Int) {
        val s         = _state.value
        val suggested = PlanRepository.suggestPlan(s.destination, days, s.usageStyle)
        _state.value  = s.copy(
            durationDays  = days,
            suggestedPlan = suggested,
            selectedPlan  = null
        )
    }

    fun setUsageStyle(style: UsageStyle) {
        val s         = _state.value
        val suggested = PlanRepository.suggestPlan(s.destination, s.durationDays, style)
        _state.value  = s.copy(
            usageStyle    = style,
            suggestedPlan = suggested,
            selectedPlan  = null
        )
    }

    fun selectPlan(plan: SailyPlan) {
        _state.value = _state.value.copy(selectedPlan = plan)
    }

    fun setLinkCode(code: String) {
        val trimmed = code.trim()
        settings.connectaLinkCode = trimmed.ifEmpty { null }
        _linkCode.value = trimmed.ifEmpty { null }
    }

    // ── Trip lifecycle ────────────────────────────────────────────────────────

    fun startTrip() {
        val s    = _state.value
        val plan = s.selectedPlan ?: s.suggestedPlan ?: return
        val trip = TripConfig(
            destination  = s.destination,
            countryCode  = plan.countryCode,
            flag         = s.flag,
            durationDays = s.durationDays,
            usageStyle   = s.usageStyle,
            selectedPlan = plan
        )
        _state.value = _state.value.copy(tripStarted = true, activeTrip = trip)
        generateInitialAlerts(trip)
        confirmTripWithConnecta(trip)
    }

    private fun confirmTripWithConnecta(trip: TripConfig) {
        val sessionId = settings.connectaLinkCode
        if (sessionId.isNullOrBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val fmt   = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = System.currentTimeMillis()
            val endMs = today + trip.durationDays * 86_400_000L

            val tripId = ConnectaApiClient.confirmNewTrip(
                sessionId   = sessionId,
                destination = trip.destination,
                startDate   = fmt.format(Date(today)),
                endDate     = fmt.format(Date(endMs)),
                plan = ConnectaApiClient.ConfirmedPlan(
                    provider     = "Connecta Local",
                    name         = trip.selectedPlan.id,
                    priceUsd     = trip.selectedPlan.priceUSD,
                    dataLabel    = if (trip.selectedPlan.isUnlimited) "Unlimited"
                                   else "${trip.selectedPlan.dataGB} GB",
                    validityDays = trip.selectedPlan.validDays
                )
            )
            if (tripId != null) {
                _state.value = _state.value.copy(
                    activeTrip = _state.value.activeTrip?.copy(connectaTripId = tripId)
                )
            }
        }
    }

    fun resetTrip() {
        _state.value  = TripSetupState()
        _alerts.value = emptyList()
    }

    // ── Alert helpers ─────────────────────────────────────────────────────────

    fun addAlert(alert: Alert) {
        _alerts.value = listOf(alert) + _alerts.value
    }

    private fun generateInitialAlerts(trip: TripConfig) {
        val needed    = trip.usageStyle.dailyGb * trip.durationDays
        val planGb    = trip.selectedPlan.dataGB
        val newAlerts = mutableListOf<Alert>()

        if (planGb < needed) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Plan May Be Insufficient",
                message  = "Your ${planGb}GB plan covers less than the " +
                           "${"%.1f".format(needed)}GB estimated for ${trip.durationDays} days " +
                           "of ${trip.usageStyle.label.lowercase()} usage.",
                severity = AlertSeverity.WARNING
            )
        } else {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Trip Started",
                message  = "${trip.flag} ${trip.destination} · ${planGb}GB Connecta plan active for ${trip.durationDays} days.",
                severity = AlertSeverity.INFO
            )
        }
        _alerts.value = newAlerts
    }
}
