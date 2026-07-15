# Data Safety Form — Fill-in Cheat-Sheet (Islam Notify)

Where: **Play Console → your app → Policy / App content → Data safety → Manage / Start.**
Rule: every answer here MUST match `PRIVACY_POLICY.md`. Under-declaring is what gets apps flagged (Google scans your SDKs), so when unsure, declare it.

Key definitions:
- **Collected** = the app (or an SDK it bundles) transmits the data OFF the device.
- **Shared** = transferred to a *third party*. Transfer to a **service provider/processor (Firebase = one)** does **NOT** count as shared. → all our "Shared" answers are **No**.
- **Ephemeral** = kept only in memory, not stored. Ours are stored/cached → **No**.

---

## Screen 1 — Overview
Click **Next / Get started.**

## Screen 2 — Data collection and security (preliminary questions)

1. **"Does your app collect or share any of the required user data types?"** → **Yes**
2. **"Is all of the user data collected by your app encrypted in transit?"** → **Yes**
   (Firebase uses HTTPS/TLS; matches policy §6.)
3. **"Do you provide a way for users to request that their data is deleted?"** → **Yes**
   - Method: users can email the support address (noor.3la.noorcontact@gmail.com); uninstalling the app clears all on-device data.
   - ⚠️ Judgment call: the separate **account-deletion** requirement only applies to apps that let users *create accounts* — Islam Notify has none, so that doesn't apply. If the form insists on a deletion URL, use the privacy-policy URL.

## Screen 3 — Select data types collected

Check ONLY these; leave everything else unchecked:

- **Location** → ☑ Approximate location · ☑ Precise location
- **App activity** → ☑ App interactions  *(Firebase Analytics logs screen/events)*
- **App info and performance** → ☑ Crash logs · ☑ Diagnostics
- **Device or other IDs** → ☑ Device or other IDs

Leave UNCHECKED: Personal info (name/email/etc.), Financial info, Health & fitness, Messages, Photos/videos, Audio files, Files & docs, Calendar, Contacts, Web browsing history, In-app search history, Installed apps.

## Screen 4 — Answer per data type

For each checked type, the Console asks: Collected/Shared · Ephemeral? · Required or user-choice · Purpose.

| Data type | Collected | Shared | Ephemeral | Required / Optional | Purpose |
|---|---|---|---|---|---|
| Approximate location | Yes | No | No | Optional (user can deny permission) | App functionality |
| Precise location | Yes | No | No | Optional (user can deny permission) | App functionality |
| App interactions | Yes | No | No | Required (no in-app opt-out) | Analytics |
| Crash logs | Yes | No | No | Required | App functionality |
| Diagnostics | Yes | No | No | Required | App functionality / Analytics |
| Device or other IDs | Yes | No | No | Required | Analytics |

Notes:
- **Location = Optional** because the user can decline the runtime permission and still open the app. (Some declare it Required; Optional is the more accurate call for us.)
- **Everything Shared = No** — Firebase is a service provider, not a third party.

## Screen 5 — Review
Check the generated preview matches the table above, then **Save** and (later) submit with the release.

---

## Authoritative source for the Firebase rows
Google publishes exactly what Crashlytics + Analytics collect and how to declare them:
**https://firebase.google.com/docs/android/play-data-disclosure**
Use it to confirm the App-interactions / Diagnostics / Device-IDs rows if you want line-by-line certainty.

## Consistency check (do this before submitting)
The trio must agree:
1. This form ↔ 2. `PRIVACY_POLICY.md` ↔ 3. what the app does.
- Location: collected, not shared, on-device use ✔ (policy §2.1)
- Crash/diagnostics via Firebase: collected, not shared (service provider) ✔ (policy §2.2 / §3)
- No accounts, no ads, no data sale ✔ (policy §2.3)
