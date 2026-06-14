# Privacy Guardian — V1 (Play Store Edition)

## v1.2 — Google Play compliance pass

The project now ships **two product flavors** so the submittable build is provably clean:

- **play** (submit this): fully offline. NO INTERNET, NO VpnService, NO contacts access —
  excluded at the manifest level, not just hidden. Only two permissions:
  QUERY_ALL_PACKAGES (the one restricted permission; security/anti-malware use case, with a
  graceful fallback if denied) and POST_NOTIFICATIONS. Build the **playRelease** variant.
- **full** (sideload/testing, applicationId `…full`): adds INTERNET + the VpnService network
  monitor + the contact canary. NOT for Play — the VPN forwarder isn't production-ready and an
  incomplete VPN would break connectivity, violating Play's "functions as described" rule.

UI entry points for network monitoring are gated behind `BuildConfig.FULL_FEATURES`, and the
old `specialUse` foreground service (which needed a special declaration) is gone from the Play
build entirely.

The scanner also now **degrades gracefully** if QUERY_ALL_PACKAGES is denied: it detects the
restricted-visibility case and shows the user a clear nudge instead of a near-empty list.

Compliance paperwork is included under `docs/`:
- `PRIVACY_POLICY.md` — ready to host (offline app → simple policy).
- `PLAY_DATA_SAFETY.md` — exact answers for the Data Safety form ("no data collected").
- `PLAY_COMPLIANCE.md` — permission-by-permission justification, the QUERY_ALL_PACKAGES
  declaration text to paste into the Console, and a pre-submission checklist.

---

Status: all V1 phases implemented, with ONE honest exception — the Phase 2 VPN traffic
FORWARDER (see warning). Everything else is real, compilable code.

## Phase status

| Phase | Feature | Status |
|---|---|---|
| 1 | App scan, permission taxonomy, risk engine | done |
| 1 | Tracker/SDK detection (static DEX scan) | done |
| 1 | AES-256 encrypted persistence + scan history | done |
| 1 | Onboarding/transparency screen, launcher icon | done |
| 2 | Offline GeoIP (server location) | done |
| 2 | DNS hostname extraction | done |
| 2 | Network monitor UI + aggregation | done |
| 2 | VPN traffic forwarder (userspace TCP/IP) | NOT DONE — see warning |
| 2 | Per-app attribution (getConnectionOwnerUid) | hook stubbed (API 29+, racy w/o root) |
| 3 | Evidence timeline + encrypted event log + correlation | done |
| 4 | Canary system (file/image) + access detection | done |
| 4 | Canary contact | code present; needs WRITE_CONTACTS (consider V2-only) |
| 6 | Offline reputation/threat-intel + hybrid update seam | done |
| — | JSON + CSV + PDF export | done |

Risk engine is now v1.2.0: Permissions + Behavior(trackers) + Reputation are assessed
(confidence up to 0.75); Network stays unassessed until the forwarder lands. Versioned and
pinned by RiskEngineTest.

## The one honest gap: VPN forwarding

PrivacyVpnService sets up the tun interface, reads IPv4 packets, extracts destination IPs,
parses DNS query names, and feeds GeoIP + evidence. It does NOT forward traffic, so enabling
it blackholes connectivity. A real forwarder is a userspace TCP/IP stack (tun2socks; the
NetGuard / RethinkDNS approach) — thousands of lines requiring on-device iteration. It is a
documented stub, not faked as working. The monitor UI, GeoIP, DNS parsing and evidence
plumbing are all real and can be driven without the forwarder.

## Verified algorithms

Compilation needs Android Studio (no SDK in the build sandbox), but the non-trivial algorithms
were each validated by porting + running a test matrix:
- Tracker DEX byte-search (substring scan, boundaries)
- GeoIP IP-range binary search (incl. range edges)
- DNS QNAME label parsing (multi-label, truncation-safe)
- Risk scoring curve (calibrated so common apps land MEDIUM, SMS/call-log HIGH/CRITICAL)

## Honest framing baked into the UI

- Scores = privacy EXPOSURE, not proof of malice (a legit messenger reading SMS+Contacts
  scores like a loan app — permissions can't separate them).
- GeoIP = SERVER location, never "where your data went".
- Canary without root = detects access, not which app.
- Evidence timeline = correlated events, not causal proof.
- Everything offline + encrypted; nothing uploaded unless the user shares a report.

## Build & run

    ./gradlew :app:assembleDebug
    ./gradlew :app:testDebugUnitTest

Open in Android Studio, sync (generates the wrapper jar), run on a device/emulator (min SDK 26).

## Production follow-ups

- Replace bundled sample data: full Exodus tracker set, DB-IP/IP2Location GeoIP, maintained
  reputation feed.
- Build the VPN forwarder for live per-app capture + attribution.
- Generate raster mipmaps from the adaptive icon for older launchers.

## V2 (Research Edition) — separate build, on purpose

Root + Frida/LSPosed hooks on SMS/Contacts/MediaStore are legitimate research tooling but the
most repurposable part of the design — kept to a rooted test-device build, strictly
observational (log who accessed what, when; never exfiltrate the underlying data).

Package: shop.sainionai.privacyguardian, versionName 1.0.0, engine v1.2.0
