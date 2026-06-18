package com.sailguard.app.data.model

// ── Plan & Trip ───────────────────────────────────────────────────────────────

data class SailyPlan(
    val id: String,
    val country: String,
    val countryCode: String,
    val dataGB: Double,       // Double.MAX_VALUE for unlimited
    val validDays: Int,       // base validity; unlimited plans default to 15
    val priceUSD: Double,     // for unlimited = 15-day base price
    val network: String = "4G/LTE",
    val isUnlimited: Boolean = false,
    val unlimitedPrices: Map<Int, Double> = emptyMap()
)

enum class UsageStyle(
    val label: String,
    val dailyGb: Double,
    val description: String
) {
    LIGHT("Light",  0.15, "Maps, messaging, light browsing"),
    MEDIUM("Medium", 0.50, "Social media, casual use"),
    HEAVY("Heavy",  1.50, "Video streaming, calls & hotspot")
}

data class TripConfig(
    val destination: String,
    val countryCode: String,
    val flag: String,
    val durationDays: Int,
    val usageStyle: UsageStyle,
    val selectedPlan: SailyPlan,
    val startTimestamp: Long = System.currentTimeMillis(),
    val connectaTripId: String? = null
)

// ── Alerts ────────────────────────────────────────────────────────────────────

enum class AlertSeverity { INFO, WARNING, CRITICAL }

data class Alert(
    val id: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long = System.currentTimeMillis()
)

// ── Device / Smart Mode ───────────────────────────────────────────────────────

enum class NetworkStrength { STRONG, MODERATE, WEAK, NONE }

data class DeviceStatus(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val networkStrength: NetworkStrength = NetworkStrength.STRONG,
    val networkType: String = "WiFi"
)

enum class SmartRecommendation(val label: String, val detail: String) {
    NORMAL(     "Normal Usage",       "Device conditions are optimal for data use"),
    REDUCE(     "Reduce Usage",       "Consider limiting non-essential data activity"),
    LIGHT_MODE( "Switch to Light",    "Battery or signal requires lighter usage profile"),
    CRITICAL(   "Save Data Now",      "Critical conditions — minimise data use immediately")
}
