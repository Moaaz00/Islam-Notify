# Privacy Policy for Islam Notify

**Effective date:** 15 July 2026
**Last updated:** 15 July 2026

This Privacy Policy explains how the **Islam Notify** mobile application handles information when you use it. Islam Notify is a free Android application that provides Islamic prayer time notifications, the athan (call to prayer) sound, events, and a Hijri date.

We have designed Islam Notify to keep your personal information on your device wherever possible. We do **not** operate any server that stores your data, we do **not** require an account, and we do **not** sell your information or use it for advertising.

If you have any questions about this policy, contact us at **noor.3la.noorcontact@gmail.com**.

---

## Summary (the short version)

- **Location:** Used to calculate your prayer times and show your city name. Prayer times are calculated **on your device**. Your coordinates are stored only on your device and are never sent to any server we operate.
- **Crash & diagnostic data:** We use Google Firebase (Crashlytics and Analytics) to receive anonymous crash reports and basic diagnostics so we can fix bugs and keep the App stable. This is the only information that leaves your device to a third party.
- **No accounts, no ads, no sale of data, no user tracking profiles.**

The sections below explain this in full.

---

## 1. Who we are

Islam Notify is published by an independent developer. For any privacy question, data request, or complaint, you can reach us at:

- **Email:** noor.3la.noorcontact@gmail.com

For the purposes of the EU/UK General Data Protection Regulation (GDPR), the developer is the "data controller" for the limited data described in this policy.

---

## 2. Information we collect and how we use it

### 2.1 Location information

**What:** With your permission, the App accesses your device's approximate and/or precise location (`ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`).

**Why:** Your location is required to:
- calculate accurate prayer times for your position, and
- display the name of your city/area in the App.

**How it is handled:**
- Prayer times are calculated **locally on your device**. Your coordinates are **not** sent to any server operated by us — we do not run one.
- Your last known coordinates and city name are cached **only on your device** (in the App's local storage) so the App can work offline and show prayer times quickly.
- Your location is **never** attached to crash reports or diagnostic data.

**City-name lookup (important detail):** To convert your coordinates into a readable city name, the App uses Android's built-in geocoding service (the operating system's `Geocoder`). On most devices this system service may transmit your coordinates to **Google** over the internet to return the corresponding place name. This is a function of the Android operating system, not of a server we control; the coordinates are used only to return the place name, and we keep only the resulting text (e.g. your city name). The App itself does not store or transmit your location to any server it operates.

**Control:** Location access is optional and controlled by the Android permission system. You can grant or revoke it at any time in your device's **Settings → Apps → Islam Notify → Permissions**. If you deny location access, prayer-time accuracy will be reduced or unavailable.

### 2.2 Crash and diagnostic data (Google Firebase)

The App uses **Google Firebase Crashlytics** and **Google Firebase Analytics** (Analytics is a dependency of Crashlytics) to help us detect and fix crashes and to understand the App's overall stability.

**What may be collected by these Google services:**
- crash reports and stack traces,
- the state of the App at the time of a crash,
- device information such as device model, operating-system version, and language,
- a randomly generated installation identifier used by Crashlytics/Analytics,
- basic usage and diagnostic events, and
- an approximate, coarse region derived from your **IP address** (this is a standard function of analytics services and is unrelated to the GPS location described in Section 2.1).

**Why:** solely to diagnose crashes, improve reliability, and understand aggregate app health. We do **not** use this data to identify you personally or to build an advertising profile.

**Processor:** This data is collected and processed by **Google** as our service provider, in accordance with [Google's Privacy Policy](https://policies.google.com/privacy) and the [Firebase Privacy and Security documentation](https://firebase.google.com/support/privacy). Crash reporting is enabled in the published version of the App.

### 2.3 Information we do NOT collect

For clarity, Islam Notify does **not**:
- require you to create an account or provide your name, email, or phone number;
- collect your contacts, photos, files, calendar, camera, or microphone;
- contain third-party advertising or advertising identifiers;
- contain in-app purchases;
- sell or rent your personal information to anyone;
- track you across other apps or websites.

---

## 3. How your information is shared

We do not share your personal information with third parties, **except**:

- **Google Firebase (Crashlytics & Analytics)** — receives the crash and diagnostic data described in Section 2.2, acting as our service provider.
- **Android system geocoding (Google)** — as described in Section 2.1, the operating system may send coordinates to Google to return a place name.
- **Legal requirements** — we may disclose information if required to do so by law, regulation, or valid legal process. (In practice we hold almost no data that could be disclosed, as most data never leaves your device.)

We do **not** sell your personal information, and we do **not** share it for advertising or marketing.

---

## 4. Device permissions we use

The App requests the following Android permissions. Each is used only for the feature described:

| Permission | Why the App uses it |
|---|---|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Determine your position to calculate prayer times and show your city name. |
| `POST_NOTIFICATIONS` | Show prayer-time reminders and notifications. |
| `USE_EXACT_ALARM` | Deliver athan (call to prayer) sounds and prayer notifications at the exact prayer time, since precise timing is the core function of the App. |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Reliably play the athan (call to prayer) sound at prayer times. |
| `WAKE_LOCK` | Allow the device to process and play a prayer alert even when idle. |
| `VIBRATE` | Provide vibration for prayer alerts. |
| `INTERNET` | Send crash/diagnostic data to Firebase and allow the operating system's geocoding lookup. |

---

## 5. Data retention

- **Data stored on your device** (your cached location, calculated prayer times, and app settings) remains on your device until you clear the App's data or uninstall the App. Uninstalling the App removes this data from your device.
- **Crash and diagnostic data** held by Google Firebase is retained according to Google's data-retention policies. Crashlytics crash reports are typically retained by Google for up to **90 days**; Analytics data is retained according to Google's configurable retention periods. See Google's documentation linked in Section 10.

---

## 6. Data security

Data transmitted to Google Firebase is encrypted in transit using industry-standard encryption (HTTPS/TLS). Data stored on your device is protected by Android's application sandbox. No method of transmission or storage is 100% secure, but because the App keeps most data on your device and collects no accounts or contact details, the amount of personal data at risk is minimal.

---

## 7. Children's privacy

Islam Notify is intended for a general audience and is **not directed at children under the age of 13** (or the equivalent minimum age in your jurisdiction). We do not knowingly collect personal information from children. If you believe a child has provided us with personal information, please contact us and we will take appropriate steps.

---

## 8. Your privacy rights

Because the App keeps most data on your device and does not require an account, you can exercise the most important controls directly:

- **Access/revoke location:** Android **Settings → Apps → Islam Notify → Permissions**.
- **Delete on-device data:** clear the App's data or uninstall the App.

Depending on where you live, you **might** also have additional rights over the limited diagnostic data processed by Google on our behalf:

**EEA / UK (GDPR):** You have the right to access, correct, delete, restrict, or object to the processing of your personal data, and to data portability. Our legal bases for processing are your **consent** (for location access, which you grant through the Android permission prompt) and our **legitimate interests** (for crash diagnostics used to keep the App stable and secure). You may withdraw consent at any time by revoking the permission. You also have the right to lodge a complaint with your local data protection authority.

**California (CCPA/CPRA):** We do **not** sell or "share" (as defined under California law) your personal information, and we do not use it for cross-context behavioral advertising. California residents may request access to or deletion of personal information we hold.

To make any request, email us at **noor.3la.noorcontact@gmail.com**. We may need to verify your request. Note that much of the diagnostic data is pseudonymous (tied only to a random installation identifier), which can limit our ability to locate data associated with a specific person.

---

## 9. International data transfers

The App is available internationally. When crash/diagnostic data is processed by Google Firebase, it may be transferred to and processed on servers located in the United States or other countries where Google or its sub-processors operate. Google implements safeguards for such transfers as described in its privacy documentation (Section 10).

---

## 10. Third-party services

The following third-party service is used by the App. We encourage you to review its privacy terms:

- **Google Firebase (Crashlytics & Analytics)** —
  [Google Privacy Policy](https://policies.google.com/privacy) ·
  [Firebase Privacy & Security](https://firebase.google.com/support/privacy)

---

## 11. Changes to this policy

We may update this Privacy Policy from time to time. When we do, we will revise the "Last updated" date at the top of this page and, where appropriate, provide notice within the App. Your continued use of the App after changes take effect constitutes acceptance of the updated policy.

---

## 12. Contact us

If you have any questions, concerns, or requests regarding this Privacy Policy or your data, please contact:

Email: **noor.3la.noorcontact@gmail.com**
