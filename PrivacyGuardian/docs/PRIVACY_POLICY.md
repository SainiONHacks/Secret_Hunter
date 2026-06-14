# Privacy Guardian — Privacy Policy

_Last updated: 2026-06-14_

Privacy Guardian ("the app") is a privacy-monitoring tool. This policy describes what
the app does and does not do with information. It is written to be plain and verifiable.

## Summary

**The app does not collect, transmit, sell, or share any personal or device data.**
All analysis happens on your device. There are no accounts, no analytics SDKs, and no
servers operated by us that receive your data.

## What the app accesses (on-device only)

To assess privacy exposure, the app reads metadata that the Android system already
exposes to apps:

- The list of installed applications and their declared permissions (via the
  QUERY_ALL_PACKAGES permission).
- The presence of known third-party SDK signatures inside each app's installation file
  (read locally; no app code is executed).

The app does **not** read your contacts, messages (SMS), call logs, photos, files, or
location. It analyses *other apps' permissions* — it does not exercise those permissions
itself.

## Storage

Scan results and any evidence logs are stored only on your device, encrypted at rest
using AES-256 with a key held in the Android Keystore. They are never uploaded.

## Reports

If you choose to export or share a report, the report is created on your device and is
shared only through the destination you pick (e.g. your email or messaging app). We do
not receive it.

## Notifications

With your permission, the app may show a local notification when a background re-scan
detects a privacy-relevant change (e.g. an app gaining a sensitive permission). These
notifications are generated on-device.

## Network

The Play Store edition of the app operates fully offline and contains no INTERNET
permission. (A separate testing build may include optional network features; those are
not part of the Play Store release.)

## Children

The app is a general security utility and is not directed at children.

## Contact

Questions about this policy: <add your contact email here before publishing>.
