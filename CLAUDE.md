# SailGuard / "Connecta" — Claude Code Guide

## What this app actually is, right now

User-facing name is "Connecta" (home screen, splash screen, in-app copy). Package id, repo name, and most internal references are still `com.sailguard.app` / SailGuard — that's intentional, not unfinished. Don't "fix" the package id without understanding this first; it's a deliberate scope boundary from the rebrand (see `docs/PRD.md`).

This app does two genuinely separate things:
1. **Standalone**: a trip-setup wizard, plan recommendation, real device usage tracking, local Room history. Works with zero network dependency.
2. **Synced**: when a "link code" (Connecta's web session ID) is set via `SettingsRepository`, it also talks to Connecta's Go/GraphQL backend — the same backend the separate Connecta web repo uses — to confirm purchases and push usage data both directions.

Every sync call is fire-and-forget. A failed/unreachable backend must never affect the standalone experience — no crash, no blocking, no user-facing error from a sync failure. If you're adding a new sync call, follow the existing pattern in `ConnectaApiClient.kt` exactly: try/catch around the OkHttp call, log a warning on failure, return null/empty, never throw out of the function.

## Repo structure

```
com.sailguard.app/
├── data/
│   ├── model/Models.kt              TripConfig.connectaTripId — set after a successful confirmTrip
│   ├── repository/
│   │   ├── PlanRepository.kt         flat global catalog ported from Connecta's mock_plans.go — keep these aligned manually, no automated check exists across repos
│   │   └── SettingsRepository.kt     SharedPreferences wrapper, just the link code
│   ├── network/ConnectaApiClient.kt   hand-rolled GraphQL client — OkHttp + org.json, blocking calls, always wrap in Dispatchers.IO
│   └── database/AppDatabase.kt, dao/TripHistoryDao.kt   Room, local history only, unrelated to sync
├── ui/screens/
│   ├── TripSetupScreen.kt            the wizard — also where the recommendation-engine logic lives (see below)
│   ├── DashboardScreen.kt             owns the 30s usage poll + push
│   └── HistoryScreen.kt               renders BOTH local Room trips AND tripsBySession results — two different data sources, don't conflate them
└── viewmodel/
    ├── TripViewModel.kt               AndroidViewModel (was plain ViewModel before the sync feature — needed Application context for SettingsRepository)
    ├── DashboardViewModel.kt          owns confirmTrip's resulting connectaTripId once bound; pushUsageSnapshot lives here
    └── HistoryViewModel.kt            fetches tripsBySession on init if a link code is set
```

## Tech stack

| Layer | Technology |
|---|---|
| Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, `StateFlow` + `collectAsState()`, ViewModels via `by viewModels()` (activity-scoped) |
| Networking | OkHttp + `org.json` — no Retrofit, no kotlinx.serialization, deliberately minimal |
| Local DB | Room + KSP |

## Real gotchas already hit once — don't re-discover these the hard way

**ViewModels are activity-scoped, which means they live for the whole app session.** A value set in one trip-setup pass (e.g. `selectedPlan`) silently persists into the next one unless something explicitly resets it. This caused a real bug: a plan tapped three test runs ago stayed "selected" by default on an unrelated fresh trip. If you're touching anything in `TripViewModel`'s state that should be per-trip, ask explicitly: does this get reset when destination/duration/usage style changes? If not, it probably should.

**Android blocks plain HTTP by default (API 28+).** `android:usesCleartextTraffic="true"` is in the manifest specifically so dev testing against a local `http://` backend works. This is a dev-only crutch — remove it once `ConnectaApiClient.BASE_URL` points at a real `https://` deployment. Don't add this flag casually elsewhere in the app; it's a real security control being deliberately, narrowly bypassed for one known reason.

**The emulator can't reach `localhost`.** `10.0.2.2` is the emulator's alias for the host machine's localhost. A real device needs the host's actual LAN IP instead. Get this wrong and you'll see a generic "unreachable" warning in Logcat with no other clue — check `BASE_URL` first before debugging anything else network-related.

**`TrafficStats.getMobileRxBytes()`/`getMobileTxBytes()` measure cellular only, not Wi-Fi.** If you're testing on a phone connected to Wi-Fi (which it needs to be to reach a LAN-hosted dev backend), this counter will barely move regardless of actual phone usage, because Android routes traffic over Wi-Fi when available. This isn't a bug — it's a real constraint of testing usage-tracking against a non-deployed backend. Don't "fix" the counter; the fix is deploying the backend somewhere reachable over real cellular.

**gqlgen on the backend side requires Go ≥1.25.** Not directly this repo's problem, but if a sync feature stops working after a Connecta backend change, check whether their `schema.graphqls` actually got regenerated (`gqlgen generate`) before assuming this app's client code is wrong.

## The recommendation engine — read before touching `TripSetupScreen.kt`'s plan logic

```
slider estimate (GB/day) + historical average (GB/day, same-destination weighted) → blended estimate
blended × trip days × 1.2 safety buffer = GB needed
cheapest fixed plan covering that, else cheapest Unlimited plan covering the trip length
```

This exact logic had three real, sequential bugs (broken Unlimited fallback, two disconnected "best plan" signals competing for the same badge, stale `selectedPlan` state) — all fixed and re-verified. If you're changing this function, re-run the actual scenario that exposed each one: Global destination, 20-day trip, Heavy usage style, all five usage sliders maxed. That combination needs more than the catalog's largest fixed plan (20GB) covers, which is exactly what exposed the first two bugs — a lighter test case won't catch a regression here.

## Working rules

- `./gradlew assembleDebug` after any change — confirm it actually compiles before testing on a device.
- Real-device testing matters more here than usual: the emulator can't exercise `TrafficStats` cellular counters meaningfully, and several real bugs (cleartext blocking, the LAN-IP issue) only show up on physical hardware.
- Check Logcat filtered on `ConnectaApiClient` when a sync feature "doesn't seem to do anything" — fire-and-forget by design means failures are silent in the UI on purpose; Logcat is where the truth actually is.
- Branch before any change, same discipline as the Connecta repo — don't commit straight to `main`.
