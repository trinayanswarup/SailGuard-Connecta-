package com.sailguard.app.data.repository

import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.UsageStyle

enum class Region(val displayName: String, val emoji: String, val description: String) {
    EUROPE  ("Europe",   "🇪🇺", "EU, UK & more"),
    ASIA    ("Asia",     "🌏", "East & Southeast Asia"),
    AMERICAS("Americas", "🌎", "North & South America"),
    GLOBAL  ("Global",   "🌐", "200+ destinations")
}

object PlanRepository {

    data class CountryInfo(val name: String, val code: String, val flag: String)

    // ── Connecta flat global plan catalog ─────────────────────────────────────
    // Pricing is identical regardless of destination — no per-country tiers.

    private val connectaPlans: List<SailyPlan> = listOf(
        SailyPlan(id = "plan-1gb",          country = "global", countryCode = "GL", dataGB = 1.0,             validDays = 7,  priceUSD = 3.99,  network = "Connecta Local"),
        SailyPlan(id = "plan-3gb",          country = "global", countryCode = "GL", dataGB = 3.0,             validDays = 30, priceUSD = 6.99,  network = "Connecta Local"),
        SailyPlan(id = "plan-5gb",          country = "global", countryCode = "GL", dataGB = 5.0,             validDays = 30, priceUSD = 9.99,  network = "Connecta Local"),
        SailyPlan(id = "plan-10gb",         country = "global", countryCode = "GL", dataGB = 10.0,            validDays = 30, priceUSD = 15.99, network = "Connecta Local"),
        SailyPlan(id = "plan-20gb",         country = "global", countryCode = "GL", dataGB = 20.0,            validDays = 30, priceUSD = 22.99, network = "Connecta Local"),
        SailyPlan(id = "plan-unlimited-10", country = "global", countryCode = "GL", dataGB = Double.MAX_VALUE, validDays = 10, priceUSD = 34.99, network = "Connecta Local", isUnlimited = true),
        SailyPlan(id = "plan-unlimited-15", country = "global", countryCode = "GL", dataGB = Double.MAX_VALUE, validDays = 15, priceUSD = 48.99, network = "Connecta Local", isUnlimited = true),
        SailyPlan(id = "plan-unlimited-20", country = "global", countryCode = "GL", dataGB = Double.MAX_VALUE, validDays = 20, priceUSD = 59.99, network = "Connecta Local", isUnlimited = true),
        SailyPlan(id = "plan-unlimited-25", country = "global", countryCode = "GL", dataGB = Double.MAX_VALUE, validDays = 25, priceUSD = 65.99, network = "Connecta Local", isUnlimited = true),
        SailyPlan(id = "plan-unlimited-30", country = "global", countryCode = "GL", dataGB = Double.MAX_VALUE, validDays = 30, priceUSD = 71.99, network = "Connecta Local", isUnlimited = true)
    )

    // ── Country master list ───────────────────────────────────────────────────

    private data class CountryEntry(val info: CountryInfo, val region: Region)

    private val allEntries: List<CountryEntry> = listOf(
        // ── Europe – Baltic
        CountryEntry(CountryInfo("Estonia",   "EE", "🇪🇪"), Region.EUROPE),
        CountryEntry(CountryInfo("Latvia",    "LV", "🇱🇻"), Region.EUROPE),
        CountryEntry(CountryInfo("Lithuania", "LT", "🇱🇹"), Region.EUROPE),
        // ── Europe – Eastern EU
        CountryEntry(CountryInfo("Albania",              "AL", "🇦🇱"), Region.EUROPE),
        CountryEntry(CountryInfo("Bosnia & Herzegovina", "BA", "🇧🇦"), Region.EUROPE),
        CountryEntry(CountryInfo("Bulgaria",             "BG", "🇧🇬"), Region.EUROPE),
        CountryEntry(CountryInfo("Croatia",              "HR", "🇭🇷"), Region.EUROPE),
        CountryEntry(CountryInfo("Czech Republic",       "CZ", "🇨🇿"), Region.EUROPE),
        CountryEntry(CountryInfo("Hungary",              "HU", "🇭🇺"), Region.EUROPE),
        CountryEntry(CountryInfo("Kosovo",               "XK", "🇽🇰"), Region.EUROPE),
        CountryEntry(CountryInfo("Moldova",              "MD", "🇲🇩"), Region.EUROPE),
        CountryEntry(CountryInfo("Montenegro",           "ME", "🇲🇪"), Region.EUROPE),
        CountryEntry(CountryInfo("North Macedonia",      "MK", "🇲🇰"), Region.EUROPE),
        CountryEntry(CountryInfo("Poland",               "PL", "🇵🇱"), Region.EUROPE),
        CountryEntry(CountryInfo("Romania",              "RO", "🇷🇴"), Region.EUROPE),
        CountryEntry(CountryInfo("Serbia",               "RS", "🇷🇸"), Region.EUROPE),
        CountryEntry(CountryInfo("Slovakia",             "SK", "🇸🇰"), Region.EUROPE),
        CountryEntry(CountryInfo("Slovenia",             "SI", "🇸🇮"), Region.EUROPE),
        CountryEntry(CountryInfo("Ukraine",              "UA", "🇺🇦"), Region.EUROPE),
        // ── Europe – Western / Nordic
        CountryEntry(CountryInfo("Austria",       "AT", "🇦🇹"), Region.EUROPE),
        CountryEntry(CountryInfo("Belgium",       "BE", "🇧🇪"), Region.EUROPE),
        CountryEntry(CountryInfo("Cyprus",        "CY", "🇨🇾"), Region.EUROPE),
        CountryEntry(CountryInfo("Denmark",       "DK", "🇩🇰"), Region.EUROPE),
        CountryEntry(CountryInfo("Finland",       "FI", "🇫🇮"), Region.EUROPE),
        CountryEntry(CountryInfo("France",        "FR", "🇫🇷"), Region.EUROPE),
        CountryEntry(CountryInfo("Germany",       "DE", "🇩🇪"), Region.EUROPE),
        CountryEntry(CountryInfo("Greece",        "GR", "🇬🇷"), Region.EUROPE),
        CountryEntry(CountryInfo("Iceland",       "IS", "🇮🇸"), Region.EUROPE),
        CountryEntry(CountryInfo("Ireland",       "IE", "🇮🇪"), Region.EUROPE),
        CountryEntry(CountryInfo("Italy",         "IT", "🇮🇹"), Region.EUROPE),
        CountryEntry(CountryInfo("Liechtenstein", "LI", "🇱🇮"), Region.EUROPE),
        CountryEntry(CountryInfo("Luxembourg",    "LU", "🇱🇺"), Region.EUROPE),
        CountryEntry(CountryInfo("Malta",         "MT", "🇲🇹"), Region.EUROPE),
        CountryEntry(CountryInfo("Netherlands",   "NL", "🇳🇱"), Region.EUROPE),
        CountryEntry(CountryInfo("Norway",        "NO", "🇳🇴"), Region.EUROPE),
        CountryEntry(CountryInfo("Portugal",      "PT", "🇵🇹"), Region.EUROPE),
        CountryEntry(CountryInfo("Spain",         "ES", "🇪🇸"), Region.EUROPE),
        CountryEntry(CountryInfo("Sweden",        "SE", "🇸🇪"), Region.EUROPE),
        CountryEntry(CountryInfo("Switzerland",   "CH", "🇨🇭"), Region.EUROPE),
        // ── Europe – UK
        CountryEntry(CountryInfo("United Kingdom", "GB", "🇬🇧"), Region.EUROPE),
        // ── Asia – Cheap
        CountryEntry(CountryInfo("Bangladesh", "BD", "🇧🇩"), Region.ASIA),
        CountryEntry(CountryInfo("Cambodia",   "KH", "🇰🇭"), Region.ASIA),
        CountryEntry(CountryInfo("India",      "IN", "🇮🇳"), Region.ASIA),
        CountryEntry(CountryInfo("Indonesia",  "ID", "🇮🇩"), Region.ASIA),
        CountryEntry(CountryInfo("Laos",       "LA", "🇱🇦"), Region.ASIA),
        CountryEntry(CountryInfo("Myanmar",    "MM", "🇲🇲"), Region.ASIA),
        CountryEntry(CountryInfo("Nepal",      "NP", "🇳🇵"), Region.ASIA),
        CountryEntry(CountryInfo("Pakistan",   "PK", "🇵🇰"), Region.ASIA),
        CountryEntry(CountryInfo("Philippines","PH", "🇵🇭"), Region.ASIA),
        CountryEntry(CountryInfo("Sri Lanka",  "LK", "🇱🇰"), Region.ASIA),
        CountryEntry(CountryInfo("Thailand",   "TH", "🇹🇭"), Region.ASIA),
        CountryEntry(CountryInfo("Vietnam",    "VN", "🇻🇳"), Region.ASIA),
        // ── Asia – Mid
        CountryEntry(CountryInfo("Brunei",   "BN", "🇧🇳"), Region.ASIA),
        CountryEntry(CountryInfo("China",    "CN", "🇨🇳"), Region.ASIA),
        CountryEntry(CountryInfo("Malaysia", "MY", "🇲🇾"), Region.ASIA),
        CountryEntry(CountryInfo("Maldives", "MV", "🇲🇻"), Region.ASIA),
        CountryEntry(CountryInfo("Mongolia", "MN", "🇲🇳"), Region.ASIA),
        // ── Asia – Premium
        CountryEntry(CountryInfo("Hong Kong",   "HK", "🇭🇰"), Region.ASIA),
        CountryEntry(CountryInfo("Japan",       "JP", "🇯🇵"), Region.ASIA),
        CountryEntry(CountryInfo("Macao",       "MO", "🇲🇴"), Region.ASIA),
        CountryEntry(CountryInfo("Singapore",   "SG", "🇸🇬"), Region.ASIA),
        CountryEntry(CountryInfo("South Korea", "KR", "🇰🇷"), Region.ASIA),
        CountryEntry(CountryInfo("Taiwan",      "TW", "🇹🇼"), Region.ASIA),
        // ── Americas – North
        CountryEntry(CountryInfo("Canada",        "CA", "🇨🇦"), Region.AMERICAS),
        CountryEntry(CountryInfo("United States", "US", "🇺🇸"), Region.AMERICAS),
        // ── Americas – Latin
        CountryEntry(CountryInfo("Argentina",          "AR", "🇦🇷"), Region.AMERICAS),
        CountryEntry(CountryInfo("Bahamas",            "BS", "🇧🇸"), Region.AMERICAS),
        CountryEntry(CountryInfo("Barbados",           "BB", "🇧🇧"), Region.AMERICAS),
        CountryEntry(CountryInfo("Bolivia",            "BO", "🇧🇴"), Region.AMERICAS),
        CountryEntry(CountryInfo("Brazil",             "BR", "🇧🇷"), Region.AMERICAS),
        CountryEntry(CountryInfo("Chile",              "CL", "🇨🇱"), Region.AMERICAS),
        CountryEntry(CountryInfo("Colombia",           "CO", "🇨🇴"), Region.AMERICAS),
        CountryEntry(CountryInfo("Costa Rica",         "CR", "🇨🇷"), Region.AMERICAS),
        CountryEntry(CountryInfo("Cuba",               "CU", "🇨🇺"), Region.AMERICAS),
        CountryEntry(CountryInfo("Dominican Republic", "DO", "🇩🇴"), Region.AMERICAS),
        CountryEntry(CountryInfo("Ecuador",            "EC", "🇪🇨"), Region.AMERICAS),
        CountryEntry(CountryInfo("El Salvador",        "SV", "🇸🇻"), Region.AMERICAS),
        CountryEntry(CountryInfo("Guatemala",          "GT", "🇬🇹"), Region.AMERICAS),
        CountryEntry(CountryInfo("Honduras",           "HN", "🇭🇳"), Region.AMERICAS),
        CountryEntry(CountryInfo("Jamaica",            "JM", "🇯🇲"), Region.AMERICAS),
        CountryEntry(CountryInfo("Mexico",             "MX", "🇲🇽"), Region.AMERICAS),
        CountryEntry(CountryInfo("Nicaragua",          "NI", "🇳🇮"), Region.AMERICAS),
        CountryEntry(CountryInfo("Panama",             "PA", "🇵🇦"), Region.AMERICAS),
        CountryEntry(CountryInfo("Paraguay",           "PY", "🇵🇾"), Region.AMERICAS),
        CountryEntry(CountryInfo("Peru",               "PE", "🇵🇪"), Region.AMERICAS),
        CountryEntry(CountryInfo("Trinidad & Tobago",  "TT", "🇹🇹"), Region.AMERICAS),
        CountryEntry(CountryInfo("Uruguay",            "UY", "🇺🇾"), Region.AMERICAS),
        CountryEntry(CountryInfo("Venezuela",          "VE", "🇻🇪"), Region.AMERICAS),
        // ── Global – Middle East
        CountryEntry(CountryInfo("Bahrain",      "BH", "🇧🇭"), Region.GLOBAL),
        CountryEntry(CountryInfo("Israel",       "IL", "🇮🇱"), Region.GLOBAL),
        CountryEntry(CountryInfo("Jordan",       "JO", "🇯🇴"), Region.GLOBAL),
        CountryEntry(CountryInfo("Kuwait",       "KW", "🇰🇼"), Region.GLOBAL),
        CountryEntry(CountryInfo("Lebanon",      "LB", "🇱🇧"), Region.GLOBAL),
        CountryEntry(CountryInfo("Oman",         "OM", "🇴🇲"), Region.GLOBAL),
        CountryEntry(CountryInfo("Qatar",        "QA", "🇶🇦"), Region.GLOBAL),
        CountryEntry(CountryInfo("Saudi Arabia", "SA", "🇸🇦"), Region.GLOBAL),
        CountryEntry(CountryInfo("Turkey",       "TR", "🇹🇷"), Region.GLOBAL),
        CountryEntry(CountryInfo("UAE",          "AE", "🇦🇪"), Region.GLOBAL),
        // ── Global – Africa
        CountryEntry(CountryInfo("Egypt",        "EG", "🇪🇬"), Region.GLOBAL),
        CountryEntry(CountryInfo("Ethiopia",     "ET", "🇪🇹"), Region.GLOBAL),
        CountryEntry(CountryInfo("Ghana",        "GH", "🇬🇭"), Region.GLOBAL),
        CountryEntry(CountryInfo("Kenya",        "KE", "🇰🇪"), Region.GLOBAL),
        CountryEntry(CountryInfo("Morocco",      "MA", "🇲🇦"), Region.GLOBAL),
        CountryEntry(CountryInfo("Nigeria",      "NG", "🇳🇬"), Region.GLOBAL),
        CountryEntry(CountryInfo("South Africa", "ZA", "🇿🇦"), Region.GLOBAL),
        CountryEntry(CountryInfo("Tanzania",     "TZ", "🇹🇿"), Region.GLOBAL),
        CountryEntry(CountryInfo("Tunisia",      "TN", "🇹🇳"), Region.GLOBAL),
        CountryEntry(CountryInfo("Uganda",       "UG", "🇺🇬"), Region.GLOBAL),
        // ── Global – Oceania
        CountryEntry(CountryInfo("Australia",       "AU", "🇦🇺"), Region.GLOBAL),
        CountryEntry(CountryInfo("Fiji",            "FJ", "🇫🇯"), Region.GLOBAL),
        CountryEntry(CountryInfo("New Zealand",     "NZ", "🇳🇿"), Region.GLOBAL),
        CountryEntry(CountryInfo("Papua New Guinea","PG", "🇵🇬"), Region.GLOBAL),
        // ── Global – Central Asia & Caucasus
        CountryEntry(CountryInfo("Armenia",    "AM", "🇦🇲"), Region.GLOBAL),
        CountryEntry(CountryInfo("Azerbaijan", "AZ", "🇦🇿"), Region.GLOBAL),
        CountryEntry(CountryInfo("Georgia",    "GE", "🇬🇪"), Region.GLOBAL),
        CountryEntry(CountryInfo("Kazakhstan", "KZ", "🇰🇿"), Region.GLOBAL),
        CountryEntry(CountryInfo("Kyrgyzstan", "KG", "🇰🇬"), Region.GLOBAL),
        CountryEntry(CountryInfo("Uzbekistan", "UZ", "🇺🇿"), Region.GLOBAL)
    )

    // ── Derived collections ───────────────────────────────────────────────────

    val countries: List<CountryInfo> = allEntries.map { it.info }.sortedBy { it.name }

    private val regionByCountry: Map<String, Region> =
        allEntries.associate { e -> e.info.name to e.region }

    fun regionForCountry(name: String): Region = regionByCountry[name] ?: Region.GLOBAL

    fun regionDisplayName(region: Region): String = region.displayName

    fun countriesInRegion(region: Region): List<CountryInfo> =
        allEntries.filter { it.region == region }.map { it.info }.sortedBy { it.name }

    // ── Plan access ───────────────────────────────────────────────────────────

    fun getPlansForCountry(country: String): List<SailyPlan> = connectaPlans

    fun getRegionalPlans(region: Region): List<SailyPlan> = connectaPlans

    fun suggestPlan(country: String, durationDays: Int, usageStyle: UsageStyle): SailyPlan? {
        val neededGb = usageStyle.dailyGb * durationDays * 1.2
        val fixedMatch = connectaPlans
            .filter { !it.isUnlimited && it.dataGB >= neededGb && it.validDays >= durationDays }
            .minByOrNull { it.priceUSD }
        if (fixedMatch != null) return fixedMatch
        return connectaPlans
            .filter { it.isUnlimited && it.validDays >= durationDays }
            .minByOrNull { it.priceUSD }
            ?: connectaPlans.filter { it.isUnlimited }.minByOrNull { it.priceUSD }
    }

    fun flagForCountry(country: String): String =
        allEntries.find { it.info.name == country }?.info?.flag ?: "🌍"

    // Retained for call sites in TripSetupScreen that still use the old unlimited price scaling UI.
    fun unlimitedPriceForDays(base15DayPrice: Double, days: Int): Double {
        val multiplier = when (days) {
            7  -> 0.67
            15 -> 1.00
            30 -> 1.80
            90 -> 4.50
            else -> 1.00
        }
        return kotlin.math.round(base15DayPrice * multiplier * 100) / 100.0
    }
}
