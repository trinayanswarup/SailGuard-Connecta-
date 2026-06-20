# Connecta — Android

Native Android companion to [Connecta](https://github.com/trinayanswarup/Connecta). The app plans trips, recommends travel data plans, tracks real mobile data usage during active trips, and syncs confirmed plan selections and usage snapshots with the Connecta web app through a shared Go/GraphQL backend.

It works fully standalone for planning, recommendation, tracking, and local history. When linked to a Connecta web session, plan confirmations from either client appear on both, and Android usage snapshots feed a live usage chart on the web.

---

## Stack

|                  |                                                           |
| ---------------- | --------------------------------------------------------- |
| Language         | Kotlin 2.0.21                                             |
| UI               | Jetpack Compose + Material 3                              |
| Architecture     | MVVM · StateFlow · activity-scoped ViewModels             |
| Networking       | OkHttp + `org.json` — hand-rolled GraphQL client          |
| Local storage    | Room + KSP                                                |
| Real device APIs | `TrafficStats` · `BatteryManager` · `ConnectivityManager` |
| Min/Target SDK   | API 26 / API 36                                           |

---

## What it does

### Trip setup wizard

Destination picker (region → country), trip duration, usage style (Light/Medium/Heavy), and a five-slider detail screen (Video Streaming, Maps, Video Calls, Social Media, Hotspot) for a precise estimate.

### Plan recommendation engine

Blends slider-based estimates with this device's own trip history — weighted toward same-destination trips — then picks the plan that covers the estimate with a 1.2× safety buffer. Falls back from fixed plans to Unlimited when nothing in the fixed catalog is large enough.

The recommendation, the default selection, and the position in the list all point to the same plan.

### Checkout

Calls `confirmTrip` on Connecta's Go backend — a real GraphQL mutation. Since this app has its own recommendation logic and never calls `analyzeTrip`, it has no existing trip to attach to. The mutation handles this: when no `tripId` is passed, it creates and confirms a new trip row in one step.

### Active trip dashboard

Real-time tracking from `TrafficStats.getMobileRxBytes()` + `getMobileTxBytes()`, an animated ring gauge, real battery % via `BatteryManager`, real network type via `ConnectivityManager.NetworkCapabilities`. Pushes a `submitUsageSnapshot` every 30 seconds while a trip is active — this feeds the live usage chart on Connecta's web trip detail page.

### Smart Mode + Alerts

Battery/network monitoring with adaptive usage recommendations. Pace alerts, weak-signal detection, 50% and 80% usage milestones.

### History

Two sections, clearly labeled:

- **This device's trips** — local Room database, includes GB used vs. plan size and whether the plan was sufficient
- **Synced from Connecta** — plan confirmations from the web, fetched via `tripsBySession`

---

## Recommendation engine

```
1. Slider estimate      GB/day from this trip's usage sliders
2. Device history       GB/day average from past trips on this device
                        (same-destination trips weighted higher)
3. Blend                60% history + 40% slider when history exists
                        100% slider when it doesn't
4. Needed               blended GB/day × trip duration × 1.2 buffer
5. Select               cheapest fixed plan covering (needed)
                        → cheapest Unlimited covering the trip length if no fixed plan fits
                        → cheapest Unlimited regardless if nothing else matches
```

A deterministic heuristic rather than ML — the dataset per user is small and explainability matters more than marginal accuracy at this stage. The logic is transparent, debuggable, and can be improved as real trip data accumulates.

---

## Backend integration

The client uses simple synchronous OkHttp calls wrapped in `Dispatchers.IO`. Sync is fire-and-forget — failures are logged but never block the standalone app experience.

```kotlin
// On trip confirmation — called without tripId since this app doesn't run analyzeTrip
ConnectaApiClient.confirmNewTrip(sessionId, destination, startDate, endDate, plan)

// Every 30s on the dashboard — feeds the live chart on the web
ConnectaApiClient.submitUsageSnapshot(connectaTripId, dataUsedMb, batteryPct, networkType)

// On History screen load — surfaces web-confirmed plans here
ConnectaApiClient.fetchTripsBySession(sessionId)
```

For the MVP, Connecta uses explicit session linking instead of user accounts. The web client generates a UUID stored in `localStorage`; pasting that UUID into this app as a "link code" connects both clients to the same Postgres rows. This keeps the scope focused on the cross-client sync problem rather than account management.

---

## Limitations

Android's `TrafficStats` reports device-level mobile byte counters. The app estimates trip usage from counter deltas while a trip is active — it is not a carrier-grade billing meter and may differ from the operator's official usage numbers. It also tracks cellular traffic only, not Wi-Fi.

---

## Running it

```bash
git clone https://github.com/trinayanswarup/SailGuard.git
```

Open in Android Studio, let Gradle sync, run on a device or emulator.

To test backend sync: run `go run ./cmd/server` in the Connecta repo, then set `ConnectaApiClient.BASE_URL`:

- **Emulator** → `http://10.0.2.2:8080/graphql`
- **Real device (same Wi-Fi)** → `http://<your-LAN-IP>:8080/graphql`

`android:usesCleartextTraffic="true"` is set in the manifest for local development against a plain `http://` backend. Remove it once the backend is deployed over HTTPS.

Get the link code: `localStorage.getItem("connecta_session_id")` in Connecta's browser console.

---

## App name vs. package name

The app displays as "Connecta" on the home screen and throughout the UI. The package id (`com.sailguard.app`) and repo name are unchanged — renaming an Android package id touches every file in the namespace for zero user-visible benefit. The technical identity and the brand identity are intentionally different.

## Development notes

CLAUDE.md and AGENTS.md are excluded from this repository. They contain AI coding instructions, Android-specific build quirks (JAVA_HOME, cleartext traffic, emulator IP addressing), and session-specific workflow notes used during active development with Claude Code in Android Studio. They are not useful to anyone reading the codebase and add noise without signal.
