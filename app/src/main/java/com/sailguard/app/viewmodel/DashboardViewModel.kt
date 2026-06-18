package com.sailguard.app.viewmodel

import android.app.Application
import android.net.TrafficStats
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sailguard.app.data.database.AppDatabase
import com.sailguard.app.data.model.Alert
import com.sailguard.app.data.model.AlertSeverity
import com.sailguard.app.data.model.DeviceStatus
import com.sailguard.app.data.model.TripConfig
import com.sailguard.app.data.model.TripHistoryEntity
import com.sailguard.app.data.repository.DeviceRepository
import com.sailguard.app.data.repository.TripHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

enum class RiskLevel(val label: String) {
    SAFE("Safe"), WARNING("Warning"), CRITICAL("Critical")
}

data class DashboardState(
    val trip:           TripConfig?  = null,
    val deviceStatus:   DeviceStatus = DeviceStatus(),
    val usedGb:         Double       = 0.0,
    val tripStartTimeMs: Long        = 0L,
    val riskLevel:      RiskLevel    = RiskLevel.SAFE,
    val dashAlerts:     List<Alert>  = emptyList()
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val deviceRepo    = DeviceRepository(app)
    private val historyRepo   by lazy {
        TripHistoryRepository(AppDatabase.getDatabase(app).tripHistoryDao())
    }

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var baselineBytes: Long = 0L
    private var pollingJob: Job?    = null

    init {
        viewModelScope.launch {
            deviceRepo.deviceStatusFlow().collect { ds ->
                _state.value = _state.value.copy(deviceStatus = ds)
            }
        }
    }

    // ── Trip binding ──────────────────────────────────────────────────────────

    fun bindTrip(trip: TripConfig) {
        // Skip full reset if the same trip is already active, but propagate
        // connectaTripId if it just arrived from the Connecta backend.
        if (_state.value.trip?.startTimestamp == trip.startTimestamp &&
            _state.value.tripStartTimeMs != 0L) {
            if (_state.value.trip?.connectaTripId != trip.connectaTripId) {
                _state.value = _state.value.copy(trip = trip)
            }
            return
        }

        pollingJob?.cancel()
        baselineBytes = readMobileBytes()
        val startTime = System.currentTimeMillis()
        _state.value = DashboardState(
            trip            = trip,
            deviceStatus    = _state.value.deviceStatus,
            usedGb          = 0.0,
            tripStartTimeMs = startTime,
            riskLevel       = RiskLevel.SAFE,
            dashAlerts      = emptyList()
        )
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                refreshUsage()
            }
        }
    }

    /**
     * Saves the trip to Room history then clears the dashboard.
     * Called when the user taps "End Trip" and confirms.
     */
    fun saveAndEndTrip() {
        val s    = _state.value
        val trip = s.trip ?: run { clearTrip(); return }
        viewModelScope.launch {
            historyRepo.saveTrip(
                TripHistoryEntity(
                    destination   = trip.destination,
                    countryFlag   = trip.flag,
                    tripDays      = trip.durationDays,
                    planSizeGb    = trip.selectedPlan.dataGB
                        .takeUnless { it == Double.MAX_VALUE } ?: -1.0,
                    actualGbUsed  = s.usedGb,
                    planWasEnough = s.usedGb <= (trip.selectedPlan.dataGB
                        .takeUnless { it == Double.MAX_VALUE } ?: Double.MAX_VALUE),
                    date          = System.currentTimeMillis(),
                    totalCost     = trip.selectedPlan.priceUSD
                )
            )
        }
        clearTrip()
    }

    fun clearTrip() {
        pollingJob?.cancel()
        pollingJob    = null
        baselineBytes = 0L
        _state.value  = DashboardState(deviceStatus = _state.value.deviceStatus)
    }

    // ── Derived helpers ───────────────────────────────────────────────────────

    fun remainingGb(): Double =
        max(0.0, (_state.value.trip?.selectedPlan?.dataGB
            ?.takeUnless { it == Double.MAX_VALUE } ?: 0.0) - _state.value.usedGb)

    fun daysLeft(): Double =
        max(0.0, (_state.value.trip?.durationDays?.toDouble() ?: 0.0) - elapsedDays())

    fun burnRateGbPerDay(): Double? {
        val elapsed = elapsedDays()
        val used    = _state.value.usedGb
        return if (elapsed > 0.0 && used > 0.0) used / elapsed else null
    }

    fun daysUntilEmpty(): Double? {
        val burn = burnRateGbPerDay() ?: return null
        return if (burn > 0.0) remainingGb() / burn else null
    }

    fun usagePercent(): Float {
        val plan  = _state.value.trip?.selectedPlan ?: return 0f
        if (plan.isUnlimited) return 0f
        return (_state.value.usedGb / plan.dataGB).toFloat().coerceIn(0f, 1f)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun elapsedDays(): Double {
        val startMs = _state.value.tripStartTimeMs
        if (startMs == 0L) return 0.0
        return (System.currentTimeMillis() - startMs) / 86_400_000.0
    }

    private fun readMobileBytes(): Long {
        val rx = TrafficStats.getMobileRxBytes()
        val tx = TrafficStats.getMobileTxBytes()
        return if (rx == -1L || tx == -1L) -1L else rx + tx
    }

    private fun refreshUsage() {
        if (baselineBytes == -1L) return
        val current   = readMobileBytes()
        if (current   == -1L) return
        val usedBytes = max(0L, current - baselineBytes)
        val usedGb    = usedBytes / 1_073_741_824.0
        _state.value  = _state.value.copy(usedGb = usedGb)
        recomputeRisk()
        checkAndAddAlerts()
        pushUsageSnapshot(usedGb)
    }

    private fun pushUsageSnapshot(usedGb: Double) {
        val tripId  = _state.value.trip?.connectaTripId ?: return
        val battery = _state.value.deviceStatus.batteryLevel
        val network = _state.value.deviceStatus.networkType
        viewModelScope.launch(Dispatchers.IO) {
            com.sailguard.app.data.network.ConnectaApiClient.submitUsageSnapshot(
                connectaTripId = tripId,
                dataUsedMb     = usedGb * 1024.0,
                batteryPct     = battery,
                networkType    = network
            )
        }
    }

    private fun recomputeRisk() {
        val plan = _state.value.trip?.selectedPlan ?: return
        if (plan.isUnlimited) return
        val total = plan.dataGB
        if (total <= 0.0) return
        val remainingFraction = remainingGb() / total
        val risk = when {
            remainingFraction <= 0.25 -> RiskLevel.CRITICAL
            remainingFraction <= 0.50 -> RiskLevel.WARNING
            else                      -> RiskLevel.SAFE
        }
        _state.value = _state.value.copy(riskLevel = risk)
    }

    private fun checkAndAddAlerts() {
        val s     = _state.value
        val trip  = s.trip ?: return
        if (s.usedGb <= 0.0 || trip.selectedPlan.isUnlimited) return
        val pct       = usagePercent()
        val daysUntil = daysUntilEmpty()
        val existing  = s.dashAlerts.map { it.title }
        val newAlerts = s.dashAlerts.toMutableList()

        if (pct >= 0.8f && "80% Data Used" !in existing) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "80% Data Used",
                message  = "You have used 80% of your ${trip.selectedPlan.dataGB.toLong()}GB plan.",
                severity = AlertSeverity.WARNING
            )
        }
        if (pct >= 0.5f && pct < 0.8f && "50% Data Used" !in existing) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "50% Data Used",
                message  = "Halfway through your data. Burn rate: ${"%.2f".format(burnRateGbPerDay())} GB/day.",
                severity = AlertSeverity.INFO
            )
        }
        if (daysUntil != null && daysUntil < daysLeft() && daysUntil < 3.0
            && "Plan Running Out Soon" !in existing) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Plan Running Out Soon",
                message  = "At this pace your plan may run out in " +
                           "${"%.1f".format(daysUntil)} days — ${daysLeft().toInt()} days remain.",
                severity = AlertSeverity.CRITICAL
            )
        }
        _state.value = s.copy(dashAlerts = newAlerts)
    }
}
