# SailGuard / "Connecta" Agent Guide

## Product Intent

This is the native Android companion to Connecta (web). It should feel like one product across two devices, not "an Android app, separately, that happens to talk to the same backend." A traveler who's used Connecta on the web should recognize this app immediately — same colors, same plan prices, same language. For full product context, read `docs/PRD.md`.

The app has two genuinely separate identities that both need to stay true at once:
- **Standalone product**: a real, complete trip-planning and usage-tracking app. Works with zero network dependency, no link code required.
- **Connecta's mobile client**: when linked, shares real backend state with the web app — purchases and live usage sync both directions.

Never build a feature that makes the standalone identity depend on the synced one. A user who never sets a link code should never see a broken or degraded experience.

## Visual Direction — ported from Connecta, not invented separately

- Background: Connecta's cream (`#FAFAF8`)
- Primary text/icons: near-black navy (`#020617`)
- Accent / CTAs / active states: Connecta's orange (`#EA580C`), white text on top of it (not dark text — that was the old Saily-yellow convention and is wrong now)
- Confirmed/success/synced state: emerald (`#059669`)

If you're adding a new screen or component and reaching for a color not already in `ui/theme/Color.kt`, check Connecta's web `globals.css`/Tailwind usage first rather than picking something that merely looks fine in isolation — visual consistency across the two apps is the actual point.

## Pricing — one shared catalog, two repos, no automated sync

`PlanRepository.kt`'s plan list is a deliberate, manual copy of Connecta's `backend/internal/plans/mock_plans.go`. There is no build-time or runtime check that keeps these aligned — if Connecta's prices change, this file needs a matching manual update, and there's nothing that will warn you if it drifts. Treat this file as "this app's local cache of a fact that's actually owned by the other repo," not as this app's own pricing decision.

## Core Flows

### Trip Setup Wizard
Destination (cosmetic — flags/country names for browsing, but pricing is global/flat, not per-country) → duration + usage style → detailed usage sliders → plan recommendation with a clear, honest explanation of the math (see Recommendation Engine below — this has had real bugs, and the fix was making the explanation and the actual selection agree with each other, not just making the math right in isolation).

### Checkout
Calls `confirmTrip` for real. No fake delay, no simulated success — a failure should be visible in Logcat at minimum, even though it's not shown to the user (fire-and-forget is the design, but "fire-and-forget" doesn't mean "untraceable").

### Dashboard
Real device data only — `TrafficStats`, `BatteryManager`, `ConnectivityManager`. Never simulate or hardcode a usage number, even for a demo; if there's nothing to show yet, show an honest empty/zero state.

### History
Two sources rendered together, clearly distinguished: this device's own local Room trips, and trips confirmed elsewhere on the same session (the "Synced from Connecta" section, sourced from `tripsBySession`). Don't merge these into one undifferentiated list — they mean different things (this device tracked usage for one; the other might have no usage data attached to it at all, since it was never run through this device).

## Recommendation Engine Rules

The blended history+slider calculation, the fixed→Unlimited fallback, and which plan gets the "checked" default and the "first in list" position must all agree with each other. This was not always true — see `CLAUDE.md` for the specific bugs already found and fixed. When changing this logic, the standing test case is: Global destination, 20-day trip, Heavy usage, all sliders maxed — a scenario that exceeds the fixed catalog's largest plan (20GB) and is what exposed every bug so far. Anything less demanding won't catch a regression.

## Frontend (Compose) Rules

- Compose + Material 3, MVVM, `StateFlow`.
- ViewModels are activity-scoped — they live for the entire app session. Any state that should reset between trips needs an explicit reset, not an assumption that a fresh screen means fresh state.
- No engineering language in user-facing copy ("GraphQL", "mutation", "sync failed" are not consumer language — if a sync-related message must be shown at all, which is rare given the fire-and-forget design, it should read like a product, not a stack trace).

## Backend Integration Rules

- `ConnectaApiClient.kt` is the only place that talks to the network. Every function in it must be fire-and-forget: try/catch, log on failure, return a safe default (null/empty list), never throw past the function boundary.
- All calls are blocking (OkHttp `execute()`, not `enqueue()`) — always wrap call sites in `viewModelScope.launch(Dispatchers.IO)`.
- `BASE_URL` is a hardcoded string, not env-configurable yet. Changing it (emulator ↔ real device ↔ deployed backend) is a manual edit — know which of the three you're testing against before debugging a "nothing's syncing" report.

## MVP Constraints

- No Play Store publishing planned (would need a paid developer account — out of scope for a free-tier-only project).
- No new networking library beyond OkHttp + `org.json` without a real reason — this was a deliberate minimal choice, not an oversight.
- Standalone experience is never allowed to regress in service of the synced one.

## Verification

- `./gradlew assembleDebug` before claiming anything works.
- Test on a real device when touching anything usage-tracking or sync related — the emulator can't meaningfully exercise cellular `TrafficStats`, and real bugs (cleartext traffic blocking, LAN-IP reachability) only show up on physical hardware.
- Check Logcat (`ConnectaApiClient` tag) to confirm a sync call actually succeeded — a clean-looking UI proves nothing on its own, since failures are silent by design.
- Re-run the standing Heavy/20-day/Global test case after any change to the recommendation logic.
