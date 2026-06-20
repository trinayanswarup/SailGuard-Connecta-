# Connecta Android — Agent Guide

## Product identity

Native Android companion to Connecta (web). Should feel like one product across two devices — same colors, same plan prices, same language. Works fully standalone; sync with the web app is additive, never a hard dependency.

## Design

Matches Connecta web exactly:

- Background: `#FAFAF8`
- Text: `#020617` (near-black)
- Accent / CTAs: `#EA580C` (orange), **white text on top**
- Confirmed/synced state: `#059669` (emerald)

If adding a screen or component, check Connecta's existing palette before picking a color.

## Consumer language only

This is a travel app, not a developer tool. No mention of GraphQL, mutations, or sync errors in any user-facing string.

## Three things that must always be true

1. **Standalone always works** — no link code set means no network calls, no errors, no degraded experience
2. **Sync is fire-and-forget** — a failed backend call must never crash or block the UI
3. **Recommendation, selection, and list position agree** — the "History Pick" badge, the checked plan, and the first plan in the list must all point to the same plan

## Plan catalog

`PlanRepository.kt` is a manual copy of Connecta's `backend/internal/plans/mock_plans.go`. If Connecta's prices change, update this file too. No automated check exists.

## Shared GraphQL surface

`confirmTrip`, `submitUsageSnapshot`, `tripsBySession` are called by this app against Connecta's backend. Don't change their expected shape without coordinating with the Connecta repo.
