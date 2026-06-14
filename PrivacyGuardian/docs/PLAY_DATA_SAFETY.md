# Google Play Data Safety form — answers

Use these answers when filling the Data Safety section in Play Console. They reflect the
Play (offline) build.

## Data collection and sharing
- **Does your app collect or share any of the required user data types?** No.
  - The app processes installed-app metadata entirely on-device and transmits nothing.
- **Is all of the user data encrypted in transit?** Not applicable (no data leaves the device).
- **Do you provide a way for users to request data deletion?** Data never leaves the
  device; users can clear all local data from within the app (scan history, evidence,
  canaries) or by clearing app storage.

## Data types
- Location: Not collected.
- Personal info: Not collected.
- Contacts: Not collected. (The app analyses whether *other* apps can access contacts;
  it does not read your contacts. The optional contact-canary feature is NOT in the Play build.)
- Messages / Photos / Files: Not collected.
- App activity / App info & performance: Processed on-device only, not collected or shed.

## Security practices
- Data is encrypted at rest (AES-256, Android Keystore).
- No data is transmitted off device in the Play build.

## Permissions to declare in the Console
- QUERY_ALL_PACKAGES — restricted permission; see PLAY_COMPLIANCE.md for the declaration text.
