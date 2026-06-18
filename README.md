# Connecta (Android)

> Native Android companion app for [Connecta](https://github.com/trinayanswarup/Connecta) — recommends a data plan from real usage, tracks actual device data consumption during a trip, and syncs both directions with Connecta's web app through a shared Go/GraphQL backend.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-green?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)](https://developer.android.com/topic/architecture)

(Package name and repo are still `com.sailguard.app` / `SailGuard` under the hood — only the brand-facing app name and product identity changed. See [Why the name's inconsistent](#why-the-name-is-still-sailguard-under-the-hood) below.)

---

## What this actually is

This started as a standalone Android app and became the mobile half of a two-app product. It still works completely standalone — destination/usage/plan wizard, real device data tracking, local trip history, all of it functions with zero network dependency. But when you paste in a "link code" (the same session ID Connecta's web app generates), it becomes the same product running on two devices: confirm a purchase here, it shows up on the website; confirm it on the website, it shows up here; real cellular usage tracked on this phone renders as a live chart on the web.

The integration is additive, not assumed — every sync call is fire-and-forget and fails silently (logged, never user-facing) if there's no link code set or the backend's unreachable. Nothing about the standalone experience depends on the backend existing.

## Architecture

```
        ┌─────────────────────────────┐
        │   Connecta Go/GraphQL API     │
        │   (shared with the web app)   │
        └───────────────┬───────────────┘
            confirmTrip / submitUsageSnapshot
            tripsBySession
                          │
              ┌───────────┴───────────┐
              │                       │
   ┌──────────▼──────────┐  ┌─────────▼──────────┐
   │  This app (Android)  │  │  Connecta (web)      │
   │  Kotlin + Compose     │  │  Next.js + Groq      │
   └──────────────────────┘  └─────────────────────┘
```

A hand-rolled GraphQL client (`ConnectaApiClient.kt` — OkHttp + `org.json`, no codegen) makes two calls:

- **`confirmTrip`** when you start a trip in the wizard. This app never runs Connecta's `analyzeTrip` AI flow — it has its own plan-recommendation logic — so it has no existing trip to attach to. The mutation handles that: pass no `tripId`, and the backend creates and confirms a new trip in one step.
- **`submitUsageSnapshot`** every 30 seconds while a trip is active, with real data pulled from `TrafficStats.getMobileRxBytes()`/`getMobileTxBytes()` — actual cellular byte counters, not simulated.

And one read: **`tripsBySession`**, which is how a trip confirmed purely on the website shows up here too, under a "Synced from Connecta" section on the History screen — distinct from this app's own local Room-backed trip history below it.

## Features

### Smart trip setup (3-step wizard)
Destination picker (region/country, cosmetic flags — pricing is global, not per-country), trip duration, usage style (Light/Medium/Heavy), and a detailed slider screen (Video Streaming, Maps, Video Calls, Social Media, Hotspot) for a finer-grained estimate.

### Plan recommendation
Blends slider-based estimates with this device's actual trip history to recommend a plan — same global catalog Connecta's web app quotes (1/3/5/10/20 GB fixed tiers, 5 Unlimited validity options), not a separate pricing model. Shows its reasoning plainly: *"Your past trips averaged X GB/day, slider estimate Y GB/day, blended Z GB/day × duration × safety buffer = N GB needed."* The recommended plan is both the one checked by default and the one shown first in the list — these two used to disagree with each other and with a separate "best $/GB value" badge; that's fixed (see [Known rough edges](#known-rough-edges-worth-knowing-about) below for why it's worth knowing this was a real bug, not just a feature).

### Checkout
Order summary, then a real `confirmTrip` call (see Architecture above) — not a fake delay.

### Active trip dashboard
Real-time tracking via `TrafficStats`, an animated ring gauge, real battery % (`BatteryManager`), real network type (`ConnectivityManager.NetworkCapabilities`), burn-rate projection, and risk levels. Shows a small "Synced with Connecta" badge once a trip's confirmTrip call succeeds.

### Smart Mode
Monitors battery/network in real time, recommends reducing usage when battery's low or signal degrades, flags both at once as critical.

### Alerts
Pace warnings, weak-signal detection, 50%/80% usage milestones, filterable by severity.

### Trip history
Local Room-backed history (this device's own trips) plus a "Synced from Connecta" section (trips confirmed on the web, fetched via `tripsBySession`) — two genuinely different data sources shown together, clearly labeled as such.

## How the recommendation engine actually works

```
1. Slider screen → daily GB estimate from this trip's usage sliders
2. Local history → average GB/day from past trips (same destination weighted higher)
3. Blend: 40-60% history + 60-40% slider depending on whether history exists
4. needed = blended GB/day × trip days × 1.2 safety buffer
5. Pick the cheapest fixed plan that covers `needed`
   → if none does (common — the fixed catalog tops out at 20GB),
     fall back to the cheapest Unlimited plan covering the trip length
   → that same plan is both the default selection and shown first in the list
```

Step 5's fallback is the part that was actually broken for a while — see below.

## Known rough edges worth knowing about

Worth stating plainly rather than hiding: this recommendation logic had three real, sequential bugs found through actual testing, not hypothetical review — useful context for understanding what "verified working" means in this repo:

1. **Broken fallback.** When the blended estimate exceeded every fixed plan's size, the code picked the *biggest fixed plan regardless of whether it covered the need* instead of falling back to Unlimited — producing literally contradictory output ("118 GB needed → 20 GB recommended").
2. **Two disconnected recommendation signals.** A "best $/GB value" badge and the real usage-based recommendation could land on two different plans simultaneously, each looking like *the* answer.
3. **Stale selection state.** The view model lives for the whole app session; a plan tapped three test runs ago could still be "selected" by default on a fresh trip, because nothing reset it when the inputs that should invalidate it changed.

All three are fixed and re-verified live. Mentioning them here on purpose — a recommendation engine that's never had a real bug either hasn't been tested hard enough, or the bugs aren't being looked for.

## Why the name is still "SailGuard" under the hood

The package id (`com.sailguard.app`) and this repo's name weren't changed during the Connecta rebrand — only the user-facing app name, colors, and plan catalog were. Renaming an Android package id is a real, riskier refactor (touches every file's namespace) for zero user-visible benefit, since nobody sees a package id. The home-screen icon and in-app text say "Connecta"; the code underneath still says SailGuard. That's deliberate, not an inconsistency someone forgot to clean up.

## Architecture (code layout)

```
com.sailguard.app/
├── data/
│   ├── model/              TripConfig (now carries connectaTripId), SailyPlan, Alert, DeviceStatus
│   ├── repository/
│   │   ├── PlanRepository.kt       Connecta's flat global catalog, not per-country tiers
│   │   └── SettingsRepository.kt   the link code, persisted via SharedPreferences
│   ├── network/
│   │   └── ConnectaApiClient.kt    hand-rolled GraphQL client, OkHttp + org.json, fire-and-forget
│   └── db/                  Room — local trip history only, unrelated to the sync feature
├── ui/screens/               TripSetupScreen, DashboardScreen, HistoryScreen (now shows synced trips too), etc.
├── ui/theme/                 Connecta's palette (orange/navy/cream), not the original yellow/black
└── viewmodel/                 TripViewModel (now AndroidViewModel, owns the confirmTrip call),
                                DashboardViewModel (owns the usage-push), HistoryViewModel (fetches synced trips)
```

**Pattern:** MVVM, unidirectional data flow, `StateFlow` + `collectAsState()`. ViewModels are activity-scoped (`by viewModels()`), so they live for the whole app session — which is exactly what caused bug #3 above; worth remembering if something looks "stuck" across navigation.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| Networking | OkHttp + `org.json` (no GraphQL codegen on this side — the Go backend's gqlgen output is the source of truth, this client just speaks the same wire format by hand) |
| Database | Room + KSP (local history only) |
| Min/Target SDK | API 26 / API 36 |

## Running it

```bash
git clone https://github.com/trinayanswarup/SailGuard.git
cd SailGuard
```

Open in Android Studio, let Gradle sync, run on a device or emulator.

To test the Connecta sync specifically: run Connecta's backend locally (`go run ./cmd/server` in that repo), set `ConnectaApiClient.BASE_URL` to wherever it's reachable from your test device —

- **Emulator:** `http://10.0.2.2:8080/graphql` (the emulator's alias for your host machine's localhost)
- **Real device on the same Wi-Fi:** `http://<your-machine's-LAN-IP>:8080/graphql`, and `android:usesCleartextTraffic="true"` needs to stay in the manifest for plain HTTP in dev — Android blocks cleartext by default since API 28. **Remove that line once this points at a real deployed `https://` backend** — it's a dev-only crutch, not something that should ship.

Paste your web session ID (`localStorage.getItem("connecta_session_id")` in Connecta's browser console) into the "Link to Connecta" field on the trip setup screen, start a trip, and check Connecta's web history page for it.

## Built by

**Trinayan Swarup**, with AI-assisted development (Claude Code, in Android Studio via the JetBrains plugin).
