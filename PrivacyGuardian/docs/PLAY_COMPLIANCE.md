# Google Play compliance notes

## Build separation (the key compliance decision)
The project ships two product flavors:

- **play** — the submittable build. Fully offline. Contains NO INTERNET permission, NO
  VpnService, NO contacts access. The in-progress network-monitor (VpnService) and the
  optional contact-canary are excluded at the MANIFEST level, so they cannot ship by
  accident. UI entry points are also gated behind `BuildConfig.FULL_FEATURES`.
- **full** — a sideload/testing build (`applicationId` suffix `.full`) that adds INTERNET,
  the VpnService network monitor, and the contact canary for development. NOT for Play.

Rationale: the VPN packet forwarder is not production-ready; an incomplete VPN would
blackhole connectivity and violate Play's "functions as described" requirement. Excluding
it from the Play build is the compliant choice until it is finished and separately declared.

## Permissions in the Play build

| Permission | Why | Policy notes |
|---|---|---|
| QUERY_ALL_PACKAGES | Core function: analyse permissions/trackers of installed apps | RESTRICTED. Requires a Permissions Declaration. Approved use case: "Device security (anti-virus, anti-malware) / app-permission analysis". Degrades gracefully if denied. |
| POST_NOTIFICATIONS | Local change alerts from the background re-scan | Standard runtime permission; requested at runtime on Android 13+. |

No other permissions are declared in the Play build. No accessibility service, no SMS/Call
Log access (which carry their own restricted-permission policies), no location.

## QUERY_ALL_PACKAGES declaration text (paste into Play Console)
> Privacy Guardian is a device-security app. Its core, user-facing feature scans installed
> applications to report their requested permissions and embedded tracking SDKs, and to
> compute a privacy-exposure score. Enumerating installed packages is essential to this
> function and is used for no other purpose. No package list or derived data leaves the device.

## "Functions as described" / deceptive behavior
- Scores are presented as privacy **exposure**, not accusations of malware. UI copy avoids
  "this app is malicious" / "this app stole your data".
- The app does not impersonate other apps and does not request permissions it does not use.

## Other Play policy checkpoints
- Target SDK 34 (meets the current target-API requirement).
- Privacy policy URL required — see docs/PRIVACY_POLICY.md (host it and add the URL).
- Data Safety form — see docs/PLAY_DATA_SAFETY.md.
- Foreground service: none in the Play build (the previous `specialUse` foreground service
  was removed; it only existed for the VPN, which is now full-flavor only).
- No ads, no payments, no third-party analytics SDKs.

## Pre-submission checklist
- [ ] Submit QUERY_ALL_PACKAGES permission declaration with the text above.
- [ ] Host PRIVACY_POLICY.md and add the URL in the Console.
- [ ] Complete the Data Safety form per PLAY_DATA_SAFETY.md.
- [ ] Build the **playRelease** variant, sign it, upload.
- [ ] Verify the uploaded APK/AAB contains no INTERNET / VpnService (it shouldn't, by flavor).
- [ ] Replace sample bundled data (trackers / GeoIP / reputation) with full datasets.
