package com.sailguard.app.data.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to Connecta's Go GraphQL backend. Both SailGuard and the Connecta web
 * frontend write to the same `trips` table via this API, keyed by a shared
 * sessionId — this is what makes "start in SailGuard, see on web" work.
 *
 * Emulator gotcha: 10.0.2.2 is the emulator's alias for the host's localhost.
 * On a real device on the same Wi-Fi, use your machine's LAN IP (e.g. 192.168.x.x:8080).
 * Swap for the real deployed URL once the backend is hosted.
 */
object ConnectaApiClient {

    private const val BASE_URL = "https://connecta-eagq.onrender.com/graphql"
    private val JSON = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    data class ConfirmedPlan(
        val provider: String,
        val name: String,
        val priceUsd: Double,
        val dataLabel: String,
        val validityDays: Int
    )

    data class SyncedTrip(
        val id: String,
        val destination: String,
        val startDate: String,
        val endDate: String,
        val confirmedAt: String?,
        val planName: String?,
        val planProvider: String?,
        val priceUsd: Double?,
        val dataLabel: String?,
        val validityDays: Int?
    )

    /** Creates AND confirms a new trip in one step. Returns the new Connecta tripId on success, null on any failure. */
    fun confirmNewTrip(
        sessionId: String,
        destination: String,
        startDate: String,
        endDate: String,
        plan: ConfirmedPlan
    ): String? {
        val query = """
            mutation ConfirmTrip(${'$'}input: ConfirmTripInput!) {
              confirmTrip(input: ${'$'}input) { id }
            }
        """.trimIndent()

        val planJson = JSONObject().apply {
            put("provider", plan.provider)
            put("name", plan.name)
            put("priceUsd", plan.priceUsd)
            put("dataLabel", plan.dataLabel)
            put("validityDays", plan.validityDays)
        }
        val inputJson = JSONObject().apply {
            put("sessionId", sessionId)
            put("destination", destination)
            put("startDate", startDate)
            put("endDate", endDate)
            put("plan", planJson)
        }
        val body = JSONObject().apply {
            put("query", query)
            put("variables", JSONObject().put("input", inputJson))
        }

        val response = post(body) ?: return null
        return response.optJSONObject("data")
            ?.optJSONObject("confirmTrip")
            ?.optString("id")
            ?.takeIf { it.isNotBlank() }
    }

    /** Fire-and-forget. Failures are logged, never surfaced to the user. */
    fun submitUsageSnapshot(
        connectaTripId: String,
        dataUsedMb: Double,
        batteryPct: Int?,
        networkType: String?
    ) {
        val query = """
            mutation SubmitUsageSnapshot(${'$'}input: SubmitUsageSnapshotInput!) {
              submitUsageSnapshot(input: ${'$'}input) { id }
            }
        """.trimIndent()

        val inputJson = JSONObject().apply {
            put("tripId", connectaTripId)
            put("dataUsedMb", dataUsedMb)
            if (batteryPct != null) put("batteryPct", batteryPct)
            if (networkType != null) put("networkType", networkType)
        }
        val body = JSONObject().apply {
            put("query", query)
            put("variables", JSONObject().put("input", inputJson))
        }

        post(body)
    }

    /** Every trip tied to this session on Connecta's backend — including ones confirmed
     *  purely from the web checkout, which SailGuard never created locally.
     *  Returns empty list on any failure, never throws. */
    fun fetchTripsBySession(sessionId: String): List<SyncedTrip> {
        val query = """
            query TripsBySession(${'$'}sessionId: String!) {
              tripsBySession(sessionId: ${'$'}sessionId) {
                id
                destination
                startDate
                endDate
                confirmedAt
                confirmedPlan { provider name priceUsd dataLabel validityDays }
              }
            }
        """.trimIndent()

        val body = JSONObject().apply {
            put("query", query)
            put("variables", JSONObject().put("sessionId", sessionId))
        }

        val response = post(body) ?: return emptyList()
        val tripsArray = response.optJSONObject("data")
            ?.optJSONArray("tripsBySession") ?: return emptyList()

        val result = mutableListOf<SyncedTrip>()
        for (i in 0 until tripsArray.length()) {
            val t = tripsArray.optJSONObject(i) ?: continue
            val plan = t.optJSONObject("confirmedPlan")
            result.add(
                SyncedTrip(
                    id           = t.optString("id"),
                    destination  = t.optString("destination"),
                    startDate    = t.optString("startDate"),
                    endDate      = t.optString("endDate"),
                    confirmedAt  = t.optString("confirmedAt").takeIf { it.isNotBlank() && it != "null" },
                    planName     = plan?.optString("name"),
                    planProvider = plan?.optString("provider"),
                    priceUsd     = plan?.optDouble("priceUsd"),
                    dataLabel    = plan?.optString("dataLabel"),
                    validityDays = plan?.optInt("validityDays")
                )
            )
        }
        return result
    }

    private fun post(body: JSONObject): JSONObject? {
        return try {
            val request = Request.Builder()
                .url(BASE_URL)
                .post(body.toString().toRequestBody(JSON))
                .build()

            client.newCall(request).execute().use { resp ->
                val text = resp.body?.string() ?: return null
                val json = JSONObject(text)
                if (json.has("errors")) {
                    Log.w("ConnectaApiClient", "GraphQL errors: ${json.getJSONArray("errors")}")
                    return null
                }
                json
            }
        } catch (e: IOException) {
            Log.w("ConnectaApiClient", "Connecta API unreachable: ${e.message}")
            null
        } catch (e: Exception) {
            Log.w("ConnectaApiClient", "Connecta API call failed: ${e.message}")
            null
        }
    }
}
