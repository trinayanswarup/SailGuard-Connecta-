# Connecta Android — Development Guide

## Running locally

Open in Android Studio, let Gradle sync, run on a device or emulator.

```bash
git clone https://github.com/trinayanswarup/SailGuard.git
cd SailGuard
```

## Tech stack

- **Language** — Kotlin 2.0.21
- **UI** — Jetpack Compose + Material 3
- **Architecture** — MVVM, StateFlow, activity-scoped ViewModels
- **Networking** — OkHttp + `org.json` (hand-rolled GraphQL client)
- **Local DB** — Room + KSP

## Testing against the Connecta backend

Run `go run ./cmd/server` in the Connecta repo, then set `ConnectaApiClient.BASE_URL`:

- **Emulator** → `http://10.0.2.2:8080/graphql`
- **Real device (same Wi-Fi)** → `http://<your-LAN-IP>:8080/graphql`

`android:usesCleartextTraffic="true"` is set in the manifest for local dev. Remove it once pointing at a real `https://` backend.

## Key rules

- All `ConnectaApiClient` calls are fire-and-forget — wrap in `Dispatchers.IO`, catch all exceptions, return null/empty, never throw
- `./gradlew assembleDebug` after every change before testing on device
- ViewModels are activity-scoped — state persists across navigation. Explicitly reset plan selection when destination/duration/usage style changes
- Check Logcat (`ConnectaApiClient` tag) when sync appears to do nothing — failures are silent in the UI by design

## Package name vs. app name

App displays as "Connecta." Package id is `com.sailguard.app`. This is intentional — don't rename the package id.
