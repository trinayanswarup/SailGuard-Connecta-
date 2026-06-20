# Connecta Android — Product Requirements

## Problem

The Connecta web app solves the "which plan should I buy" question before a trip. This app solves the rest:

1. **Planning from a phone** — not everyone planning a trip has a laptop open. The full recommendation flow should be native, not a mobile-optimized website.
2. **During the trip** — a plan purchased on the website is invisible once the traveler is actually traveling. There's no live view of whether the plan is holding up, and no warning before it runs out.
3. **Cross-device state** — the purchased plan, the usage tracking, and the history should be one thing across whatever devices the traveler has — not three separate, disconnected records.

## Solution

A native Android app that handles trip planning and usage tracking on the device, and uses the same backend as Connecta's web app to keep the two clients in sync. The sync is additive: remove the link code and everything works standalone.

## Core flows

### 1. Trip setup wizard (3 steps)

**Step 1 — Destination + duration + usage style**
Region/country picker for destination (cosmetic — the plan catalog is global, not per-country), trip duration, and a Light/Medium/Heavy usage style selector. An optional "Link to Connecta" field for the session ID.

**Step 2 — Usage fine-tuning**
Five sliders: Video Streaming, Maps, Video Calls, Social Media, Hotspot. Each shows real MB/hr rates. Produces a GB/day estimate that blends with historical data.

**Step 3 — Plan review**
Recommendation card showing the math clearly ("Your X past trips averaged Y GB/day + slider estimate Z GB/day blended → N GB needed → [plan]"), the recommended plan marked and sorted to the top, the full catalog below it for manual override.

### 2. Checkout

Order summary screen, then a `confirmTrip` call. This creates a durable purchase record in Postgres — visible on the Connecta website immediately. Since this app has its own recommendation logic and never runs `analyzeTrip`, `confirmTrip` is called without an existing `tripId`; the backend handles this by creating and confirming a new trip row in one step.

### 3. Active trip dashboard

Four things tracked in real time from real device APIs:

- **Data used** — `TrafficStats.getMobileRxBytes()` + `getMobileTxBytes()`, cellular only
- **Battery %** — `BatteryManager`
- **Network type** — `ConnectivityManager.NetworkCapabilities` (WiFi / LTE / 3G / 2G)
- **Burn rate** — projected days remaining at current pace

Every 30 seconds, a `submitUsageSnapshot` is pushed to the backend. The Connecta web app reads these to render a live usage chart on the trip detail page.

### 4. Smart Mode

Continuous background monitoring of battery and network quality. Recommends reducing usage when battery drops below 20% or network degrades to 3G/2G. Flags both simultaneously as critical.

### 5. Alerts

Pace warning ("at this pace your plan runs out X days before your trip ends"), weak signal detected, 50% and 80% data milestones. Filterable by severity.

### 6. History

Two sections, rendered separately because they represent different things:

**This device's trips** — saved to local Room database when a trip ends. Shows destination, duration, GB used vs. plan size, cost, and whether the plan was sufficient.

**Synced from Connecta** — trips confirmed on the web, fetched via `tripsBySession`. Shows destination, dates, plan purchased, and a "Web checkout" label. These trips may have no usage data (they were never run through this device) — that's expected, not an error.

## Plan catalog

Matches Connecta's web backend exactly — a flat global catalog, no per-country pricing:

| Data      | Price         | Validity   |
| --------- | ------------- | ---------- |
| 1 GB      | $3.99         | 7 days     |
| 3 GB      | $6.99         | 30 days    |
| 5 GB      | $9.99         | 30 days    |
| 10 GB     | $15.99        | 30 days    |
| 20 GB     | $22.99        | 30 days    |
| Unlimited | $34.99–$71.99 | 10–30 days |

Kept in sync with the backend manually. Both clients quoting the same prices matters for the cross-client purchase story.

## Identity model

No auth. The session ID from Connecta's web app (`connecta_session_id` in `localStorage`) is pasted once into this app as a "link code" and stored locally. That UUID is the only thing tying both clients to the same Postgres rows.

This is a deliberate, explicit design decision matching how the web side works. The tradeoff: anyone with the UUID can read that session's trips. The justification: for an anonymous travel planning tool, the data at stake is "which data plan did I buy for Japan" — low sensitivity, and the friction of real auth at this stage is a worse product outcome than the theoretical exposure.

## Non-goals

- No Play Store distribution (requires a paid developer account — APK shared directly)
- No WiFi usage tracking — `TrafficStats` measures cellular only by design
- No automated plan catalog sync with the backend — maintained manually
- No push notifications — alerts are in-app only

## Decisions worth noting

**Hand-rolled GraphQL client over Retrofit/Apollo.** OkHttp + `org.json` is three files and no code generation. For four GraphQL operations that are stable and don't need type safety across a generated schema, the minimal footprint is the right call. Apollo would be appropriate if this grew to a full client-side schema with many operations.

**Fire-and-forget sync.** Every backend call returns void from the UI's perspective — failures are logged, never surfaced. The alternative is showing sync error states to a traveler who is actively using the app while traveling, which is worse than silent retry. The backend being unreachable can't degrade the core usage-tracking experience.

**Activity-scoped ViewModels.** Survive tab switches, hold cross-screen state (active trip, current usage readings). The tradeoff is that state accumulated in a ViewModel across multiple test sessions doesn't reset automatically — requires explicit reset on inputs that should invalidate it (destination, duration, usage style changes all reset the plan selection).
