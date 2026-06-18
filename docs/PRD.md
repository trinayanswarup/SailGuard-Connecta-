# SailGuard / "Connecta" (Android) — Product Requirements Document

## Product Summary

A native Android app, user-facing as "Connecta," that does two things: stands alone as a complete trip-connectivity tool (plan recommendation, real device usage tracking, local history), and acts as the mobile client for Connecta's web product, sharing real backend state — purchases and usage data — through Connecta's Go/GraphQL API.

This was originally built as a standalone portfolio piece (targeting Saily's brand and internship application) before being deliberately rebranded and integrated as Connecta's companion app. Both the standalone identity and the integration are real, current, working features — not a pivot that left the old version half-functioning.

---

## Problem

Two separate but related problems:

1. **Before a trip**, travelers don't know how much data they'll actually need or which plan covers it — the same problem Connecta's web app solves, but someone planning from their phone shouldn't need a laptop open.
2. **During a trip**, a plan purchased on one device (commonly: bought it on the website while planning, days before departure) is invisible from whatever device the traveler actually has on them while traveling — there's no way to see "what did I buy" or "how much have I used" without going back to wherever the purchase happened.

This app solves #1 independently, and #2 specifically by sharing Connecta's backend rather than building a separate, disconnected mobile experience.

---

## Target Users

Same as Connecta's web product — leisure travelers, remote workers/business travelers needing reliable connectivity, with the added assumption that this user has the phone they're about to travel with in hand right now, which the website can't assume.

---

## Product Goals

1. A complete, real standalone trip-planning and usage-tracking experience — never gated behind the web integration.
2. Visual and pricing consistency with Connecta's web app, strong enough that using both feels like one product, not two.
3. Real device data only — `TrafficStats`, `BatteryManager`, `ConnectivityManager` — never simulated, even for a demo.
4. A purchase or usage event happening on either client should be visible on the other, without requiring any manual export/import step beyond the one-time link code.
5. The sync integration must be additive — if removed entirely, the standalone app should be unaffected.

---

## Non-Goals

- No Play Store publishing (would require a paid developer account — out of scope for a free-tier-only project; the APK is the distributable artifact).
- No user authentication — the link code (Connecta's web session ID) is the entire identity bridge.
- No real payment processor — `confirmTrip` persists a real, durable purchase record, but no money actually moves.
- No automated cross-repo check keeping this app's plan catalog in sync with Connecta's — that's a known, accepted manual-discipline gap, not a planned feature.
- No package id / repo rename — the brand-facing name changed, the technical identity (`com.sailguard.app`) deliberately didn't.

---

## Complete Feature Set

### 1. Trip Setup Wizard (3 steps)

Destination (region/country picker, cosmetic for browsing purposes), trip duration, usage style (Light/Medium/Heavy) on step 1; a detailed five-slider usage estimator (Video Streaming, Maps, Video Calls, Social Media, Hotspot) on step 2; plan review and selection on step 3.

### 2. Plan Recommendation

Drawn from Connecta's actual global plan catalog (1/3/5/10/20 GB fixed tiers, 5 Unlimited validity options — 10/15/20/25/30 days) — not a separate per-country pricing model. Blends the slider estimate with this device's own trip history (weighted toward same-destination trips when available) into a single GB-needed figure, then picks the cheapest plan that actually covers it, falling back to Unlimited when no fixed plan does. The recommended plan is simultaneously: the one shown with a "History Pick" badge, the one checked by default, and the one sorted to the top of the plan list — these three used to be three different, disagreeing things; they're now guaranteed to be the same plan (see "What's been real engineering work" below).

### 3. Checkout

Order summary → real `confirmTrip` GraphQL call against Connecta's backend. Since this app never runs Connecta's AI-driven `analyzeTrip` flow, it has no existing trip to attach a confirmation to — `confirmTrip` is called without a `tripId`, which the backend handles by creating and confirming a new trip in the same step.

### 4. Active Trip Dashboard

Real-time usage ring gauge (`TrafficStats`), real battery (`BatteryManager`), real network type (`ConnectivityManager.NetworkCapabilities`), burn-rate projection, risk levels (Safe/Warning/Critical). Pushes a `submitUsageSnapshot` every 30 seconds while a trip is active and linked. Shows a small "Synced with Connecta" indicator once that trip's `confirmTrip` call has succeeded.

### 5. Smart Mode

Real-time battery/network monitoring; recommends reducing usage on low battery or degraded signal, flags both simultaneously as critical.

### 6. Alerts

Pace warnings, weak-signal detection, 50%/80% usage milestones, severity filtering.

### 7. Trip History

Two distinct sections, clearly labeled, not merged: this device's own local Room-backed trip history (with the original "did the plan cover actual usage" check), and a "Synced from Connecta" section showing trips confirmed elsewhere on the same session — fetched via `tripsBySession`, which may include trips with no usage data at all (e.g. confirmed purely on the web, never run through this device).

---

## Cross-App Integration — the part that's actually new engineering, not just a mobile UI

**Identity:** a session ID, generated by Connecta's web app and pasted in once here as a "link code," persisted via `SettingsRepository` (SharedPreferences). No auth system; whoever has the code can read/write that session's data, an explicit accepted tradeoff matching Connecta's own web identity model.

**Networking:** a hand-rolled GraphQL client (`ConnectaApiClient.kt`, OkHttp + `org.json`) rather than a generated SDK — small, deliberate footprint matching this project's free-tier/minimal-dependency discipline. Every call is fire-and-forget: failures are logged, never surfaced to the user, never block the standalone experience.

**What's actually shared:**
- `confirmTrip` — see Checkout above
- `submitUsageSnapshot` — the Dashboard's 30-second real-usage push
- `tripsBySession` — what populates the "Synced from Connecta" history section

## What's been real engineering work, not just feature-building

Worth stating directly: the recommendation engine had three real, sequential bugs, found and fixed through actual device testing against a deliberately demanding scenario (Global destination, 20-day trip, Heavy usage, all sliders maxed — a case that exceeds the fixed catalog's largest plan):

1. A broken Unlimited-plan fallback that recommended a plan smaller than its own stated requirement.
2. Two independent "best plan" signals (a $/GB-value badge and the actual usage-based recommendation) that could land on different plans simultaneously.
3. Stale selection state — a plan tapped in an earlier, unrelated test session stayed selected by default on a fresh trip, because the activity-scoped ViewModel never reset it.

Also real: a cleartext-HTTP networking block (Android refuses plain `http://` by default since API 28 — required `android:usesCleartextTraffic="true"` as a dev-only fix, explicitly flagged to remove once a real `https://` backend exists), and the standard emulator-vs-real-device IP addressing gotcha (`10.0.2.2` vs. a real LAN IP). None of these were hypothetical — all were hit, diagnosed from actual Logcat output, and fixed.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, `StateFlow`, activity-scoped ViewModels |
| Networking | OkHttp + `org.json`, hand-rolled GraphQL calls |
| Local DB | Room + KSP |
| Min/Target SDK | API 26 / API 36 |

---

## What's Actually Done vs. Genuinely Still Open

### Done
- Full standalone trip wizard, recommendation engine (with all three bugs above fixed and re-verified), checkout, dashboard, Smart Mode, alerts, local history
- Full Connecta rebrand — colors, app name, plan catalog all matching the web product
- Full backend integration — `confirmTrip`, `submitUsageSnapshot`, `tripsBySession` — tested end to end on physical Android hardware against a real (local, not yet deployed) Connecta backend

### Genuinely still open
- `ConnectaApiClient.BASE_URL` is hardcoded, not environment-configurable — needs a manual edit per environment (emulator/real device/deployed)
- `usesCleartextTraffic="true"` needs removing once the backend is actually deployed over HTTPS
- No automated check keeps this app's plan catalog aligned with Connecta's — purely manual discipline today
- Not distributed anywhere — no Play Store listing, APK shared directly if needed

---

## Success Criteria

- Every feature in "Complete Feature Set" above works standalone, with zero network dependency, when no link code is set
- A trip confirmed here shows up in Connecta's web history under the same session, and vice versa
- Real cellular usage tracked here renders as a live chart on Connecta's web trip detail page
- The recommendation engine's badge, default selection, and list position all agree with each other for any input combination
- `./gradlew assembleDebug` succeeds and the app installs/runs on real hardware, not just an emulator

---

## Future (Post-MVP)

- Make `BASE_URL` environment-configurable instead of a hardcoded string
- Some automated way to keep the plan catalog aligned with Connecta's backend, rather than manual copy-paste discipline
- Push notifications for usage milestones (currently in-app only)
- Wider real-device testing beyond the single Pixel 7 this has been verified on so far
